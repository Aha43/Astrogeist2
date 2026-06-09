package astrogeist.scanner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.Comparator;
import static org.junit.jupiter.api.Assertions.*;

class ConfigurableScannerTest {

    private Path root;
    private ScannerConfig config;

    @BeforeEach
    void setUp() throws Exception {
        root = Files.createTempDirectory("astrogeist-test");
        config = new ScannerConfigReader().readBuiltin("SharpCap");

        // Real SharpCap format: [Camera Name] section header + Key=Value pairs
        createSession("2023-07-15_22_30_45", """
                [ZWO ASI533MC-Pro]
                Gain=100
                ExposureMs=50000
                Binning=1
                FPS=10.5
                FrameCount=500
                Temperature=-10.2
                Capture Area=3008x3008
                SharpCapVersion=4.2.1
                """);

        createSession("2023-07-16_21_15_00", """
                [ZWO ASI294MC-Pro]
                Gain=200
                ExposureMs=30000
                Binning=2
                FPS=25.0
                FrameCount=300
                """);

        // non-matching folder — should be ignored
        Files.createDirectory(root.resolve("not_a_session"));
        Files.writeString(root.resolve("not_a_session/random.txt"), "hello");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(root).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(java.io.File::delete);
    }

    @Test
    void findsExactlyTwoSessions() throws IOException {
        var snapshots = new ConfigurableScanner(config).scan(root);
        assertEquals(2, snapshots.size());
    }

    @Test
    void snapshotsAreInChronologicalOrder() throws IOException {
        var snapshots = new ConfigurableScanner(config).scan(root);
        assertTrue(snapshots.get(0).instant().isBefore(snapshots.get(1).instant()));
    }

    @Test
    void firstSessionHasCorrectInstant() throws IOException {
        var snapshots = new ConfigurableScanner(config).scan(root);
        assertEquals(Instant.parse("2023-07-15T22:30:45Z"), snapshots.get(0).instant());
    }

    @Test
    void extractsGainFromCameraSettings() throws IOException {
        var snapshots = new ConfigurableScanner(config).scan(root);
        assertEquals("100", snapshots.get(0).raw("gain"));
        assertEquals("200", snapshots.get(1).raw("gain"));
    }

    @Test
    void extractsExposureFromCameraSettings() throws IOException {
        var snapshots = new ConfigurableScanner(config).scan(root);
        assertEquals("50000", snapshots.get(0).raw("exposure_ms"));
    }

    @Test
    void extractsCameraFromSectionHeader() throws IOException {
        var snapshot = new ConfigurableScanner(config).scan(root).get(0);
        assertEquals("ZWO ASI533MC-Pro", snapshot.raw("camera"));
    }

    @Test
    void extractsTemperatureAndResolution() throws IOException {
        var snapshot = new ConfigurableScanner(config).scan(root).get(0);
        assertEquals("-10.2", snapshot.raw("temperature"));
        assertEquals("3008x3008", snapshot.raw("resolution"));
    }

    @Test
    void extractsAllExpectedFields() throws IOException {
        var snapshot = new ConfigurableScanner(config).scan(root).get(0);
        assertNotNull(snapshot.raw("camera"));
        assertNotNull(snapshot.raw("gain"));
        assertNotNull(snapshot.raw("exposure_ms"));
        assertNotNull(snapshot.raw("binning"));
        assertNotNull(snapshot.raw("fps"));
        assertNotNull(snapshot.raw("frame_count"));
    }

    @Test
    void serFileIsRecordedWhenPresent() throws IOException {
        var serName = "2023-07-15_22_30_45.ser";
        Files.writeString(root.resolve("2023-07-15_22_30_45/" + serName), "");

        var snapshot = new ConfigurableScanner(config).scan(root).get(0);
        assertEquals(serName, snapshot.raw("ser_file"));
    }

    @Test
    void progressListenerReceivesOneCallPerFolder() throws IOException {
        var calls = new java.util.ArrayList<int[]>();
        new ConfigurableScanner(config).scan(root, (done, total, name) -> calls.add(new int[]{done, total}));

        assertEquals(2, calls.size());
        assertEquals(1, calls.get(0)[0]);
        assertEquals(2, calls.get(0)[1]);
        assertEquals(2, calls.get(1)[0]);
        assertEquals(2, calls.get(1)[1]);
    }

    @Test
    void cancelledThreadStopsScanEarly() throws Exception {
        // pre-interrupt the thread so the scanner loop exits after the first folder
        Thread.currentThread().interrupt();
        try {
            var snapshots = new ConfigurableScanner(config).scan(root, (d, t, n) -> {});
            assertTrue(snapshots.size() < 2, "Should have stopped early after interruption");
        } finally {
            Thread.interrupted(); // clear flag
        }
    }

    private void createSession(String folderName, String cameraSettings) throws IOException {
        var dir = root.resolve(folderName);
        Files.createDirectory(dir);
        Files.writeString(dir.resolve("CameraSettings.txt"), cameraSettings);
    }
}
