package com.hotel.ui.panels;

import com.hotel.model.Room;
import com.hotel.model.User;
import com.hotel.service.RoomService;
import com.hotel.ui.components.*;
import com.hotel.ui.dialogs.RoomDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * RoomPanel — lists all rooms, with Add / Edit / Delete / Status-change actions.
 */
public class RoomPanel extends JPanel {

    private final JFrame        owner;
    private final User          currentUser;
    private final RoomService   roomService = new RoomService();

    private JTable             table;
    private DefaultTableModel  model;
    private JTextField         searchField;
    private JComboBox<String>  filterStatus;

    public RoomPanel(JFrame owner, User currentUser) {
        this.owner       = owner;
        this.currentUser = currentUser;
        setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
        loadData();
    }

    private void build() {
        // ── Header row
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        header.add(UIUtils.title("Room Management"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        searchField = UIUtils.textField("Search room number...");
        searchField.setPreferredSize(new Dimension(180, 34));
        searchField.addActionListener(e -> loadData());

        filterStatus = UIUtils.comboBox("All Status", "AVAILABLE", "BOOKED", "MAINTENANCE");
        filterStatus.setPreferredSize(new Dimension(140, 34));
        filterStatus.addActionListener(e -> loadData());

        JButton addBtn = UIUtils.primaryBtn("+ Add Room");
        addBtn.addActionListener(e -> openAddDialog());

        if (!currentUser.isAdmin()) addBtn.setEnabled(false);

        actions.add(searchField);
        actions.add(filterStatus);
        actions.add(addBtn);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Table
        String[] cols = {"ID", "Room No.", "Type", "Floor", "₹/Night", "Status", "Description", "Actions"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 7; }
            @Override public Class<?> getColumnClass(int c) { return c == 7 ? JPanel.class : Object.class; }
        };
        table = new JTable(model);
        UIUtils.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(50);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(200);
        table.getColumnModel().getColumn(7).setPreferredWidth(180);

        // Custom renderer + editor for action buttons
        table.getColumnModel().getColumn(7).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new ActionEditor());

        JPanel tableCard = UIUtils.card();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(UIUtils.scrollPane(table), BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        wrapper.add(tableCard);
        add(wrapper, BorderLayout.CENTER);
    }

    public void loadData() {
        model.setRowCount(0);
        String search       = searchField.getText().trim().toLowerCase();
        String statusFilter = (String) filterStatus.getSelectedItem();

        List<Room> rooms = roomService.getAllRooms();
        for (Room r : rooms) {
            if (!search.isEmpty() && !r.getRoomNumber().toLowerCase().contains(search)) continue;
            if (!"All Status".equals(statusFilter) && !r.getStatus().name().equals(statusFilter)) continue;
            model.addRow(new Object[]{
                r.getRoomId(), r.getRoomNumber(), r.getRoomType(),
                r.getFloorNumber(), String.format("₹%.2f", r.getPricePerNight()),
                r.getStatus().name(), r.getDescription(), r.getRoomId()
            });
        }
    }

    private void openAddDialog() {
        RoomDialog dlg = new RoomDialog(owner, null, currentUser);
        dlg.setVisible(true);
        if (dlg.isSaved()) { loadData(); UIUtils.toast(owner, "Room added successfully!", true); }
    }

    private void editRoom(int roomId) {
        Room room = roomService.getById(roomId);
        RoomDialog dlg = new RoomDialog(owner, room, currentUser);
        dlg.setVisible(true);
        if (dlg.isSaved()) { loadData(); UIUtils.toast(owner, "Room updated.", true); }
    }

    private void deleteRoom(int roomId) {
        int r = JOptionPane.showConfirmDialog(owner,
            "Delete Room #" + roomId + "? This cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) return;
        try {
            roomService.deleteRoom(roomId);
            loadData();
            UIUtils.toast(owner, "Room deleted.", true);
        } catch (Exception ex) {
            UIUtils.toast(owner, ex.getMessage(), false);
        }
    }

    private void toggleMaintenance(int roomId) {
        try {
            Room room = roomService.getById(roomId);
            Room.RoomStatus next = room.getStatus() == Room.RoomStatus.MAINTENANCE
                                   ? Room.RoomStatus.AVAILABLE : Room.RoomStatus.MAINTENANCE;
            roomService.setRoomStatus(roomId, next);
            loadData();
        } catch (Exception ex) {
            UIUtils.toast(owner, ex.getMessage(), false);
        }
    }

    // ── Action button renderer / editor ───────────────────────────────────────

    class ActionRenderer implements javax.swing.table.TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int row, int col) {
            return buildActions((int) model.getValueAt(row, 0), false);
        }
    }

    class ActionEditor extends DefaultCellEditor {
        private int roomId;
        private JPanel panel;
        ActionEditor() {
            super(new JCheckBox());
            setClickCountToStart(1);
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v,
                boolean sel, int row, int col) {
            roomId = (int) model.getValueAt(row, 0);
            panel  = buildActions(roomId, true);
            return panel;
        }
        @Override public Object getCellEditorValue() { return roomId; }
    }

    private JPanel buildActions(int roomId, boolean interactive) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        p.setBackground(table.getBackground());

        JButton editBtn = UIUtils.outlineBtn("Edit");
        JButton delBtn  = UIUtils.dangerBtn("Delete");
        JButton mntBtn  = UIUtils.button("Maint.", new Color(217, 119, 6), Color.WHITE);

        editBtn.setFont(Theme.FONT_SMALL);
        delBtn.setFont(Theme.FONT_SMALL);
        mntBtn.setFont(Theme.FONT_SMALL);

        Dimension btnSize = new Dimension(62, 26);
        editBtn.setPreferredSize(btnSize);
        delBtn.setPreferredSize(btnSize);
        mntBtn.setPreferredSize(btnSize);

        if (interactive && currentUser.isAdmin()) {
            editBtn.addActionListener(e -> { fireEditingStopped(); editRoom(roomId); });
            delBtn.addActionListener(e  -> { fireEditingStopped(); deleteRoom(roomId); });
            mntBtn.addActionListener(e  -> { fireEditingStopped(); toggleMaintenance(roomId); });
        }
        if (!currentUser.isAdmin()) { editBtn.setEnabled(false); delBtn.setEnabled(false); }

        p.add(editBtn); p.add(delBtn); p.add(mntBtn);
        return p;
    }

    private void fireEditingStopped() {
        if (table.getCellEditor() != null) table.getCellEditor().stopCellEditing();
    }
}
