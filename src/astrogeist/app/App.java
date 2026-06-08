package astrogeist.app;

import astrogeist.persist.XmlUserDataStore;
import astrogeist.service.SnapshotSelectionService;
import astrogeist.ui.*;

import javax.swing.*;

public final class App {

    private App() {}

    public static void launch() {
        var selectionService = new SnapshotSelectionService();
        var userDataStore    = new XmlUserDataStore(Resources.resolve("userdata"));

        var timelinePanel    = new TimelineTablePanel(selectionService);
        var metadataPanel    = new MetadataPanel(selectionService);
        var annotationPanel  = new AnnotationPanel(userDataStore, selectionService);

        var rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, metadataPanel, annotationPanel);
        rightSplit.setResizeWeight(0.6);

        var mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, timelinePanel, rightSplit);
        mainSplit.setResizeWeight(0.7);
        mainSplit.setDividerLocation(820);

        var frame = new JFrame(AppInfo.NAME + " " + AppInfo.version());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setJMenuBar(new MenuBarFactory(timelinePanel).build(frame));
        frame.getContentPane().add(mainSplit);
        frame.setSize(1200, 750);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
