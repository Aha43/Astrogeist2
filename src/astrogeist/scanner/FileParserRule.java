package astrogeist.scanner;

import java.util.List;

public record FileParserRule(
    String filePattern,
    String format,
    String separator,
    List<FieldMapping> fields
) {
    public FileParserRule {
        fields = List.copyOf(fields);
    }
}
