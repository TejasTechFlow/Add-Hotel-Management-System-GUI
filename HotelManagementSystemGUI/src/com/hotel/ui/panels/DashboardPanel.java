package com.hotel.ui.panels;

import com.hotel.dao.*;
import com.hotel.model.*;
import com.hotel.service.BillingService;
import com.hotel.service.ReportService;
import com.hotel.ui.components.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * DashboardPanel — summary KPI cards + recent bookings + quick stats.
 */
public class DashboardPanel extends JPanel {

    private final User            currentUser;
    private final RoomDAO         roomDAO     = new RoomDAO();
    private final CustomerDAO     customerDAO = new CustomerDAO();
    private final BookingDAO      bookingDAO  = new BookingDAO();
    private final BillingService  billing     = new BillingService();

    private StatsCard cardRooms, cardCustomers, cardBookings, cardRevenue;
    private JTable    recentTable;
    private DefaultTableModel tableModel;

    public DashboardPanel(User user) {
        this.currentUser = user;
        setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
        refresh();
    }

    private void build() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = UIUtils.title("Dashboard");
        JLabel date  = UIUtils.muted("Today: " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")));
        header.add(title, BorderLayout.WEST);
        header.add(date,  BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Stats cards row
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        cardRooms     = new StatsCard("Total Rooms",      "—", "🏠", Theme.PRIMARY);
        cardCustomers = new StatsCard("Customers",         "—", "👥", Theme.ACCENT);
        cardBookings  = new StatsCard("Active Bookings",   "—", "📅", new Color(124, 58, 237));
        cardRevenue   = new StatsCard("Today's Revenue",   "—", "💰", new Color(217, 119, 6));
        statsRow.add(cardRooms);
        statsRow.add(cardCustomers);
        statsRow.add(cardBookings);
        statsRow.add(cardRevenue);

        // Main content: table + right sidebar
        JPanel content = new JPanel(new BorderLayout(20, 0));
        content.setOpaque(false);

        // Recent bookings table
        JPanel tableCard = UIUtils.card();
        tableCard.setLayout(new BorderLayout(0, 12));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        tableHeader.add(UIUtils.heading("Recent Bookings"), BorderLayout.WEST);

        String[] cols = {"#", "Customer", "Room", "Type", "Check-In", "Check-Out", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        recentTable = new JTable(tableModel);
        UIUtils.styleTable(recentTable);
        recentTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        recentTable.getColumnModel().getColumn(6).setPreferredWidth(110);

        tableCard.add(tableHeader,              BorderLayout.NORTH);
        tableCard.add(UIUtils.scrollPane(recentTable), BorderLayout.CENTER);

        // Right panel (check-in / check-out today)
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(240, 0));

        rightPanel.add(buildTodayCard("Today's Check-Ins",  bookingDAO.findTodaysCheckIns()));
        rightPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        rightPanel.add(buildTodayCard("Today's Check-Outs", bookingDAO.findTodaysCheckOuts()));

        content.add(tableCard,  BorderLayout.CENTER);
        content.add(rightPanel, BorderLayout.EAST);

        JPanel centre = new JPanel(new BorderLayout());
        centre.setOpaque(false);
        centre.add(statsRow, BorderLayout.NORTH);
        centre.add(content,  BorderLayout.CENTER);
        add(centre, BorderLayout.CENTER);
    }

    private JPanel buildTodayCard(String title, List<Booking> bookings) {
        JPanel card = UIUtils.card();
        card.setLayout(new BorderLayout(0, 8));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        card.add(UIUtils.subhead(title), BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        if (bookings.isEmpty()) {
            JLabel none = UIUtils.muted("None today");
            none.setAlignmentX(LEFT_ALIGNMENT);
            list.add(none);
        } else {
            for (Booking b : bookings) {
                JLabel row = new JLabel("• " + b.getCustomerName() + " — Room " + b.getRoomNumber());
                row.setFont(Theme.FONT_SMALL);
                row.setForeground(Theme.TEXT_PRIMARY);
                row.setAlignmentX(LEFT_ALIGNMENT);
                list.add(row);
                list.add(Box.createRigidArea(new Dimension(0, 4)));
            }
        }
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    public void refresh() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                int totalRooms     = roomDAO.findAll().size();
                int availRooms     = roomDAO.findAvailable().size();
                int totalCustomers = customerDAO.findAll().size();
                int activeBookings = bookingDAO.findActive().size();
                double todayRev    = billing.getDailyRevenue(LocalDate.now());

                SwingUtilities.invokeLater(() -> {
                    cardRooms.setValue(availRooms + " / " + totalRooms);
                    cardCustomers.setValue(String.valueOf(totalCustomers));
                    cardBookings.setValue(String.valueOf(activeBookings));
                    cardRevenue.setValue("₹" + String.format("%.0f", todayRev));
                    refreshTable();
                });
                return null;
            }
        };
        worker.execute();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Booking> bookings = bookingDAO.findAll();
        int limit = Math.min(bookings.size(), 20);
        for (int i = 0; i < limit; i++) {
            Booking b = bookings.get(i);
            tableModel.addRow(new Object[]{
                b.getBookingId(), b.getCustomerName(), b.getRoomNumber(),
                b.getRoomType(), b.getCheckIn(), b.getCheckOut(), b.getStatus()
            });
        }
    }
}
