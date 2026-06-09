package astrogeist.ui;

import astrogeist.persist.AppSettings;
import astrogeist.persist.XmlScanTargetsStore;
import astrogeist.persist.XmlSettingsStore;
import astrogeist.scanner.ScannerConfigReader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public final class MenuBarFactory {

    private final TimelineTablePanel timelinePanel;
    private final AppSettings settings;
    private final XmlSettingsStore settingsStore;
    private final XmlScanTargetsStore scanTargetsStore;
    private Consumer<Boolean> onDenseChange = dense -> {};

    public MenuBarFactory(TimelineTablePanel timelinePanel, AppSettings settings,
                          XmlSettingsStore settingsStore, XmlScanTargetsStore scanTargetsStore) {
        this.timelinePanel    = timelinePanel;
        this.settings         = settings;
        this.settingsStore    = settingsStore;
        this.scanTargetsStore = scanTargetsStore;
    }

    public void setOnDenseChange(Consumer<Boolean> c) { this.onDenseChange = c; }

    public JMenuBar build(JFrame owner) {
        var bar = new JMenuBar();
        bar.add(buildFileMenu(owner));
        bar.add(buildHelpMenu(owner));
        return bar;
    }

    private JMenu buildFileMenu(JFrame owner) {
        var menu = new JMenu("File");

        var scanItem = new JMenuItem("Scan…");
        scanItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        scanItem.addActionListener(e -> scan(owner));
        menu.add(scanItem);

        var settingsItem = new JMenuItem("Settings…");
        settingsItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_COMMA, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        settingsItem.addActionListener(e -> SettingsDialog.show(owner, settings, settingsStore, onDenseChange));
        menu.add(settingsItem);

        menu.addSeparator();

        var quitItem = new JMenuItem("Quit");
        quitItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        quitItem.addActionListener(e -> System.exit(0));
        menu.add(quitItem);

        return menu;
    }

    private JMenu buildHelpMenu(JFrame owner) {
        var menu = new JMenu("Help");
        var aboutItem = new JMenuItem("About " + astrogeist.app.AppInfo.NAME + "…");
        aboutItem.addActionListener(e -> AboutDialog.show(owner));
        menu.add(aboutItem);
        return menu;
    }

    private void scan(JFrame owner) {
        var snapshots = new ScanDialog(owner, scanTargetsStore, settings, new ScannerConfigReader()).open();
        if (snapshots != null && !snapshots.isEmpty()) timelinePanel.setSnapshots(snapshots);
    }
}
