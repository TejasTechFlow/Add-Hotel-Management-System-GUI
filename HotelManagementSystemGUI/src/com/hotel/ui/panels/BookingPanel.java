package com.hotel.ui.panels;

import com.hotel.model.*;
import com.hotel.service.BookingService;
import com.hotel.ui.components.*;
import com.hotel.ui.dialogs.BookingDialog;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * BookingPanel — create bookings, check-in, check-out, cancel.
 */
public class BookingPanel extends JPanel {

    private final JFrame         owner;
    private final User           currentUser;
    private final BookingService bookingService = new BookingService();

    private JTable            table;
    private DefaultTableModel model;
    private JComboBox<String> filterStatus;
    private JTextField        searchField;

    public BookingPanel(JFrame owner, User currentUser) {
        this.owner       = owner;
        this.currentUser = currentUser;
        setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
        loadData();
    }

    private void build() {
        // Header
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        header.add(UIUtils.title("Booking System"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        searchField = UIUtils.textField("Search customer...");
        searchField.setPreferredSize(new Dimension(180, 34));
        searchField.addActionListener(e -> loadData());

        filterStatus = UIUtils.comboBox("All", "CONFIRMED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED");
        filterStatus.setPreferredSize(new Dimension(140, 34));
        filterStatus.addActionListener(e -> loadData());

        JButton newBtn = UIUtils.primaryBtn("+ New Booking");
        newBtn.addActionListener(e -> openNewBooking());

        actions.add(searchField);
        actions.add(filterStatus);
        actions.add(newBtn);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"#", "Customer", "Room", "Type", "Check-In", "Check-Out", "Nights", "Status", "Actions"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 8; }
        };
        table = new JTable(model);
        UIUtils.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(70);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(95);
        table.getColumnModel().getColumn(5).setPreferredWidth(95);
        table.getColumnModel().getColumn(6).setPreferredWidth(55);
        table.getColumnModel().getColumn(7).setPreferredWidth(115);
        table.getColumnModel().getColumn(8).setPreferredWidth(220);

        table.getColumnModel().getColumn(7).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(8).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(8).setCellEditor(new ActionEditor());

        JPanel card = UIUtils.card();
        card.setLayout(new BorderLayout());
        card.add(UIUtils.scrollPane(table), BorderLayout.CENTER);
        JPanel w = new JPanel(new BorderLayout()); w.setOpaque(false);
        w.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        w.add(card);
        add(w, BorderLayout.CENTER);
    }

    private void loadData() {
        model.setRowCount(0);
        String keyword = searchField.getText().trim().toLowerCase();
        String status  = (String) filterStatus.getSelectedItem();
        List<Booking> list = bookingService.getAllBookings();
        for (Booking b : list) {
            if (!keyword.isEmpty() && !b.getCustomerName().toLowerCase().contains(keyword)) continue;
            if (!"All".equals(status) && !b.getStatus().name().equals(status)) continue;
            model.addRow(new Object[]{
                b.getBookingId(), b.getCustomerName(), b.getRoomNumber(),
                b.getRoomType(), b.getCheckIn(), b.getCheckOut(),
                b.getNights(), b.getStatus().name(), b.getBookingId()
            });
        }
    }

    private void openNewBooking() {
        BookingDialog dlg = new BookingDialog(owner, currentUser);
        dlg.setVisible(true);
        if (dlg.isSaved()) { loadData(); UIUtils.toast(owner, "Booking confirmed!", true); }
    }

    private void doCheckIn(int id) {
        try { bookingService.checkIn(id); loadData(); UIUtils.toast(owner, "Check-In successful!", true); }
        catch (Exception ex) { UIUtils.toast(owner, ex.getMessage(), false); }
    }

    private void doCheckOut(int id) {
        try { bookingService.checkOut(id); loadData(); UIUtils.toast(owner, "Check-Out done. Generate bill now.", true); }
        catch (Exception ex) { UIUtils.toast(owner, ex.getMessage(), false); }
    }

    private void doCancel(int id) {
        int r = JOptionPane.showConfirmDialog(owner, "Cancel Booking #" + id + "?",
                "Confirm Cancel", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) return;
        try { bookingService.cancelBooking(id); loadData(); UIUtils.toast(owner, "Booking cancelled.", true); }
        catch (Exception ex) { UIUtils.toast(owner, ex.getMessage(), false); }
    }

    // ── Status renderer ───────────────────────────────────────────────────────

    class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int row, int col) {
            JLabel chip = UIUtils.statusChip(String.valueOf(v), UIUtils.statusColor(String.valueOf(v)));
            chip.setOpaque(true);
            chip.setBackground(sel ? Theme.PRIMARY_LIGHT : (row%2==0 ? Theme.BG_CARD : Theme.BG_TABLE_ALT));
            return chip;
        }
    }

    // ── Action cells ──────────────────────────────────────────────────────────

    class ActionRenderer implements javax.swing.table.TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int row, int col) {
            String status = String.valueOf(model.getValueAt(row, 7));
            return buildActions((int)model.getValueAt(row, 0), status, false);
        }
    }

    class ActionEditor extends DefaultCellEditor {
        private int id; private String status; private JPanel panel;
        ActionEditor() { super(new JCheckBox()); setClickCountToStart(1); }
        @Override public Component getTableCellEditorComponent(JTable t, Object v,
                boolean sel, int row, int col) {
            id     = (int) model.getValueAt(row, 0);
            status = String.valueOf(model.getValueAt(row, 7));
            panel  = buildActions(id, status, true);
            return panel;
        }
        @Override public Object getCellEditorValue() { return id; }
    }

    private JPanel buildActions(int id, String status, boolean interactive) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 4));
        p.setBackground(table.getBackground());

        JButton ciBtn  = UIUtils.successBtn("Check-In");
        JButton coBtn  = UIUtils.primaryBtn("Check-Out");
        JButton canBtn = UIUtils.dangerBtn("Cancel");

        ciBtn.setPreferredSize(new Dimension(80, 26));  ciBtn.setFont(Theme.FONT_SMALL);
        coBtn.setPreferredSize(new Dimension(84, 26));  coBtn.setFont(Theme.FONT_SMALL);
        canBtn.setPreferredSize(new Dimension(64, 26)); canBtn.setFont(Theme.FONT_SMALL);

        // Enable/disable per status
        ciBtn.setEnabled("CONFIRMED".equals(status));
        coBtn.setEnabled("CHECKED_IN".equals(status));
        canBtn.setEnabled("CONFIRMED".equals(status) || "CHECKED_IN".equals(status));

        if (interactive) {
            ciBtn.addActionListener(e  -> { fireStop(); doCheckIn(id);  });
            coBtn.addActionListener(e  -> { fireStop(); doCheckOut(id); });
            canBtn.addActionListener(e -> { fireStop(); doCancel(id);   });
        }
        p.add(ciBtn); p.add(coBtn); p.add(canBtn);
        return p;
    }
    private void fireStop() { if (table.getCellEditor() != null) table.getCellEditor().stopCellEditing(); }
}
