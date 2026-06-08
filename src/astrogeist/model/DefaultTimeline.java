package astrogeist.model;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

public final class DefaultTimeline implements Timeline {

    private final ConcurrentSkipListMap<Instant, Snapshot> map = new ConcurrentSkipListMap<>();

    @Override
    public void put(Snapshot snapshot) {
        map.put(snapshot.instant(), snapshot);
    }

    @Override
    public Optional<Snapshot> get(Instant instant) {
        return Optional.ofNullable(map.get(instant));
    }

    @Override
    public List<Snapshot> snapshots() {
        return List.copyOf(map.values());
    }

    @Override
    public List<Snapshot> snapshotsBetween(Instant from, Instant to) {
        return List.copyOf(map.subMap(from, true, to, true).values());
    }
}
