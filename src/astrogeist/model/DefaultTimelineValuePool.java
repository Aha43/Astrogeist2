package astrogeist.model;

import java.util.concurrent.ConcurrentHashMap;

public final class DefaultTimelineValuePool {

    private record Key(Type type, String raw) {
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Key k)) return false;
            return type == k.type && raw.equals(k.raw);
        }
        @Override
        public int hashCode() {
            return System.identityHashCode(type) * 31 + raw.hashCode();
        }
    }

    private final ConcurrentHashMap<Key, TimelineValue> pool = new ConcurrentHashMap<>();

    public TimelineValue get(Type type, String raw) {
        return pool.computeIfAbsent(new Key(type, raw), k -> new TimelineValue(k.type(), k.raw()));
    }
}
