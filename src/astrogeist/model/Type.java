package astrogeist.model;

import java.util.Map;

public abstract class Type {

    private Type() {}

    @Override
    public final boolean equals(Object o) { return this == o; }

    @Override
    public final int hashCode() { return System.identityHashCode(this); }

    public boolean isA(Class<? extends Type> kind) {
        return kind.isAssignableFrom(getClass());
    }

    // --- Concrete types ---

    public static final class GainType        extends Type {}
    public static final class ExposureType    extends Type {}
    public static final class BinningType     extends Type {}
    public static final class FpsType         extends Type {}
    public static final class FrameCountType  extends Type {}
    public static final class SerFileType     extends Type {}
    public static final class TextType        extends Type {}

    // --- Singletons ---

    public static final GainType       GAIN        = new GainType();
    public static final ExposureType   EXPOSURE_MS = new ExposureType();
    public static final BinningType    BINNING     = new BinningType();
    public static final FpsType        FPS         = new FpsType();
    public static final FrameCountType FRAME_COUNT = new FrameCountType();
    public static final SerFileType    SER_FILE    = new SerFileType();
    public static final TextType       TEXT        = new TextType();

    // --- Key registry ---

    private static final Map<String, Type> BY_KEY = Map.of(
        "gain",        GAIN,
        "exposure_ms", EXPOSURE_MS,
        "binning",     BINNING,
        "fps",         FPS,
        "frame_count", FRAME_COUNT,
        "ser_file",    SER_FILE
    );

    public static Type forKey(String key) {
        return BY_KEY.getOrDefault(key, TEXT);
    }
}
