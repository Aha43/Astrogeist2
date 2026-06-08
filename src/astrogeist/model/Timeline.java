package astrogeist.model;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface Timeline {

    void put(Snapshot snapshot);

    Optional<Snapshot> get(Instant instant);

    List<Snapshot> snapshots();

    List<Snapshot> snapshotsBetween(Instant from, Instant to);

    default int size() { return snapshots().size(); }
}
