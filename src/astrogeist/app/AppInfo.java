package astrogeist.app;

import java.io.InputStream;

public final class AppInfo {

    public static final String NAME = "Astrogeist 2";

    private AppInfo() {}

    public static String version() {
        try (InputStream stream = AppInfo.class.getResourceAsStream("/VERSION")) {
            if (stream == null) return "unknown";
            return new String(stream.readAllBytes()).trim();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
