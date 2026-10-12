package com.hotel.ui.dialogs;

import com.hotel.model.*;
import com.hotel.service.BookingService;
import com.hotel.service.CustomerService;
import com.hotel.service.RoomService;
import com.hotel.ui.components.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * BookingDialog — new booking form.
 * Shows available rooms and customer list via combo boxes.
 */
public class BookingDialog extends JDialog {

    private final User           currentUser;
    private final BookingService bookingService  = new BookingService();
    private final CustomerService customerService = new CustomerService();
    private final RoomService    roomService     = new RoomService();
    private boolean saved = false;

    private JComboBox<String> customerCombo;
    private JComboBox<String> roomCombo;
    private JSpinner          checkInSpinner, checkOutSpinner;

    private List<Customer> customers;
    private List<Room>     rooms;

    public BookingDialog(JFrame owner, User user) {
        super(owner, "New Booking", true);
        this.currentUser = user;
        buildUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Theme.BG_CARD);
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        root.add(UIUtils.heading("Create New Booking"), BorderLayout.NORTH);

        // Load customers and available rooms
        customers = customerService.getAllCustomers();
        rooms     = roomService.getAvailableRooms();

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 12));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));

        // Customer combo
        customerCombo = new JComboBox<>();
        customerCombo.setFont(Theme.FONT_INPUT);
        customerCombo.setPreferredSize(new Dimension(260, 36));
        for (Customer c : customers) {
            customerCombo.addItem("#" + c.getCustomerId() + " — " + c.getName() + " (" + c.getContact() + ")");
        }
        if (customers.isEmpty()) customerCombo.addItem("No customers found — add one first");

        // Room combo
        roomCombo = new JComboBox<>();
        roomCombo.setFont(Theme.FONT_INPUT);
        roomCombo.setPreferredSize(new Dimension(260, 36));
        for (Room r : rooms) {
            roomCombo.addItem("#" + r.getRoomId() + " — Room " + r.getRoomNumber()
                    + " (" + r.getRoomType() + ") ₹" + r.getPricePerNight() + "/night");
        }
        if (rooms.isEmpty()) roomCombo.addItem("No rooms available");

        // Date spinners
        checkInSpinner  = buildDateSpinner(LocalDate.now());
        checkOutSpinner = buildDateSpinner(LocalDate.now().plusDays(1));

        form.add(UIUtils.formRow("Customer *",    customerCombo));
        form.add(UIUtils.formRow("Room *",        roomCombo));
        form.add(UIUtils.formRow("Check-In *",    checkInSpinner));
        form.add(UIUtils.formRow("Check-Out *",   checkOutSpinner));

        // Price summary label
        JLabel priceLbl = UIUtils.muted("Select room to see estimated cost");
        roomCombo.addActionListener(e -> updatePriceLabel(priceLbl));
        updatePriceLabel(priceLbl);

        JPanel priceRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        priceRow.setOpaque(false);
        priceRow.add(priceLbl);
        form.add(priceRow);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        JButton cancelBtn = UIUtils.outlineBtn("Cancel");
        JButton saveBtn   = UIUtils.primaryBtn("Confirm Booking");
        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> save());
        if (customers.isEmpty() || rooms.isEmpty()) saveBtn.setEnabled(false);
        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);

        root.add(form,   BorderLayout.CENTER);
        root.add(btnRow, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JSpinner buildDateSpinner(LocalDate initial) {
        JSpinner sp = new JSpinner(new SpinnerDateModel());
        sp.setEditor(new JSpinner.DateEditor(sp, "yyyy-MM-dd"));
        sp.setFont(Theme.FONT_INPUT);
        sp.setPreferredSize(new Dimension(260, 36));
        sp.setValue(java.util.Date.from(initial.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        return sp;
    }

    private LocalDate getDate(JSpinner sp) {
        java.util.Date d = (java.util.Date) sp.getValue();
        return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    private void updatePriceLabel(JLabel lbl) {
        int idx = roomCombo.getSelectedIndex();
        if (idx < 0 || idx >= rooms.size()) return;
        double price = rooms.get(idx).getPricePerNight();
        LocalDate ci = getDate(checkInSpinner);
        LocalDate co = getDate(checkOutSpinner);
        if (co.isAfter(ci)) {
            long nights   = java.time.temporal.ChronoUnit.DAYS.between(ci, co);
            double subtot = nights * price;
            double tax    = subtot * 0.09;
            lbl.setText(String.format("Est. %d nights × ₹%.2f = ₹%.2f  (+9%% GST ₹%.2f = ₹%.2f)",
                    nights, price, subtot, tax, subtot + tax));
        } else {
            lbl.setText("Check-out must be after check-in.");
        }
    }

    private void save() {
        try {
            int custIdx = customerCombo.getSelectedIndex();
            int roomIdx = roomCombo.getSelectedIndex();
            if (custIdx < 0 || roomIdx < 0) return;

            int customerId = customers.get(custIdx).getCustomerId();
            int roomId     = rooms.get(roomIdx).getRoomId();
            LocalDate ci   = getDate(checkInSpinner);
            LocalDate co   = getDate(checkOutSpinner);

            bookingService.createBooking(customerId, roomId, ci, co, currentUser.getUserId());
            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Booking Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
}
