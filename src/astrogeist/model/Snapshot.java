package astrogeist.model;

import java.time.Instant;
import java.util.Map;

public record Snapshot(Instant instant, Map<String, TimelineValue> values) {

    public Snapshot {
        values = Map.copyOf(values);
    }

    public String raw(String key) {
        var v = values.get(key);
        return v != null ? v.raw() : null;
    }
}
