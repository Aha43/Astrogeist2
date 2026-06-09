package astrogeist.persist;

import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public final class XmlScanTargetsStore {

    private final Path file;

    public XmlScanTargetsStore(Path file) {
        this.file = file;
    }

    public List<ScanTarget> load() {
        var targets = new ArrayList<ScanTarget>();
        if (!Files.exists(file)) return targets;
        try (var in = Files.newInputStream(file)) {
            var doc  = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
            var nodes = doc.getDocumentElement().getElementsByTagName("target");
            for (int i = 0; i < nodes.getLength(); i++) {
                var el = (Element) nodes.item(i);
                targets.add(new ScanTarget(el.getAttribute("scanner"), el.getAttribute("folder")));
            }
        } catch (Exception ignored) {}
        return targets;
    }

    public void save(List<ScanTarget> targets) {
        try {
            Files.createDirectories(file.getParent());
            var doc  = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            var root = doc.createElement("scanTargets");
            doc.appendChild(root);
            for (var t : targets) {
                var el = doc.createElement("target");
                el.setAttribute("scanner", t.scannerName());
                el.setAttribute("folder",  t.folder());
                root.appendChild(el);
            }
            var tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.transform(new DOMSource(doc), new StreamResult(file.toFile()));
        } catch (Exception e) {
            System.err.println("Failed to save scan targets: " + e.getMessage());
        }
    }
}
