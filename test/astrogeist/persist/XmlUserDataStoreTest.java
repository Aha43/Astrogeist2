package astrogeist.persist;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class XmlUserDataStoreTest {

    private Path dir;
    private XmlUserDataStore store;

    @BeforeEach
    void setUp() throws IOException {
        dir = Files.createTempDirectory("userdata-test");
        store = new XmlUserDataStore(dir);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(dir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(java.io.File::delete);
    }

    @Test
    void loadReturnsEmptyMapWhenNoFileExists() {
        var result = store.load(Instant.parse("2023-07-15T22:30:45Z"));
        assertTrue(result.isEmpty());
    }

    @Test
    void saveAndLoadRoundTrip() {
        var instant = Instant.parse("2023-07-15T22:30:45Z");
        store.save(instant, Map.of("notes", "Great seeing tonight", "rating", "5"));

        var loaded = store.load(instant);
        assertEquals("Great seeing tonight", loaded.get("notes"));
        assertEquals("5", loaded.get("rating"));
    }

    @Test
    void saveCreatesDirectoryIfMissing() throws IOException {
        var nested = dir.resolve("sub").resolve("userdata");
        store = new XmlUserDataStore(nested);
        var instant = Instant.parse("2023-07-15T22:30:45Z");

        assertDoesNotThrow(() -> store.save(instant, Map.of("key", "val")));
        assertTrue(Files.exists(nested));
    }

    @Test
    void overwritePreviousAnnotations() {
        var instant = Instant.parse("2023-07-15T22:30:45Z");
        store.save(instant, Map.of("notes", "first"));
        store.save(instant, Map.of("notes", "second", "rating", "3"));

        var loaded = store.load(instant);
        assertEquals("second", loaded.get("notes"));
        assertEquals("3", loaded.get("rating"));
        assertEquals(2, loaded.size());
    }

    @Test
    void saveWithEmptyEntriesWritesEmptyFile() {
        var instant = Instant.parse("2023-07-16T10:00:00Z");
        store.save(instant, Map.of());
        var loaded = store.load(instant);
        assertTrue(loaded.isEmpty());
    }

    @Test
    void differentInstantsStoredSeparately() {
        var a = Instant.parse("2023-07-15T22:30:45Z");
        var b = Instant.parse("2023-07-16T21:15:00Z");
        store.save(a, Map.of("notes", "session A"));
        store.save(b, Map.of("notes", "session B"));

        assertEquals("session A", store.load(a).get("notes"));
        assertEquals("session B", store.load(b).get("notes"));
    }
}
