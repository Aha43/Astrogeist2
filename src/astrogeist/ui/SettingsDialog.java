package astrogeist.ui;

import astrogeist.persist.AppSettings;
import astrogeist.persist.XmlSettingsStore;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.function.Consumer;

public final class SettingsDialog extends JDialog {

    public SettingsDialog(JFrame owner, AppSettings settings, XmlSettingsStore store,
                          Consumer<Boolean> onDenseChange) {
        super(owner, "Settings", true);
        setResizable(false);

        // ── Appearance ────────────────────────────────────────────────────────
        var denseBox = new JCheckBox("Dense mode");
        denseBox.setSelected(settings.isDenseMode());
        denseBox.addActionListener(e -> {
            settings.setDenseMode(denseBox.isSelected());
            onDenseChange.accept(denseBox.isSelected());
            store.save(settings);
        });

        var appearancePanel = new JPanel(new GridBagLayout());
        appearancePanel.setBorder(BorderFactory.createTitledBorder("Appearance"));
        var gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;
        appearancePanel.add(denseBox, gbc);

        // ── Scanning ──────────────────────────────────────────────────────────
        var dataRootField = new JTextField(28);
        dataRootField.setText(settings.getDataRootFolder());
        dataRootField.setEditable(false);

        var browseBtn = new JButton("Browse…");
        browseBtn.addActionListener(e -> {
            var chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Select default data folder");
            var current = dataRootField.getText();
            if (!current.isBlank()) chooser.setCurrentDirectory(Path.of(current).toFile());
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                var path = chooser.getSelectedFile().getAbsolutePath();
                dataRootField.setText(path);
                settings.setDataRootFolder(path);
                store.save(settings);
            }
        });

        var clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> {
            dataRootField.setText("");
            settings.setDataRootFolder("");
            store.save(settings);
        });

        var folderRow = new JPanel(new BorderLayout(4, 0));
        folderRow.add(dataRootField, BorderLayout.CENTER);
        var folderBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        folderBtns.add(browseBtn);
        folderBtns.add(clearBtn);
        folderRow.add(folderBtns, BorderLayout.EAST);

        var scanningPanel = new JPanel(new GridBagLayout());
        scanningPanel.setBorder(BorderFactory.createTitledBorder("Scanning"));
        var gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(4, 8, 4, 8);
        gbc2.anchor = GridBagConstraints.WEST;
        gbc2.gridx = 0; gbc2.gridy = 0;
        scanningPanel.add(new JLabel("Default data folder:"), gbc2);
        gbc2.gridx = 1; gbc2.fill = GridBagConstraints.HORIZONTAL; gbc2.weightx = 1;
        scanningPanel.add(folderRow, gbc2);

        // ── Layout ────────────────────────────────────────────────────────────
        var closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(closeBtn);
        var btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(closeBtn);

        var sections = new JPanel();
        sections.setLayout(new BoxLayout(sections, BoxLayout.Y_AXIS));
        sections.add(appearancePanel);
        sections.add(Box.createVerticalStrut(6));
        sections.add(scanningPanel);

        var content = (JPanel) getContentPane();
        content.setLayout(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        content.add(sections,  BorderLayout.CENTER);
        content.add(btnPanel,  BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(420, getHeight()));
        setLocationRelativeTo(owner);
    }

    public static void show(JFrame owner, AppSettings settings, XmlSettingsStore store,
                            Consumer<Boolean> onDenseChange) {
        new SettingsDialog(owner, settings, store, onDenseChange).setVisible(true);
    }
}
