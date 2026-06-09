package astrogeist.app;

import astrogeist.persist.AppSettings;
import astrogeist.persist.XmlSettingsStore;
import astrogeist.persist.XmlUserDataStore;
import astrogeist.service.SnapshotSelectionService;
import astrogeist.ui.*;

import javax.swing.*;

public final class App {

    private App() {}

    public static void launch() {
        var settingsStore = new XmlSettingsStore(Resources.resolve("settings.xml"));
        var settings      = settingsStore.load();
        var userDataStore = new XmlUserDataStore(Resources.resolve("userdata"));

        var selectionService = new SnapshotSelectionService();
        var timelinePanel    = new TimelineTablePanel(selectionService);
        var metadataPanel    = new MetadataPanel(selectionService);
        var annotationPanel  = new AnnotationPanel(userDataStore, selectionService);

        timelinePanel.setColumnWidths(settings.getColumnWidths());
        timelinePanel.onColumnWidthChange(widths -> {
            settings.setColumnWidths(widths);
            settingsStore.save(settings);
        });

        var rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, metadataPanel, annotationPanel);
        rightSplit.setResizeWeight(0.6);

        var mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, timelinePanel, rightSplit);
        mainSplit.setResizeWeight(0.7);
        mainSplit.setDividerLocation(820);

        var frame = new JFrame(AppInfo.NAME + " " + AppInfo.version());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                settingsStore.save(settings);
                System.exit(0);
            }
        });
        frame.setJMenuBar(new MenuBarFactory(timelinePanel, settings).build(frame));
        frame.getContentPane().add(mainSplit);
        frame.setSize(1200, 750);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
