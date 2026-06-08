package astrogeist.persist;

import java.time.Instant;
import java.util.Map;

public interface UserDataStore {
    Map<String, String> load(Instant instant);
    void save(Instant instant, Map<String, String> entries);
}
