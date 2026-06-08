package astrogeist.scanner;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class ScannerConfigReader {

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
