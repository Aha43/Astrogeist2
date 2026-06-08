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

        createSession("2023-07-15_22_30_45", """
                [Camera]
                Gain=100
                Exposure=50000
                Binning=1
                FPS=10.5
                FrameCount=500
                """);

        createSession("2023-07-16_21_15_00", """
                [Camera]
                Gain=200
                Exposure=30000
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
    void extractsAllExpectedFields() throws IOException {
        var snapshot = new ConfigurableScanner(config).scan(root).get(0);
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

    private void createSession(String folderName, String cameraSettings) throws IOException {
        var dir = root.resolve(folderName);
        Files.createDirectory(dir);
        Files.writeString(dir.resolve("CameraSettings.txt"), cameraSettings);
    }
}
