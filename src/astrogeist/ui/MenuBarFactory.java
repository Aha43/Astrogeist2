package astrogeist.ui;

import astrogeist.model.DefaultTimeline;
import astrogeist.scanner.ConfigurableScanner;
import astrogeist.scanner.ScannerConfigReader;

import javax.swing.*;
import java.awt.*;

public final class MenuBarFactory {

    private final TimelineTablePanel timelinePanel;

    public MenuBarFactory(TimelineTablePanel timelinePanel) {
        this.timelinePanel = timelinePanel;
    }

    public JMenuBar build(JFrame owner) {
        var bar = new JMenuBar();
        bar.add(buildFileMenu(owner));
        return bar;
    }

    private JMenu buildFileMenu(JFrame owner) {
        var menu = new JMenu("File");

        var scanItem = new JMenuItem("Scan folder (SharpCap)…");
        scanItem.addActionListener(e -> scanFolder(owner));
        menu.add(scanItem);

        menu.addSeparator();

        var quitItem = new JMenuItem("Quit");
        quitItem.addActionListener(e -> System.exit(0));
        menu.add(quitItem);

        return menu;
    }

    private void scanFolder(JFrame owner) {
        var chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select SharpCap data folder");
        if (chooser.showOpenDialog(owner) != JFileChooser.APPROVE_OPTION) return;

        var root = chooser.getSelectedFile().toPath();
        owner.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<java.util.List<astrogeist.model.Snapshot>, Void> worker = new SwingWorker<>() {
            @Override
            protected java.util.List<astrogeist.model.Snapshot> doInBackground() throws Exception {
                var config = new ScannerConfigReader().readBuiltin("SharpCap");
                return new ConfigurableScanner(config).scan(root);
            }

            @Override
            protected void done() {
                owner.setCursor(Cursor.getDefaultCursor());
                try {
                    var snapshots = get();
                    timelinePanel.setSnapshots(snapshots);
                    if (snapshots.isEmpty()) {
                        JOptionPane.showMessageDialog(owner,
                            "No SharpCap sessions found in the selected folder.",
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
