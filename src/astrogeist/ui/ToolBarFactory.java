package astrogeist.ui;

import astrogeist.persist.AppSettings;
import astrogeist.persist.XmlScanTargetsStore;
import astrogeist.persist.XmlSettingsStore;
import astrogeist.scanner.ScannerConfigReader;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public final class ToolBarFactory {

    private record LabeledButton(JButton button, String label) {}

    private final TimelineTablePanel timelinePanel;
    private final AppSettings settings;
    private final XmlSettingsStore settingsStore;
    private final XmlScanTargetsStore scanTargetsStore;
    private Consumer<Boolean> onDenseChange = dense -> {};
    private final List<LabeledButton> labeledButtons = new ArrayList<>();

    public ToolBarFactory(TimelineTablePanel timelinePanel, AppSettings settings,
                          XmlSettingsStore settingsStore, XmlScanTargetsStore scanTargetsStore) {
        this.timelinePanel    = timelinePanel;
        this.settings         = settings;
        this.settingsStore    = settingsStore;
        this.scanTargetsStore = scanTargetsStore;
    }

    public void setOnDenseChange(Consumer<Boolean> c) { this.onDenseChange = c; }

    public void setDense(boolean dense) {
        for (var lb : labeledButtons) lb.button().setText(dense ? null : lb.label());
    }

    public JToolBar build(JFrame owner) {
        var bar = new JToolBar();
        bar.setFloatable(false);

        var scanBtn = makeButton("telescope.svg", "Scan", "Scan (⌘O)");
        scanBtn.addActionListener(e -> scan(owner));
        bar.add(scanBtn);

        var settingsBtn = makeButton("settings.svg", "Settings", "Settings (⌘,)");
        settingsBtn.addActionListener(e -> SettingsDialog.show(owner, settings, settingsStore, onDenseChange));
        bar.add(settingsBtn);

        bar.add(Box.createHorizontalGlue());

        var exitBtn = makeButton("door-exit.svg", "Exit", "Exit (⌘Q)");
        exitBtn.addActionListener(e -> System.exit(0));
        bar.add(exitBtn);

        return bar;
    }

    private JButton makeButton(String iconFile, String label, String tooltip) {
        var url  = getClass().getResource("/icons/" + iconFile);
        var icon = url != null ? new FlatSVGIcon(url).derive(16, 16) : null;
        var btn  = new JButton(label, icon);
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setToolTipText(tooltip);
        btn.setFocusable(false);
        labeledButtons.add(new LabeledButton(btn, label));
        return btn;
    }

    private void scan(JFrame owner) {
        var snapshots = new ScanDialog(owner, scanTargetsStore, settings, new ScannerConfigReader()).open();
        if (snapshots != null && !snapshots.isEmpty()) timelinePanel.setSnapshots(snapshots);
    }
}
