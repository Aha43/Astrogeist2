package astrogeist.persist;

import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.nio.file.*;

public final class XmlSettingsStore {

    private final Path file;

    public XmlSettingsStore(Path file) {
        this.file = file;
    }

    public AppSettings load() {
        var settings = new AppSettings();
        if (!Files.exists(file)) return settings;
        try (var in = Files.newInputStream(file)) {
            var doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
            var root = doc.getDocumentElement();

            var folders = root.getElementsByTagName("lastFolder");
            if (folders.getLength() > 0) settings.setLastFolder(folders.item(0).getTextContent());

            var scanners = root.getElementsByTagName("lastScanner");
            if (scanners.getLength() > 0) settings.setLastScanner(scanners.item(0).getTextContent());

            var cols = root.getElementsByTagName("column");
            if (cols.getLength() > 0) {
                var widths = new int[cols.getLength()];
                for (int i = 0; i < cols.getLength(); i++) {
                    var el = (Element) cols.item(i);
                    widths[i] = Integer.parseInt(el.getAttribute("width"));
                }
                settings.setColumnWidths(widths);
            }
        } catch (Exception ignored) {}
        return settings;
    }

    public void save(AppSettings settings) {
        try {
            Files.createDirectories(file.getParent());
            var doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            var root = doc.createElement("settings");
            doc.appendChild(root);

            var folder = doc.createElement("lastFolder");
            folder.setTextContent(settings.getLastFolder());
            root.appendChild(folder);

            var scanner = doc.createElement("lastScanner");
            scanner.setTextContent(settings.getLastScanner());
            root.appendChild(scanner);

            var columns = doc.createElement("columns");
            root.appendChild(columns);
            var widths = settings.getColumnWidths();
            for (int i = 0; i < widths.length; i++) {
                var col = doc.createElement("column");
                col.setAttribute("index", String.valueOf(i));
                col.setAttribute("width", String.valueOf(widths[i]));
                columns.appendChild(col);
            }

            var tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.transform(new DOMSource(doc), new StreamResult(file.toFile()));
        } catch (Exception e) {
            System.err.println("Failed to save settings: " + e.getMessage());
        }
    }
}
