package astrogeist.model;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class DefaultTimelineTest {

    private static Snapshot snapshot(String isoInstant) {
        return new Snapshot(Instant.parse(isoInstant), Map.of());
    }

    @Test
    void putAndGetByInstant() {
        var timeline = new DefaultTimeline();
        var s = snapshot("2023-07-15T22:30:45Z");
        timeline.put(s);
        assertEquals(s, timeline.get(Instant.parse("2023-07-15T22:30:45Z")).orElseThrow());
    }

    @Test
    void snapshotsReturnedInChronologicalOrder() {
        var timeline = new DefaultTimeline();
        timeline.put(snapshot("2023-07-16T00:00:00Z"));
        timeline.put(snapshot("2023-07-14T00:00:00Z"));
        timeline.put(snapshot("2023-07-15T00:00:00Z"));

        var list = timeline.snapshots();
        assertEquals(3, list.size());
        assertTrue(list.get(0).instant().isBefore(list.get(1).instant()));
        assertTrue(list.get(1).instant().isBefore(list.get(2).instant()));
    }

    @Test
    void snapshotsBetweenReturnsInclusive() {
        var timeline = new DefaultTimeline();
        timeline.put(snapshot("2023-07-13T00:00:00Z"));
        timeline.put(snapshot("2023-07-14T00:00:00Z"));
        timeline.put(snapshot("2023-07-15T00:00:00Z"));
        timeline.put(snapshot("2023-07-16T00:00:00Z"));

        var result = timeline.snapshotsBetween(
            Instant.parse("2023-07-14T00:00:00Z"),
            Instant.parse("2023-07-15T00:00:00Z")
        );
        assertEquals(2, result.size());
    }

    @Test
    void getMissingInstantReturnsEmpty() {
        var timeline = new DefaultTimeline();
        assertTrue(timeline.get(Instant.parse("2023-01-01T00:00:00Z")).isEmpty());
    }

    @Test
    void latestSnapshotOverwritesPrevious() {
        var timeline = new DefaultTimeline();
        var instant = Instant.parse("2023-07-15T22:30:45Z");
        timeline.put(new Snapshot(instant, Map.of("a", new TimelineValue(Type.TEXT, "first"))));
        timeline.put(new Snapshot(instant, Map.of("a", new TimelineValue(Type.TEXT, "second"))));

        assertEquals("second", timeline.get(instant).orElseThrow().raw("a"));
        assertEquals(1, timeline.size());
    }
}
