package astrogeist.ui;

import astrogeist.model.Snapshot;
import astrogeist.persist.AppSettings;
import astrogeist.persist.ScanTarget;
import astrogeist.persist.XmlScanTargetsStore;
import astrogeist.scanner.ConfigurableScanner;
import astrogeist.scanner.ScannerConfigReader;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class ScanDialog extends JDialog {

    private static final DateTimeFormatter MONTH_FMT =
        DateTimeFormatter.ofPattern("MMM yyyy").withZone(ZoneId.systemDefault());

    // ── targets tab ───────────────────────────────────────────────────────────

    private final XmlScanTargetsStore targetsStore;
    private final AppSettings settings;
    private final List<ScanTarget> targets;
    private final TargetsModel targetsModel;

    // ── progress tab ─────────────────────────────────────────────────────────

    private final List<ProgressRow> progressRows = new ArrayList<>();
    private final ProgressModel progressModel = new ProgressModel();

    // ── result ────────────────────────────────────────────────────────────────

    private List<Snapshot> result;

    // ── shared controls ───────────────────────────────────────────────────────

    private final JTabbedPane tabs = new JTabbedPane();
    private final JButton scanBtn   = new JButton("Scan selected");
    private final JButton cancelBtn = new JButton("Cancel");
    private final JButton closeBtn  = new JButton("Close");
    private final JPanel  buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    public ScanDialog(JFrame owner, XmlScanTargetsStore targetsStore, AppSettings settings,
                      ScannerConfigReader scannerReader) {
        super(owner, "Scan", true);
        this.targetsStore = targetsStore;
        this.settings     = settings;
        this.targets      = targetsStore.load();
        this.targetsModel = new TargetsModel(targets);

        cancelBtn.addActionListener(e -> dispose());
        closeBtn.addActionListener(e  -> dispose());
        scanBtn.addActionListener(e   -> startScan(owner));

        tabs.addTab("Targets",  buildTargetsTab(owner, scannerReader));
        tabs.addTab("Progress", buildProgressTab());

        showTargetsButtons();

        var content = (JPanel) getContentPane();
        content.setLayout(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        content.add(tabs,      BorderLayout.CENTER);
        content.add(buttonBar, BorderLayout.SOUTH);

        setSize(640, 420);
        setMinimumSize(new Dimension(540, 360));
        setLocationRelativeTo(owner);
    }

    // ── targets tab ───────────────────────────────────────────────────────────

    private JPanel buildTargetsTab(JFrame owner, ScannerConfigReader scannerReader) {
        var table = new JTable(targetsModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);   // Use
        table.getColumnModel().getColumn(1).setPreferredWidth(100);  // Scanner
        table.getColumnModel().getColumn(2).setPreferredWidth(340);  // Folder

        // Grey out rows where folder no longer exists
        var folderRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                var c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                var folder = targets.get(row).folder();
                c.setForeground(Files.isDirectory(Path.of(folder))
                    ? t.getForeground() : Color.GRAY);
                return c;
            }
        };
        table.getColumnModel().getColumn(1).setCellRenderer(folderRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(folderRenderer);

        var addBtn = new JButton("Add…");
        addBtn.addActionListener(e -> addTarget(owner, scannerReader, table));

        var removeBtn = new JButton("Remove");
        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                targets.remove(row);
                targetsStore.save(targets);
                targetsModel.fireTableDataChanged();
            }
        });

        var toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        toolbar.add(addBtn);
        toolbar.add(removeBtn);

        var panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(toolbar, BorderLayout.SOUTH);
        return panel;
    }

    private void addTarget(JFrame owner, ScannerConfigReader scannerReader, JTable table) {
        var available = scannerReader.listAvailable();
        var scannerBox = new JComboBox<>(available.toArray(new String[0]));
        var folderField = new JTextField(30);
        folderField.setEditable(false);

        var browseBtn = new JButton("Browse…");
        browseBtn.addActionListener(e -> {
            var chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            // prefer: already-typed folder > data root setting > home
            var start = !folderField.getText().isBlank() ? folderField.getText()
                      : !settings.getDataRootFolder().isBlank() ? settings.getDataRootFolder()
                      : System.getProperty("user.home");
            chooser.setCurrentDirectory(Path.of(start).toFile());
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
                folderField.setText(chooser.getSelectedFile().getAbsolutePath());
        });

        var folderRow = new JPanel(new BorderLayout(4, 0));
        folderRow.add(folderField, BorderLayout.CENTER);
        folderRow.add(browseBtn,   BorderLayout.EAST);

        var form = new JPanel(new GridBagLayout());
        var gbc  = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4); gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Scanner:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        form.add(scannerBox, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        form.add(new JLabel("Folder:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        form.add(folderRow, gbc);

        int rc = JOptionPane.showConfirmDialog(this, form, "Add scan target",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (rc != JOptionPane.OK_OPTION) return;

        var folder = folderField.getText().trim();
        if (folder.isBlank()) return;

        var t = new ScanTarget((String) scannerBox.getSelectedItem(), folder);
        targets.add(t);
        targetsStore.save(targets);
        targetsModel.fireTableDataChanged();
    }

    // ── progress tab ─────────────────────────────────────────────────────────

    private JPanel buildProgressTab() {
        var table = new JTable(progressModel);
        table.setRowHeight(24);
        table.setEnabled(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(90);   // Scanner
        table.getColumnModel().getColumn(1).setPreferredWidth(220);  // Folder
        table.getColumnModel().getColumn(2).setPreferredWidth(160);  // Progress
        table.getColumnModel().getColumn(3).setPreferredWidth(160);  // Status

        table.getColumnModel().getColumn(2).setCellRenderer(new ProgressBarRenderer());

        var panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ── scan execution ────────────────────────────────────────────────────────

    private void startScan(JFrame owner) {
        var selected = getCheckedTargets();
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No targets selected.", "Nothing to scan",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        progressRows.clear();
        for (var t : selected)
            progressRows.add(new ProgressRow(t.scannerName(),
                Path.of(t.folder()).getFileName().toString(), t.folder()));
        progressModel.fireTableDataChanged();

        tabs.setSelectedIndex(1);
        showScanningButtons();

        SwingWorker<List<Snapshot>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Snapshot> doInBackground() {
                var accumulated = new ArrayList<Snapshot>();
                for (int i = 0; i < progressRows.size(); i++) {
                    if (Thread.interrupted()) break;
                    var row = progressRows.get(i);
                    final int idx = i;
                    row.status = "Running…";
                    SwingUtilities.invokeLater(() -> progressModel.fireTableRowsUpdated(idx, idx));
                    try {
                        var config   = new ScannerConfigReader().read(row.scanner);
                        var snapshots = new ConfigurableScanner(config)
                            .scan(Path.of(row.folder), (done, total, name) -> {
                                row.progress = done;
                                row.total    = total;
                                SwingUtilities.invokeLater(() -> progressModel.fireTableRowsUpdated(idx, idx));
                            });
                        accumulated.addAll(snapshots);
                        row.progress = row.total;
                        row.status = "Done (" + snapshots.size() + " session"
                            + (snapshots.size() == 1 ? "" : "s") + ")";
                    } catch (Exception ex) {
                        row.status = "Error: " + ex.getMessage();
                    }
                    final int fi = i;
                    SwingUtilities.invokeLater(() -> progressModel.fireTableRowsUpdated(fi, fi));
                }
                accumulated.sort((a, b) -> a.instant().compareTo(b.instant()));
                return accumulated;
            }

            @Override
            protected void done() {
                showCloseButton();
                if (isCancelled()) {
                    for (var r : progressRows) if ("Running…".equals(r.status)) r.status = "Cancelled";
                    progressModel.fireTableDataChanged();
                    return;
                }
                try { result = get(); } catch (Exception ignored) {}
            }
        };

        cancelBtn.addActionListener(e -> worker.cancel(true));
        worker.execute();
    }

    private List<ScanTarget> getCheckedTargets() {
        var out = new ArrayList<ScanTarget>();
        for (int i = 0; i < targets.size(); i++)
            if ((Boolean) targetsModel.getValueAt(i, 0)) out.add(targets.get(i));
        return out;
    }

    // ── button bar ────────────────────────────────────────────────────────────

    private void showTargetsButtons() {
        buttonBar.removeAll();
        buttonBar.add(cancelBtn);
        buttonBar.add(scanBtn);
        buttonBar.revalidate(); buttonBar.repaint();
    }

    private void showScanningButtons() {
        buttonBar.removeAll();
        buttonBar.add(cancelBtn);
        buttonBar.revalidate(); buttonBar.repaint();
    }

    private void showCloseButton() {
        buttonBar.removeAll();
        buttonBar.add(closeBtn);
        buttonBar.revalidate(); buttonBar.repaint();
    }

    public List<Snapshot> open() {
        setVisible(true);
        return result;
    }

    // ── targets table model ───────────────────────────────────────────────────

    private static final class TargetsModel extends AbstractTableModel {
        private final List<ScanTarget> targets;
        private final boolean[] checked;

        TargetsModel(List<ScanTarget> targets) {
            this.targets = targets;
            this.checked = new boolean[Math.max(targets.size(), 64)];
            java.util.Arrays.fill(checked, 0, targets.size(), true);
        }

        @Override public int getRowCount()    { return targets.size(); }
        @Override public int getColumnCount() { return 3; }
        @Override public String getColumnName(int col) {
            return switch (col) { case 0 -> "Use"; case 1 -> "Scanner"; default -> "Folder"; };
        }
        @Override public Class<?> getColumnClass(int col) {
            return col == 0 ? Boolean.class : String.class;
        }
        @Override public boolean isCellEditable(int row, int col) { return col == 0; }

        @Override public Object getValueAt(int row, int col) {
            return switch (col) {
                case 0 -> row < checked.length ? checked[row] : false;
                case 1 -> targets.get(row).scannerName();
                default -> targets.get(row).folder();
            };
        }
        @Override public void setValueAt(Object val, int row, int col) {
            if (col == 0 && row < checked.length) { checked[row] = (Boolean) val; fireTableCellUpdated(row, col); }
        }
    }

    // ── progress table model ──────────────────────────────────────────────────

    private static final class ProgressRow {
        final String scanner, folderName, folder;
        volatile int progress, total;
        volatile String status = "Waiting";
        ProgressRow(String scanner, String folderName, String folder) {
            this.scanner = scanner; this.folderName = folderName; this.folder = folder;
        }
    }

    private final class ProgressModel extends AbstractTableModel {
        @Override public int getRowCount()    { return progressRows.size(); }
        @Override public int getColumnCount() { return 4; }
        @Override public String getColumnName(int col) {
            return switch (col) { case 0 -> "Scanner"; case 1 -> "Folder"; case 2 -> "Progress"; default -> "Status"; };
        }
        @Override public Object getValueAt(int row, int col) {
            var r = progressRows.get(row);
            return switch (col) {
                case 0 -> r.scanner;
                case 1 -> r.folderName;
                case 2 -> r;   // renderer reads progress/total directly
                default -> r.status;
            };
        }
    }

    // ── progress bar cell renderer ────────────────────────────────────────────

    private static final class ProgressBarRenderer implements TableCellRenderer {
        private final JProgressBar bar = new JProgressBar(0, 100);
        { bar.setStringPainted(true); }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof ProgressRow r) {
                int pct = r.total > 0 ? (r.progress * 100 / r.total) : 0;
                bar.setValue(pct);
                bar.setString(r.total > 0 ? r.progress + " / " + r.total : "");
            }
            return bar;
        }
    }
}
