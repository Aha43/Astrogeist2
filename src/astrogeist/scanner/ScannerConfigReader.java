package astrogeist.scanner;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public final class ScannerConfigReader {

    private static final List<String> BUNDLED = List.of("SharpCap", "Seestar");

    public List<String> listAvailable() {
        var names = new ArrayList<>(BUNDLED);
        var userDir = Path.of(System.getProperty("user.home"), ".astrogeist2", "scanners");
        if (Files.isDirectory(userDir)) {
            try (var s = Files.list(userDir)) {
                s.filter(p -> p.toString().endsWith(".xml"))
                 .map(p -> p.getFileName().toString().replace(".xml", ""))
                 .forEach(names::add);
            } catch (IOException ignored) {}
        }
        return names;
    }

    public ScannerConfig read(String name) throws Exception {
        var path = "/resources/scanners/" + name + ".xml";
        var url = ScannerConfigReader.class.getResource(path);
        if (url != null) {
            try (var in = url.openStream()) { return read(in); }
        }
        var userPath = Path.of(System.getProperty("user.home"), ".astrogeist2", "scanners", name + ".xml");
        if (Files.exists(userPath)) {
            try (var in = Files.newInputStream(userPath)) { return read(in); }
        }
        throw new IllegalArgumentException("Unknown scanner: " + name);
    }

    public ScannerConfig read(InputStream in) throws Exception {
        var doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(in);
        doc.getDocumentElement().normalize();

        var root = doc.getDocumentElement();
        var name    = root.getAttribute("name");
        var version = root.getAttribute("version");

        var folderPatternEl = (Element) root.getElementsByTagName("folderPattern").item(0);
        var folderPattern = folderPatternEl.getAttribute("regex");

        var tsEl = (Element) root.getElementsByTagName("timestamp").item(0);
        var timestamp = new TimestampConfig(
            tsEl.getAttribute("source"),
            tsEl.getAttribute("pattern"),
            tsEl.getAttribute("format")
        );

        var parserNodes = root.getElementsByTagName("fileParser");
        var fileParsers = new ArrayList<FileParserRule>();
        for (int i = 0; i < parserNodes.getLength(); i++) {
            fileParsers.add(parseFileParser((Element) parserNodes.item(i)));
        }

        return new ScannerConfig(name, version, folderPattern, timestamp, fileParsers);
    }

    public ScannerConfig readBuiltin(String name) throws Exception {
        var path = "/resources/scanners/" + name + ".xml";
        try (var in = ScannerConfigReader.class.getResourceAsStream(path)) {
            if (in == null) throw new IllegalArgumentException("Bundled scanner config not found: " + path);
            return read(in);
        }
    }

    private FileParserRule parseFileParser(Element el) {
        var filePattern = el.getAttribute("filePattern");
        var format      = el.getAttribute("format");
        var separator   = el.getAttribute("separator");

        var fieldNodes = el.getElementsByTagName("field");
        var fields = new ArrayList<FieldMapping>();
        for (int i = 0; i < fieldNodes.getLength(); i++) {
            var f = (Element) fieldNodes.item(i);
            fields.add(new FieldMapping(f.getAttribute("src"), f.getAttribute("key")));
        }

        return new FileParserRule(filePattern, format, separator, fields);
    }
}
