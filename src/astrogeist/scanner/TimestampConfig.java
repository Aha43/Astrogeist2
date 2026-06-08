package astrogeist.scanner;

public record TimestampConfig(
    String source,
    String pattern,
    String format
) {}
