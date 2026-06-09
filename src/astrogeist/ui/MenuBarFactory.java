package astrogeist.ui;

import astrogeist.persist.AppSettings;
import astrogeist.persist.XmlSettingsStore;
import astrogeist.scanner.ConfigurableScanner;
import astrogeist.scanner.ScannerConfigReader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public final class MenuBarFactory {

    private final TimelineTablePanel timelinePanel;
    private final AppSettings settings;
    private final XmlSettingsStore settingsStore;
    private Consumer<Boolean> onDenseChange = dense -> {};

    public MenuBarFactory(TimelineTablePanel timelinePanel, AppSettings settings,
                          XmlSettingsStore settingsStore) {
        this.timelinePanel = timelinePanel;
        this.settings      = settings;
        this.settingsStore = settingsStore;
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
        var dialog = new ScanConfigDialog(owner, settings.getLastFolder(), settings.getLastScanner());
        var result = dialog.open();
        if (result == null) return;

        settings.setLastFolder(result.folder().toString());
        settings.setLastScanner(result.scannerName());

        owner.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<java.util.List<astrogeist.model.Snapshot>, Void> worker = new SwingWorker<>() {
            @Override
            protected java.util.List<astrogeist.model.Snapshot> doInBackground() throws Exception {
                var config = new ScannerConfigReader().readBuiltin(result.scannerName());
                return new ConfigurableScanner(config).scan(result.folder());
            }

            @Override
            protected void done() {
                owner.setCursor(Cursor.getDefaultCursor());
                try {
                    var snapshots = get();
                    timelinePanel.setSnapshots(snapshots);
                    if (snapshots.isEmpty()) {
                        JOptionPane.showMessageDialog(owner,
                            "No " + result.scannerName() + " sessions found in the selected folder.",
                            "No sessions", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(owner,
                        "Scan failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
