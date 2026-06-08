package astrogeist.scanner;

import java.util.List;

public record ScannerConfig(
    String name,
    String version,
    String folderPattern,
    TimestampConfig timestamp,
    List<FileParserRule> fileParsers
) {
    public ScannerConfig {
        fileParsers = List.copyOf(fileParsers);
    }
}
