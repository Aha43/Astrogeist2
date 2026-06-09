package astrogeist.ui;

import astrogeist.persist.AppSettings;
import astrogeist.persist.XmlSettingsStore;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public final class SettingsDialog extends JDialog {

    public SettingsDialog(JFrame owner, AppSettings settings, XmlSettingsStore store,
                          Consumer<Boolean> onDenseChange) {
        super(owner, "Settings", true);
        setResizable(false);

        var denseBox = new JCheckBox("Dense mode");
        denseBox.setSelected(settings.isDenseMode());
        denseBox.addActionListener(e -> {
            boolean dense = denseBox.isSelected();
            settings.setDenseMode(dense);
            onDenseChange.accept(dense);
            store.save(settings);
        });

        var appearancePanel = new JPanel(new GridBagLayout());
        appearancePanel.setBorder(BorderFactory.createTitledBorder("Appearance"));
        var gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;
        appearancePanel.add(denseBox, gbc);

        var closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(closeBtn);
        var btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(closeBtn);

        var content = (JPanel) getContentPane();
        content.setLayout(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        content.add(appearancePanel, BorderLayout.CENTER);
        content.add(btnPanel, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(280, getHeight()));
        setLocationRelativeTo(owner);
    }

    public static void show(JFrame owner, AppSettings settings, XmlSettingsStore store,
                            Consumer<Boolean> onDenseChange) {
        new SettingsDialog(owner, settings, store, onDenseChange).setVisible(true);
    }
}
