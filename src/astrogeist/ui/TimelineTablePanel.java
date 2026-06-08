package astrogeist.ui;

import astrogeist.model.Snapshot;
import astrogeist.service.SnapshotSelectionService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class TimelineTablePanel extends JPanel {

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss").withZone(ZoneId.systemDefault());

    private static final String[] FIELD_COLUMNS = {
        "gain", "exposure_ms", "binning", "fps", "frame_count"
    };
    private static final String[] COLUMN_HEADERS = {
        "Date / Time", "Gain", "Exposure (ms)", "Binning", "FPS", "Frame Count"
    };

    private final List<Snapshot> snapshots = new ArrayList<>();
    private final SnapshotSelectionService selectionService;
    private final Model model = new Model();

    public TimelineTablePanel(SnapshotSelectionService selectionService) {
        this.selectionService = selectionService;
        setLayout(new java.awt.BorderLayout());

        var table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.getColumnModel().getColumn(0).setPreferredWidth(180);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = table.getSelectedRow();
            if (row >= 0 && row < snapshots.size()) {
                selectionService.select(snapshots.get(row));
            }
        });

        add(new JScrollPane(table), java.awt.BorderLayout.CENTER);
    }

    public void setSnapshots(List<Snapshot> list) {
        snapshots.clear();
        snapshots.addAll(list);
        model.fireTableDataChanged();
    }

    private class Model extends AbstractTableModel {

        @Override public int getRowCount()    { return snapshots.size(); }
        @Override public int getColumnCount() { return COLUMN_HEADERS.length; }
        @Override public String getColumnName(int col) { return COLUMN_HEADERS[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            var s = snapshots.get(row);
            if (col == 0) return FMT.format(s.instant());
            var raw = s.raw(FIELD_COLUMNS[col - 1]);
            return raw != null ? raw : "";
        }
    }
}
