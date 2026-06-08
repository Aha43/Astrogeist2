package astrogeist.scanner;

import nom.tam.fits.*;
import nom.tam.fits.FitsFactory;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FitsScannerTest {

    private Path root;

    @BeforeEach
    void setUp() throws Exception {
        root = Files.createTempDirectory("fits-scan-test");
        var sessionDir = root.resolve("2023-10-15");
        Files.createDirectory(sessionDir);
        writeFits(sessionDir.resolve("light_001.fits"),
            "M42",        // OBJECT
            "30.0",       // EXPTIME
            "100",        // GAIN
            "L",          // FILTER
            "Seestar S50" // INSTRUME
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(root).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(java.io.File::delete);
    }

    @Test
    void extractsObjectFromFitsHeader() throws Exception {
        var snapshots = scanWithSeestar();
        assertEquals("M42", snapshots.get(0).raw("subject"));
    }

    @Test
    void extractsExposureFromFitsHeader() throws Exception {
        var snapshots = scanWithSeestar();
        assertEquals("30.0", snapshots.get(0).raw("exposure_s"));
    }

    @Test
    void extractsGainFromFitsHeader() throws Exception {
        var snapshots = scanWithSeestar();
        assertEquals("100", snapshots.get(0).raw("gain"));
    }

    @Test
    void extractsFilterFromFitsHeader() throws Exception {
        var snapshots = scanWithSeestar();
        assertEquals("L", snapshots.get(0).raw("filter"));
    }

    @Test
    void extractsTelescopeFromFitsHeader() throws Exception {
        var snapshots = scanWithSeestar();
        assertEquals("Seestar S50", snapshots.get(0).raw("telescope"));
    }

    @Test
    void filenameIsRecorded() throws Exception {
        var snapshots = scanWithSeestar();
        assertEquals("light_001.fits", snapshots.get(0).raw("fit_file"));
    }

    private List<astrogeist.model.Snapshot> scanWithSeestar() throws Exception {
        var config = new ScannerConfigReader().readBuiltin("Seestar");
        return new ConfigurableScanner(config).scan(root);
    }

    private void writeFits(Path path, String object, String exptime,
                           String gain, String filter, String instrume) throws Exception {
        var hdu = FitsFactory.hduFactory(new short[1][1]);
        var h = hdu.getHeader();
        h.addValue("OBJECT",   object,  "target name");
        h.addValue("EXPTIME",  exptime, "exposure time");
        h.addValue("GAIN",     gain,    "camera gain");
        h.addValue("FILTER",   filter,  "filter name");
        h.addValue("INSTRUME", instrume,"instrument");
        var fits = new Fits();
        fits.addHDU(hdu);
        fits.write(path.toFile());
    }
}
