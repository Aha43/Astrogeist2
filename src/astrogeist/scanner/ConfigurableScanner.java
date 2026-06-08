package astrogeist.scanner;

import astrogeist.model.*;
import nom.tam.fits.Fits;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public final class ConfigurableScanner {

    private final ScannerConfig config;

    public ConfigurableScanner(ScannerConfig config) {
        this.config = config;
    }

    public List<Snapshot> scan(Path root) throws IOException {
        var folderRegex = Pattern.compile(config.folderPattern());
        var pool = new DefaultTimelineValuePool();

        try (var entries = Files.list(root)) {
            return entries
                .filter(Files::isDirectory)
                .filter(p -> folderRegex.matcher(p.getFileName().toString()).matches())
                .map(folder -> scanFolder(folder, pool))
                .filter(s -> s != null)
                .sorted((a, b) -> a.instant().compareTo(b.instant()))
                .toList();
        }
    }

    private Snapshot scanFolder(Path folder, DefaultTimelineValuePool pool) {
        var instant = extractInstant(folder);
        if (instant == null) return null;

        var values = new HashMap<String, TimelineValue>();

        for (var rule : config.fileParsers()) {
            var fileRegex = Pattern.compile(rule.filePattern());
            try (var files = Files.list(folder)) {
                files.filter(f -> fileRegex.matcher(f.getFileName().toString()).matches())
                     .forEach(f -> applyRule(f, rule, values, pool));
            } catch (IOException e) {
                // skip unreadable subdirectory
            }
        }

        return new Snapshot(instant, values);
    }

    private void applyRule(Path file, FileParserRule rule,
                           HashMap<String, TimelineValue> values,
                           DefaultTimelineValuePool pool) {
        switch (rule.format()) {
            case "keyvalue" -> applyKeyValueRule(file, rule, values, pool);
            case "filename" -> applyFilenameRule(file, rule, values, pool);
            case "fits-header" -> applyFitsHeaderRule(file, rule, values, pool);
        }
    }

    private void applyKeyValueRule(Path file, FileParserRule rule,
                                   HashMap<String, TimelineValue> values,
                                   DefaultTimelineValuePool pool) {
        var sep = rule.separator().isEmpty() ? "=" : rule.separator();
        var fieldMap = new HashMap<String, String>();
        for (var m : rule.fields()) fieldMap.put(m.src(), m.key());

        var sectionPattern = Pattern.compile("^\\[(.+)\\]$");

        try {
            for (var line : Files.readAllLines(file)) {
                var trimmed = line.trim();

                // section header line: [Camera Name]
                var sectionMatch = sectionPattern.matcher(trimmed);
                if (sectionMatch.matches()) {
                    var outKey = fieldMap.get("$section");
                    if (outKey != null) {
                        values.put(outKey, pool.get(Type.forKey(outKey), sectionMatch.group(1)));
                    }
                    continue;
                }

                var idx = trimmed.indexOf(sep);
                if (idx < 0) continue;
                var src = trimmed.substring(0, idx).trim();
                var val = trimmed.substring(idx + sep.length()).trim();
                var outKey = fieldMap.get(src);
                if (outKey != null) {
                    values.put(outKey, pool.get(Type.forKey(outKey), val));
                }
            }
        } catch (IOException e) {
            // skip unreadable file
        }
    }

    private void applyFilenameRule(Path file, FileParserRule rule,
                                   HashMap<String, TimelineValue> values,
                                   DefaultTimelineValuePool pool) {
        var filename = file.getFileName().toString();
        for (var m : rule.fields()) {
            if ("filename".equals(m.src())) {
                values.put(m.key(), pool.get(Type.forKey(m.key()), filename));
            }
        }
    }

    private void applyFitsHeaderRule(Path file, FileParserRule rule,
                                     HashMap<String, TimelineValue> values,
                                     DefaultTimelineValuePool pool) {
        var fieldMap = new HashMap<String, String>();
        for (var m : rule.fields()) fieldMap.put(m.src(), m.key());

        try (var fits = new Fits(file.toFile())) {
            var hdu = fits.readHDU();
            if (hdu == null) return;
            var header = hdu.getHeader();
            for (var entry : fieldMap.entrySet()) {
                var val = header.getStringValue(entry.getKey());
                if (val != null && !val.isBlank()) {
                    values.put(entry.getValue(), pool.get(Type.forKey(entry.getValue()), val.trim()));
                }
            }
        } catch (Exception e) {
            // skip unreadable or invalid FITS file
        }
    }

    private java.time.Instant extractInstant(Path folder) {
        var ts = config.timestamp();
        return switch (ts.source()) {
            case "folderName"  -> extractFromFolderName(folder.getFileName().toString(), ts);
            case "folderMtime" -> extractFromMtime(folder);
            default            -> null;
        };
    }

    private java.time.Instant extractFromFolderName(String folderName, TimestampConfig ts) {
        var matcher = Pattern.compile(ts.pattern()).matcher(folderName);
        if (!matcher.find()) return null;
        try {
            var matched = matcher.group(0);
            var formatter = DateTimeFormatter.ofPattern(ts.format());
            return LocalDateTime.parse(matched, formatter).toInstant(ZoneOffset.UTC);
        } catch (Exception e) {
            return null;
        }
    }

    private java.time.Instant extractFromMtime(Path folder) {
        try {
            return Files.readAttributes(folder, BasicFileAttributes.class)
                        .lastModifiedTime().toInstant();
        } catch (IOException e) {
            return null;
        }
    }
}
