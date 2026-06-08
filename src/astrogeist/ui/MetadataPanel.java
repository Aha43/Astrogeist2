package astrogeist.ui;

import astrogeist.model.Snapshot;
import astrogeist.service.SnapshotSelectionService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class MetadataPanel extends JPanel implements SnapshotSelectionService.Listener {

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z").withZone(ZoneId.systemDefault());

    private final List<String[]> rows = new ArrayList<>();
    private final Model model = new Model();

    public MetadataPanel(SnapshotSelectionService selectionService) {
        setLayout(new java.awt.BorderLayout());
        selectionService.addListener(this);

        var table = new JTable(model);
        table.setShowGrid(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(0).setMaxWidth(180);

        add(new JScrollPane(table), java.awt.BorderLayout.CENTER);
    }

    @Override
    public void onSelected(Snapshot snapshot) {
        rows.clear();
        rows.add(new String[]{ "Time (UTC)", snapshot.instant().toString() });
        rows.add(new String[]{ "Time (local)", FMT.format(snapshot.instant()) });
        snapshot.values().entrySet().stream()
            .sorted(java.util.Map.Entry.comparingByKey())
            .forEach(e -> rows.add(new String[]{ e.getKey(), e.getValue().raw() }));
        model.fireTableDataChanged();
    }

    private class Model extends AbstractTableModel {

        @Override public int getRowCount()    { return rows.size(); }
        @Override public int getColumnCount() { return 2; }
        @Override public String getColumnName(int col) { return col == 0 ? "Key" : "Value"; }
        @Override public Object getValueAt(int row, int col) { return rows.get(row)[col]; }
    }
}
