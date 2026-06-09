package astrogeist.ui;

import astrogeist.model.Snapshot;
import astrogeist.persist.AppSettings;
import astrogeist.persist.XmlSettingsStore;
import astrogeist.scanner.ConfigurableScanner;
import astrogeist.scanner.ScannerConfigReader;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class ScanConfigDialog extends JDialog {

    private static final DateTimeFormatter MONTH_FMT =
        DateTimeFormatter.ofPattern("MMM yyyy").withZone(ZoneId.systemDefault());

    private final AppSettings settings;
    private final XmlSettingsStore settingsStore;

    private final JTextField folderField = new JTextField(40);
    private final JComboBox<String> scannerBox;

    private final JProgressBar progressBar = new JProgressBar();
    private final JLabel statusLabel       = new JLabel(" ");

    // Separate buttons — never share a component between two parents
    private final JButton scanBtn   = new JButton("Scan");
    private final JButton cancelBtn = new JButton("Cancel");
    private final JButton stopBtn   = new JButton("Cancel");
    private final JButton closeBtn  = new JButton("Close");

    private final CardLayout cards     = new CardLayout();
    private final JPanel     cardPanel = new JPanel(cards);

    // Button bar outside the cards so buttons are never reparented
    private final JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    private List<Snapshot> result;
    private SwingWorker<List<Snapshot>, String> worker;

    public ScanConfigDialog(JFrame owner, AppSettings settings, XmlSettingsStore settingsStore) {
        super(owner, "Scan", true);
        this.settings      = settings;
        this.settingsStore = settingsStore;

        var reader    = new ScannerConfigReader();
        var available = reader.listAvailable();
        scannerBox = new JComboBox<>(available.toArray(new String[0]));
        var last = settings.getLastScanner();
        for (int i = 0; i < available.size(); i++) {
            if (available.get(i).equals(last)) { scannerBox.setSelectedIndex(i); break; }
        }
        folderField.setText(settings.getLastFolder());
        folderField.setEditable(false);

        scanBtn.addActionListener(e   -> startScan());
        cancelBtn.addActionListener(e -> dispose());
        stopBtn.addActionListener(e   -> { if (worker != null) worker.cancel(true); });
        closeBtn.addActionListener(e  -> dispose());

        cardPanel.add(buildConfigCard(owner), "config");
        cardPanel.add(buildProgressCard(),    "progress");
        cards.show(cardPanel, "config");

        showConfigButtons();
        getRootPane().setDefaultButton(scanBtn);

        var content = (JPanel) getContentPane();
        content.setLayout(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        content.add(cardPanel,  BorderLayout.CENTER);
        content.add(buttonBar,  BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(500, getHeight()));
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    // ── card builders ──────────────────────────────────────────────────────────

    private JPanel buildConfigCard(JFrame owner) {
        var browseBtn = new JButton("Browse…");
        browseBtn.addActionListener(e -> browse(owner));

        var folderRow = new JPanel(new BorderLayout(4, 0));
        folderRow.add(folderField, BorderLayout.CENTER);
        folderRow.add(browseBtn,   BorderLayout.EAST);

        var form = new JPanel(new GridBagLayout());
        var gbc  = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Folder:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        form.add(folderRow, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        form.add(new JLabel("Scanner:"), gbc);
        gbc.gridx = 1; form.add(scannerBox, gbc);

        return form;
    }

    private JPanel buildProgressCard() {
        progressBar.setStringPainted(true);
        progressBar.setString("");

        var panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        var gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets  = new Insets(4, 0, 4, 0);

        gbc.gridy = 0; panel.add(progressBar, gbc);
        gbc.gridy = 1; panel.add(statusLabel,  gbc);
        return panel;
    }

    // ── button bar helpers ────────────────────────────────────────────────────

    private void showConfigButtons() {
        buttonBar.removeAll();
        buttonBar.add(cancelBtn);
        buttonBar.add(scanBtn);
        buttonBar.revalidate();
        buttonBar.repaint();
    }

    private void showScanningButtons() {
        buttonBar.removeAll();
        buttonBar.add(stopBtn);
        buttonBar.revalidate();
        buttonBar.repaint();
    }

    private void showCloseButton() {
        buttonBar.removeAll();
        buttonBar.add(closeBtn);
        buttonBar.revalidate();
        buttonBar.repaint();
    }

    // ── scan execution ────────────────────────────────────────────────────────

    private void startScan() {
        var folderText = folderField.getText().trim();
        if (folderText.isBlank() || !Files.isDirectory(Path.of(folderText))) {
            JOptionPane.showMessageDialog(this, "Please select a valid folder.", "No folder",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        var folder      = Path.of(folderText);
        var scannerName = (String) scannerBox.getSelectedItem();

        settings.setLastFolder(folderText);
        settings.setLastScanner(scannerName);
        settingsStore.save(settings);

        cards.show(cardPanel, "progress");
        showScanningButtons();
        statusLabel.setText("Starting…");
        progressBar.setValue(0);
        progressBar.setMaximum(1);
        progressBar.setString("");

        worker = new SwingWorker<>() {
            @Override
            protected List<Snapshot> doInBackground() throws Exception {
                var config  = new ScannerConfigReader().read(scannerName);
                return new ConfigurableScanner(config).scan(folder, (done, total, folderName) ->
                    publish(done + "|" + total + "|" + folderName));
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                var last   = chunks.get(chunks.size() - 1);
                var parts  = last.split("\\|");
                int done   = Integer.parseInt(parts[0]);
                int total  = Integer.parseInt(parts[1]);
                progressBar.setMaximum(total);
                progressBar.setValue(done);
                progressBar.setString(done + " / " + total);
                statusLabel.setText("Scanning " + done + " of " + total + ": " + parts[2]);
            }

            @Override
            protected void done() {
                progressBar.setValue(progressBar.getMaximum());
                progressBar.setString("");
                showCloseButton();

                if (isCancelled()) {
                    statusLabel.setText("Cancelled.");
                    return;
                }
                try {
                    result = get();
                    if (result.isEmpty()) {
                        statusLabel.setText("No sessions found.");
                    } else {
                        var first = MONTH_FMT.format(result.get(0).instant());
                        var last2 = MONTH_FMT.format(result.get(result.size() - 1).instant());
                        var range = first.equals(last2) ? first : first + " – " + last2;
                        statusLabel.setText("Found " + result.size() + " session"
                            + (result.size() == 1 ? "" : "s") + "  (" + range + ")");
                    }
                } catch (java.util.concurrent.CancellationException ex) {
                    statusLabel.setText("Cancelled.");
                } catch (Exception ex) {
                    var cause = ex.getCause() != null ? ex.getCause() : ex;
                    statusLabel.setText("Error: " + cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void browse(JFrame owner) {
        var chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select data folder");
        var current = folderField.getText();
        if (!current.isBlank()) chooser.setCurrentDirectory(Path.of(current).toFile());
        if (chooser.showOpenDialog(owner) == JFileChooser.APPROVE_OPTION) {
            folderField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    public List<Snapshot> open() {
        setVisible(true);
        return result;
    }
}
