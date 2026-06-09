package astrogeist.persist;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class XmlScanTargetsStoreTest {

    private Path dir;
    private XmlScanTargetsStore store;

    @BeforeEach
    void setUp() throws IOException {
        dir   = Files.createTempDirectory("scan-targets-test");
        store = new XmlScanTargetsStore(dir.resolve("scan-targets.xml"));
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(dir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(java.io.File::delete);
    }

    @Test
    void returnsEmptyListWhenNoFileExists() {
        assertTrue(store.load().isEmpty());
    }

    @Test
    void roundTripSingleTarget() {
        store.save(List.of(new ScanTarget("SharpCap", "/data/sessions")));
        var loaded = store.load();
        assertEquals(1, loaded.size());
        assertEquals("SharpCap",      loaded.get(0).scannerName());
        assertEquals("/data/sessions", loaded.get(0).folder());
    }

    @Test
    void roundTripMultipleTargets() {
        var targets = List.of(
            new ScanTarget("SharpCap", "/data/sharpcap"),
            new ScanTarget("Seestar",  "/data/seestar"),
            new ScanTarget("SharpCap", "/data/other")
        );
        store.save(targets);
        var loaded = store.load();
        assertEquals(3, loaded.size());
        assertEquals("Seestar",   loaded.get(1).scannerName());
        assertEquals("/data/seestar", loaded.get(1).folder());
    }

    @Test
    void overwritesPreviousSave() {
        store.save(List.of(new ScanTarget("SharpCap", "/old")));
        store.save(List.of(new ScanTarget("Seestar",  "/new")));
        var loaded = store.load();
        assertEquals(1, loaded.size());
        assertEquals("Seestar", loaded.get(0).scannerName());
    }

    @Test
    void createsParentDirectoryIfMissing() {
        var nested = new XmlScanTargetsStore(dir.resolve("sub/deep/scan-targets.xml"));
        assertDoesNotThrow(() -> nested.save(List.of(new ScanTarget("SharpCap", "/x"))));
        assertEquals(1, nested.load().size());
    }
}
