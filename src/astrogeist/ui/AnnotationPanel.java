package astrogeist.ui;

import astrogeist.model.Snapshot;
import astrogeist.persist.UserDataStore;
import astrogeist.service.SnapshotSelectionService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class AnnotationPanel extends JPanel implements SnapshotSelectionService.Listener {

    private final UserDataStore store;
    private final List<String[]> rows = new ArrayList<>();
    private final Model model = new Model();
    private Instant currentInstant;

    public AnnotationPanel(UserDataStore store, SnapshotSelectionService selectionService) {
        this.store = store;
        selectionService.addListener(this);
        setLayout(new BorderLayout());

        var table = new JTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(0).setMaxWidth(180);
        table.putClientProperty("terminateEditOnFocusLost", true);

        var toolbar = new JToolBar();
        toolbar.setFloatable(false);

        var addBtn = new JButton("+");
        addBtn.setToolTipText("Add annotation");
        addBtn.addActionListener(e -> {
            rows.add(new String[]{"", ""});
            model.fireTableDataChanged();
            table.editCellAt(rows.size() - 1, 0);
        });

        var removeBtn = new JButton("−");
        removeBtn.setToolTipText("Remove selected row");
        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) { rows.remove(row); model.fireTableDataChanged(); }
        });

        var saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> save(table));

        toolbar.add(addBtn);
        toolbar.add(removeBtn);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(saveBtn);

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        setBorder(BorderFactory.createTitledBorder("Annotations"));
    }

    @Override
    public void onSelected(Snapshot snapshot) {
        currentInstant = snapshot.instant();
        rows.clear();
        store.load(currentInstant).forEach((k, v) -> rows.add(new String[]{k, v}));
        model.fireTableDataChanged();
    }

    private void save(JTable table) {
        if (currentInstant == null) return;
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
        var entries = new LinkedHashMap<String, String>();
        for (var row : rows) {
            if (!row[0].isBlank()) entries.put(row[0].trim(), row[1]);
        }
        store.save(currentInstant, entries);
    }

    private class Model extends AbstractTableModel {
        @Override public int getRowCount()    { return rows.size(); }
        @Override public int getColumnCount() { return 2; }
        @Override public String getColumnName(int col) { return col == 0 ? "Key" : "Value"; }
        @Override public boolean isCellEditable(int r, int c) { return currentInstant != null; }
        @Override public Object getValueAt(int row, int col)  { return rows.get(row)[col]; }
        @Override public void setValueAt(Object val, int row, int col) {
            rows.get(row)[col] = val == null ? "" : val.toString();
        }
    }
}
