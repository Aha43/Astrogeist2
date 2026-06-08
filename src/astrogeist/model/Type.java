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

    public static final class CameraType        extends Type {}
    public static final class GainType          extends Type {}
    public static final class ExposureType      extends Type {}
    public static final class BinningType       extends Type {}
    public static final class FpsType           extends Type {}
    public static final class FrameCountType    extends Type {}
    public static final class TemperatureType   extends Type {}
    public static final class StarsType         extends Type {}
    public static final class ResolutionType    extends Type {}
    public static final class SubjectType       extends Type {}
    public static final class TelescopeType     extends Type {}
    public static final class CaptureTypeType   extends Type {}
    public static final class FilterType        extends Type {}
    public static final class SerFileType       extends Type {}
    public static final class FitFileType       extends Type {}
    public static final class TifFileType       extends Type {}
    public static final class JpgFileType       extends Type {}
    public static final class PngFileType       extends Type {}
    public static final class SoftwareType      extends Type {}
    public static final class TextType          extends Type {}

    // --- Singletons ---

    public static final CameraType      CAMERA       = new CameraType();
    public static final GainType        GAIN         = new GainType();
    public static final ExposureType    EXPOSURE_MS  = new ExposureType();
    public static final BinningType     BINNING      = new BinningType();
    public static final FpsType         FPS          = new FpsType();
    public static final FrameCountType  FRAME_COUNT  = new FrameCountType();
    public static final TemperatureType TEMPERATURE  = new TemperatureType();
    public static final StarsType       STARS        = new StarsType();
    public static final ResolutionType  RESOLUTION   = new ResolutionType();
    public static final SubjectType     SUBJECT      = new SubjectType();
    public static final TelescopeType   TELESCOPE    = new TelescopeType();
    public static final CaptureTypeType CAPTURE_TYPE = new CaptureTypeType();
    public static final FilterType      FILTER       = new FilterType();
    public static final SerFileType     SER_FILE     = new SerFileType();
    public static final FitFileType     FIT_FILE     = new FitFileType();
    public static final TifFileType     TIF_FILE     = new TifFileType();
    public static final JpgFileType     JPG_FILE     = new JpgFileType();
    public static final PngFileType     PNG_FILE     = new PngFileType();
    public static final SoftwareType    SOFTWARE     = new SoftwareType();
    public static final TextType        TEXT         = new TextType();

    // --- Key registry ---

    private static final Map<String, Type> BY_KEY = Map.ofEntries(
        Map.entry("camera",        CAMERA),
        Map.entry("gain",          GAIN),
        Map.entry("exposure_ms",   EXPOSURE_MS),
        Map.entry("exposure_s",    EXPOSURE_MS),
        Map.entry("binning",       BINNING),
        Map.entry("fps",           FPS),
        Map.entry("frame_count",   FRAME_COUNT),
        Map.entry("temperature",   TEMPERATURE),
        Map.entry("stars",         STARS),
        Map.entry("resolution",    RESOLUTION),
        Map.entry("subject",       SUBJECT),
        Map.entry("telescope",     TELESCOPE),
        Map.entry("capture_type",  CAPTURE_TYPE),
        Map.entry("filter",        FILTER),
        Map.entry("ser_file",      SER_FILE),
        Map.entry("fit_file",      FIT_FILE),
        Map.entry("tif_file",      TIF_FILE),
        Map.entry("jpg_file",      JPG_FILE),
        Map.entry("png_file",      PNG_FILE),
        Map.entry("software",      SOFTWARE)
    );

    public static Type forKey(String key) {
        return BY_KEY.getOrDefault(key, TEXT);
    }
}
