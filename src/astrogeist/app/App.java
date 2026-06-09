package astrogeist.app;

import astrogeist.persist.AppSettings;
import astrogeist.persist.XmlScanTargetsStore;
import astrogeist.persist.XmlSettingsStore;
import astrogeist.persist.XmlUserDataStore;
import astrogeist.scanner.ConfigurableScanner;
import astrogeist.scanner.ScannerConfigReader;
import astrogeist.service.SnapshotSelectionService;
import astrogeist.ui.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.Map;
import java.util.function.Consumer;

public final class App {

    private App() {}

    public static void launch(boolean demoMode) {
        var settingsStore     = new XmlSettingsStore(Resources.resolve("settings.xml"));
        var settings          = settingsStore.load();
        var userDataStore     = new XmlUserDataStore(Resources.resolve("userdata"));
        var scanTargetsStore  = new XmlScanTargetsStore(Resources.resolve("scan-targets.xml"));

        var selectionService = new SnapshotSelectionService();
        var timelinePanel    = new TimelineTablePanel(selectionService);
        var metadataPanel    = new MetadataPanel(selectionService);
        var annotationPanel  = new AnnotationPanel(userDataStore, selectionService);

        timelinePanel.setColumnWidths(settings.getColumnWidths());
        timelinePanel.onColumnWidthChange(widths -> {
            settings.setColumnWidths(widths);
            settingsStore.save(settings);
        });

        var toolbarFactory = new ToolBarFactory(timelinePanel, settings, settingsStore, scanTargetsStore);
        var menuBarFactory = new MenuBarFactory(timelinePanel, settings, settingsStore, scanTargetsStore);

        Consumer<Boolean> applyDense = dense -> {
            timelinePanel.setDense(dense);
            toolbarFactory.setDense(dense);
        };
        toolbarFactory.setOnDenseChange(applyDense);
        menuBarFactory.setOnDenseChange(applyDense);

        applyDense.accept(settings.isDenseMode());

        var rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, metadataPanel, annotationPanel);
        rightSplit.setResizeWeight(0.6);

        var mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, timelinePanel, rightSplit);
        mainSplit.setResizeWeight(0.7);
        mainSplit.setDividerLocation(820);

        var title = AppInfo.NAME + " " + AppInfo.version();
        if (demoMode) title += "  [demo]";
        var frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                settingsStore.save(settings);
                System.exit(0);
            }
        });
        frame.setJMenuBar(menuBarFactory.build(frame));
        frame.add(toolbarFactory.build(frame), BorderLayout.NORTH);
        frame.getContentPane().add(mainSplit);
        frame.setSize(1200, 750);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        if (demoMode) loadDemoData(timelinePanel);
    }

    private static void loadDemoData(TimelineTablePanel timelinePanel) {
        SwingWorker<java.util.List<astrogeist.model.Snapshot>, Void> worker = new SwingWorker<>() {
            @Override
            protected java.util.List<astrogeist.model.Snapshot> doInBackground() throws Exception {
                var demoPath = resolveDemoPath();
                var config = new ScannerConfigReader().readBuiltin("SharpCap");
                return new ConfigurableScanner(config).scan(demoPath);
            }

            @Override
            protected void done() {
                try {
                    timelinePanel.setSnapshots(get());
                } catch (Exception ex) {
                    System.err.println("Demo load failed: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private static Path resolveDemoPath() throws Exception {
        var url = App.class.getResource("/resources/demo");
        if (url == null) throw new IllegalStateException("Demo data not found in classpath");
        var uri = url.toURI();
        if ("file".equals(uri.getScheme())) return Path.of(uri);

        // JAR mode: extract demo folder to a temp directory
        var tmp = Files.createTempDirectory("astrogeist-demo");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> deleteDir(tmp)));
        try (var fs = FileSystems.newFileSystem(uri, Map.of())) {
            var src = fs.getPath("/resources/demo");
            try (var walk = Files.walk(src)) {
                walk.forEach(p -> {
                    try {
                        var rel = src.relativize(p).toString();
                        var dst = rel.isEmpty() ? tmp : tmp.resolve(rel);
                        if (Files.isDirectory(p)) Files.createDirectories(dst);
                        else Files.copy(p, dst, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) { throw new UncheckedIOException(e); }
                });
            }
        }
        return tmp;
    }

    private static void deleteDir(Path dir) {
        try (var walk = Files.walk(dir)) {
            walk.sorted(java.util.Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(java.io.File::delete);
        } catch (IOException ignored) {}
    }
}
