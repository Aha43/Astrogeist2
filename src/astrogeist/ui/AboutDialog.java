package astrogeist.ui;

import astrogeist.app.AppInfo;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public final class AboutDialog extends JDialog {

    public AboutDialog(JFrame owner) {
        super(owner, "About " + AppInfo.NAME, true);
        setResizable(false);

        var logoLabel = new JLabel();
        try {
            var url = getClass().getResource("/resources/logo.png");
            if (url != null) {
                BufferedImage img = ImageIO.read(url);
                Image scaled = img.getScaledInstance(200, 150, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(scaled));
            }
        } catch (IOException ignored) {}
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        var nameLabel    = new JLabel(AppInfo.NAME, SwingConstants.CENTER);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 16f));
        var versionLabel = new JLabel("Version " + AppInfo.version(), SwingConstants.CENTER);
        var descLabel    = new JLabel("Desktop app for astrophotographers", SwingConstants.CENTER);

        var closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(closeBtn);

        var btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.add(closeBtn);

        var content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        for (var c : new JComponent[]{logoLabel, nameLabel, versionLabel, descLabel}) {
            c.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(c);
            content.add(Box.createVerticalStrut(6));
        }
        content.add(Box.createVerticalStrut(8));
        content.add(btnPanel);

        getContentPane().add(content);
        pack();
        setLocationRelativeTo(owner);
    }

    public static void show(JFrame owner) {
        new AboutDialog(owner).setVisible(true);
    }
}
