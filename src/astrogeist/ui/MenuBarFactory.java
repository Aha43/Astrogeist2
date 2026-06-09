package astrogeist.ui;

import astrogeist.persist.AppSettings;
import astrogeist.scanner.ConfigurableScanner;
import astrogeist.scanner.ScannerConfigReader;

import javax.swing.*;
import java.awt.*;

public final class MenuBarFactory {

    private final TimelineTablePanel timelinePanel;
    private final AppSettings settings;

    public MenuBarFactory(TimelineTablePanel timelinePanel, AppSettings settings) {
        this.timelinePanel = timelinePanel;
        this.settings      = settings;
    }

    public JMenuBar build(JFrame owner) {
        var bar = new JMenuBar();
        bar.add(buildFileMenu(owner));
        return bar;
    }

    private JMenu buildFileMenu(JFrame owner) {
        var menu = new JMenu("File");

        var scanItem = new JMenuItem("Scan…");
        scanItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_O, java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        scanItem.addActionListener(e -> scan(owner));
        menu.add(scanItem);

        menu.addSeparator();

        var quitItem = new JMenuItem("Quit");
        quitItem.addActionListener(e -> System.exit(0));
        menu.add(quitItem);

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
