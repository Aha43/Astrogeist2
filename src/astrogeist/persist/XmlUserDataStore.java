package astrogeist.persist;

import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class XmlUserDataStore implements UserDataStore {

    private final Path dir;

    public XmlUserDataStore(Path dir) {
        this.dir = dir;
    }

    @Override
    public Map<String, String> load(Instant instant) {
        var file = fileFor(instant);
        if (!Files.exists(file)) return Map.of();
        try (var in = Files.newInputStream(file)) {
            var doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
            var entries = new LinkedHashMap<String, String>();
            var nodes = doc.getDocumentElement().getElementsByTagName("e");
            for (int i = 0; i < nodes.getLength(); i++) {
                var el = (Element) nodes.item(i);
                entries.put(el.getAttribute("key"), el.getAttribute("value"));
            }
            return entries;
        } catch (Exception e) {
            return Map.of();
        }
    }

    @Override
    public void save(Instant instant, Map<String, String> entries) {
        try {
            Files.createDirectories(dir);
            var doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            var root = doc.createElement("userdata");
            root.setAttribute("instant", instant.toString());
            doc.appendChild(root);
            for (var entry : entries.entrySet()) {
                var el = doc.createElement("e");
                el.setAttribute("key", entry.getKey());
                el.setAttribute("value", entry.getValue());
                root.appendChild(el);
            }
            var transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(fileFor(instant).toFile()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to save user data for " + instant, e);
        }
    }

    private Path fileFor(Instant instant) {
        return dir.resolve(instant.toString().replace(":", "-") + ".xml");
    }
}
