package com.hotel.ui.panels;

import com.hotel.model.*;
import com.hotel.service.BillingService;
import com.hotel.service.BookingService;
import com.hotel.ui.components.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * BillingPanel — generate bills, process payments, view all bills.
 */
public class BillingPanel extends JPanel {

    private final JFrame          owner;
    private final User            currentUser;
    private final BillingService  billingService  = new BillingService();
    private final BookingService  bookingService  = new BookingService();

    private JTable            table;
    private DefaultTableModel model;
    private JComboBox<String> filterPay;

    public BillingPanel(JFrame owner, User currentUser) {
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
        header.add(UIUtils.title("Billing System"), BorderLayout.WEST);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acts.setOpaque(false);

        JButton genBtn = UIUtils.primaryBtn("Generate Bill");
        genBtn.addActionListener(e -> generateBill());

        JButton payBtn = UIUtils.successBtn("Process Payment");
        payBtn.addActionListener(e -> processPayment());

        filterPay = UIUtils.comboBox("All", "PENDING", "PAID", "PARTIALLY_PAID", "REFUNDED");
        filterPay.setPreferredSize(new Dimension(150, 34));
        filterPay.addActionListener(e -> loadData());

        acts.add(filterPay);
        acts.add(genBtn);
        acts.add(payBtn);
        header.add(acts, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"Bill#", "Booking#", "Customer", "Room", "Subtotal", "Tax", "Discount", "Net Amount", "Status", "Method"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        UIUtils.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(65);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(65);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);
        table.getColumnModel().getColumn(8).setPreferredWidth(110);
        table.getColumnModel().getColumn(9).setPreferredWidth(110);

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
        String filter = (String) filterPay.getSelectedItem();
        List<Bill> bills = billingService.getAllBills();
        for (Bill b : bills) {
            if (!"All".equals(filter) && !b.getPaymentStatus().name().equals(filter)) continue;
            model.addRow(new Object[]{
                b.getBillId(), b.getBookingId(), b.getCustomerName(),
                b.getRoomNumber(),
                String.format("₹%.2f", b.getTotalAmount()),
                String.format("₹%.2f", b.getTaxAmount()),
                String.format("₹%.2f", b.getDiscount()),
                String.format("₹%.2f", b.getNetAmount()),
                b.getPaymentStatus().name(),
                b.getPaymentMethod() != null ? b.getPaymentMethod().name() : "—"
            });
        }
    }

    private void generateBill() {
        String input = JOptionPane.showInputDialog(owner,
            "Enter Booking ID to generate bill:", "Generate Bill", JOptionPane.PLAIN_MESSAGE);
        if (input == null || input.isBlank()) return;
        try {
            int bookingId = Integer.parseInt(input.trim());
            String discInput = JOptionPane.showInputDialog(owner,
                "Enter discount amount (₹) or 0:", "Discount", JOptionPane.PLAIN_MESSAGE);
            double discount = discInput != null && !discInput.isBlank()
                              ? Double.parseDouble(discInput.trim()) : 0.0;

            Bill bill       = billingService.generateBill(bookingId, discount);
            Booking booking = bookingService.getById(bookingId);
            String receipt  = billingService.formatBillReceipt(bill, booking);

            showReceipt(receipt, bill);
            loadData();
        } catch (NumberFormatException e) {
            UIUtils.toast(owner, "Invalid input — enter a number.", false);
        } catch (Exception e) {
            UIUtils.toast(owner, e.getMessage(), false);
        }
    }

    private void processPayment() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtils.toast(owner, "Select a bill row first.", false);
            return;
        }
        int billId = (int) model.getValueAt(row, 0);
        String currentStatus = (String) model.getValueAt(row, 8);
        if ("PAID".equals(currentStatus)) {
            UIUtils.toast(owner, "Bill #" + billId + " is already paid.", false);
            return;
        }

        String[] methods = {"CASH", "CARD", "UPI", "NET_BANKING"};
        String chosen = (String) JOptionPane.showInputDialog(owner,
            "Select payment method for Bill #" + billId + ":",
            "Process Payment", JOptionPane.PLAIN_MESSAGE, null, methods, methods[0]);
        if (chosen == null) return;

        try {
            Bill.PaymentMethod method = Bill.PaymentMethod.valueOf(chosen);
            billingService.processPayment(billId, method);
            loadData();
            UIUtils.toast(owner, "Payment recorded via " + chosen + "!", true);
        } catch (Exception ex) {
            UIUtils.toast(owner, ex.getMessage(), false);
        }
    }

    private void showReceipt(String receipt, Bill bill) {
        JTextArea area = new JTextArea(receipt);
        area.setFont(Theme.FONT_MONO);
        area.setEditable(false);
        area.setBackground(new Color(248, 250, 252));
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(480, 380));

        JDialog dlg = new JDialog(owner, "Bill #" + bill.getBillId(), true);
        dlg.setLayout(new BorderLayout(0, 12));
        dlg.add(sp, BorderLayout.CENTER);

        JButton closeBtn = UIUtils.primaryBtn("Close");
        closeBtn.addActionListener(e -> dlg.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setBackground(Theme.BG_CARD);
        bottom.add(closeBtn);
        dlg.add(bottom, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
    }
}
