package astrogeist.app;

import astrogeist.service.SnapshotSelectionService;
import astrogeist.ui.MenuBarFactory;
import astrogeist.ui.MetadataPanel;
import astrogeist.ui.TimelineTablePanel;

import javax.swing.*;

public final class App {

    private App() {}

    public static void launch() {
        var selectionService  = new SnapshotSelectionService();
        var timelinePanel     = new TimelineTablePanel(selectionService);
        var metadataPanel     = new MetadataPanel(selectionService);

        var split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, timelinePanel, metadataPanel);
        split.setResizeWeight(0.7);
        split.setDividerLocation(850);

        var frame = new JFrame(AppInfo.NAME + " " + AppInfo.version());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setJMenuBar(new MenuBarFactory(timelinePanel).build(frame));
        frame.getContentPane().add(split);
        frame.setSize(1200, 750);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
