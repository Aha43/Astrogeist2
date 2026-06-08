package astrogeist.service;

import astrogeist.model.Snapshot;
import java.util.ArrayList;
import java.util.List;

public final class SnapshotSelectionService {

    public interface Listener {
        void onSelected(Snapshot snapshot);
    }

    private final List<Listener> listeners = new ArrayList<>();

    public void addListener(Listener l) {
        listeners.add(l);
    }

    public void select(Snapshot snapshot) {
        for (var l : listeners) l.onSelected(snapshot);
    }
}
