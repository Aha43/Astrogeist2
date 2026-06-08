package astrogeist.persist;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import static org.junit.jupiter.api.Assertions.*;

class XmlSettingsStoreTest {

    private Path dir;
    private XmlSettingsStore store;

    @BeforeEach
    void setUp() throws IOException {
        dir = Files.createTempDirectory("settings-test");
        store = new XmlSettingsStore(dir.resolve("settings.xml"));
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(dir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(java.io.File::delete);
    }

    @Test
    void returnsDefaultsWhenNoFileExists() {
        var s = store.load();
        assertEquals("", s.getLastFolder());
        assertEquals("SharpCap", s.getLastScanner());
        assertEquals(0, s.getColumnWidths().length);
    }

    @Test
    void savesAndLoadsFolder() {
        var s = new AppSettings();
        s.setLastFolder("/Users/arne/astrophotos");
        store.save(s);

        assertEquals("/Users/arne/astrophotos", store.load().getLastFolder());
    }

    @Test
    void savesAndLoadsScannerName() {
        var s = new AppSettings();
        s.setLastScanner("Seestar");
        store.save(s);

        assertEquals("Seestar", store.load().getLastScanner());
    }

    @Test
    void savesAndLoadsColumnWidths() {
        var s = new AppSettings();
        s.setColumnWidths(new int[]{180, 60, 90, 55, 55, 80});
        store.save(s);

        var loaded = store.load().getColumnWidths();
        assertArrayEquals(new int[]{180, 60, 90, 55, 55, 80}, loaded);
    }

    @Test
    void overwritesPreviousSave() {
        var s = new AppSettings();
        s.setLastScanner("SharpCap");
        store.save(s);

        s.setLastScanner("Seestar");
        store.save(s);

        assertEquals("Seestar", store.load().getLastScanner());
    }

    @Test
    void createsParentDirectoryIfMissing() {
        var nested = new XmlSettingsStore(dir.resolve("sub/deep/settings.xml"));
        var s = new AppSettings();
        s.setLastFolder("/tmp/test");
        assertDoesNotThrow(() -> nested.save(s));
        assertEquals("/tmp/test", nested.load().getLastFolder());
    }
}
