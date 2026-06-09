package astrogeist.ui;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ScanConfigDialog extends JDialog {

    public record Result(Path folder, String scannerName) {}

    private static final String[] BUNDLED_SCANNERS = { "SharpCap", "Seestar" };

    private final JTextField folderField = new JTextField(40);
    private final JComboBox<String> scannerBox = new JComboBox<>(BUNDLED_SCANNERS);
    private Result result;

    public ScanConfigDialog(JFrame owner, String lastFolder, String lastScanner) {
        super(owner, "Scan folder", true);

        folderField.setText(lastFolder);
        folderField.setEditable(false);
        for (int i = 0; i < BUNDLED_SCANNERS.length; i++) {
            if (BUNDLED_SCANNERS[i].equals(lastScanner)) { scannerBox.setSelectedIndex(i); break; }
        }

        var browseBtn = new JButton("Browse…");
        browseBtn.addActionListener(e -> browse(owner));

        var folderRow = new JPanel(new BorderLayout(4, 0));
        folderRow.add(folderField, BorderLayout.CENTER);
        folderRow.add(browseBtn, BorderLayout.EAST);

        var form = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Folder:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        form.add(folderRow, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        form.add(new JLabel("Scanner:"), gbc);
        gbc.gridx = 1; form.add(scannerBox, gbc);

        var okBtn = new JButton("Scan");
        okBtn.addActionListener(e -> confirm());
        var cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        var buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelBtn);
        buttons.add(okBtn);

        getRootPane().setDefaultButton(okBtn);
        getContentPane().setLayout(new BorderLayout(8, 8));
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));

        pack();
        setMinimumSize(new Dimension(500, getHeight()));
        setLocationRelativeTo(owner);
    }

    public Result open() {
        setVisible(true);
        return result;
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

    private void confirm() {
        var text = folderField.getText().trim();
        if (text.isBlank() || !Files.isDirectory(Path.of(text))) {
            JOptionPane.showMessageDialog(this, "Please select a valid folder.", "No folder", JOptionPane.WARNING_MESSAGE);
            return;
        }
        result = new Result(Path.of(text), (String) scannerBox.getSelectedItem());
        dispose();
    }
}
