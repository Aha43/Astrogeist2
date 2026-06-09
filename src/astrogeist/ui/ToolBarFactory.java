package astrogeist.ui;

import astrogeist.persist.AppSettings;
import astrogeist.persist.XmlSettingsStore;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public final class ToolBarFactory {

    private final TimelineTablePanel timelinePanel;
    private final AppSettings settings;
    private final XmlSettingsStore settingsStore;

    public ToolBarFactory(TimelineTablePanel timelinePanel, AppSettings settings,
                          XmlSettingsStore settingsStore) {
        this.timelinePanel = timelinePanel;
        this.settings      = settings;
        this.settingsStore = settingsStore;
    }

    public JToolBar build(JFrame owner) {
        var bar = new JToolBar();
        bar.setFloatable(false);

        var scanBtn = makeButton("telescope.svg", "Scan (⌘O)");
        scanBtn.addActionListener(e -> scan(owner));
        bar.add(scanBtn);

        var settingsBtn = makeButton("settings.svg", "Settings (⌘,)");
        settingsBtn.addActionListener(e -> SettingsDialog.show(owner, settings, settingsStore, timelinePanel));
        bar.add(settingsBtn);

        bar.add(Box.createHorizontalGlue());

        var exitBtn = makeButton("door-exit.svg", "Exit");
        exitBtn.addActionListener(e -> System.exit(0));
        bar.add(exitBtn);

        return bar;
    }

    private JButton makeButton(String iconFile, String tooltip) {
        var url  = getClass().getResource("/icons/" + iconFile);
        var icon = url != null ? new FlatSVGIcon(url).derive(16, 16) : null;
        var btn  = new JButton(icon);
        btn.setToolTipText(tooltip);
        btn.setFocusable(false);
        return btn;
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
                var config = new astrogeist.scanner.ScannerConfigReader().readBuiltin(result.scannerName());
                return new astrogeist.scanner.ConfigurableScanner(config).scan(result.folder());
            }

            @Override
            protected void done() {
                owner.setCursor(Cursor.getDefaultCursor());
                try {
                    var snapshots = get();
                    timelinePanel.setSnapshots(snapshots);
                    if (snapshots.isEmpty()) {
                        JOptionPane.showMessageDialog(owner,
                            "No " + result.scannerName() + " sessions found.",
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
