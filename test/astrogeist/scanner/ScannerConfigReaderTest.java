package astrogeist.scanner;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ScannerConfigReaderTest {

    private final ScannerConfigReader reader = new ScannerConfigReader();

    @Test
    void readsSharpCapBuiltinConfig() throws Exception {
        var config = reader.readBuiltin("SharpCap");

        assertEquals("SharpCap", config.name());
        assertNotNull(config.folderPattern());
        assertFalse(config.folderPattern().isBlank());
    }

    @Test
    void sharpCapTimestampConfigIsPresent() throws Exception {
        var config = reader.readBuiltin("SharpCap");
        var ts = config.timestamp();

        assertEquals("folderName", ts.source());
        assertFalse(ts.pattern().isBlank());
        assertFalse(ts.format().isBlank());
    }

    @Test
    void sharpCapHasCameraSettingsParser() throws Exception {
        var config = reader.readBuiltin("SharpCap");

        var cameraParser = config.fileParsers().stream()
            .filter(r -> r.filePattern().contains("CameraSettings"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No CameraSettings parser found"));

        assertEquals("keyvalue", cameraParser.format());
        assertEquals("=", cameraParser.separator());

        var keys = cameraParser.fields().stream().map(FieldMapping::key).toList();
        assertTrue(keys.contains("gain"));
        assertTrue(keys.contains("exposure_ms"));
        assertTrue(keys.contains("binning"));
        assertTrue(keys.contains("fps"));
        assertTrue(keys.contains("frame_count"));
    }

    @Test
    void sharpCapHasSerFileParser() throws Exception {
        var config = reader.readBuiltin("SharpCap");

        var serParser = config.fileParsers().stream()
            .filter(r -> r.filePattern().contains(".ser"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No .ser parser found"));

        assertEquals("filename", serParser.format());
    }
}
