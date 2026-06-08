package astrogeist.app;

import java.nio.file.Path;

public final class Resources {

    private static final String DIR_NAME = ".astrogeist2";

    private Resources() {}

    public static Path home() {
        return Path.of(System.getProperty("user.home"), DIR_NAME);
    }

    public static Path resolve(String relative) {
        return home().resolve(relative);
    }
}
