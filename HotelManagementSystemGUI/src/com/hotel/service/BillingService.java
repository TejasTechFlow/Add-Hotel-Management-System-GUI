package com.hotel.service;

import com.hotel.dao.BillDAO;
import com.hotel.dao.BookingDAO;
import com.hotel.exception.EntityNotFoundException;
import com.hotel.exception.ValidationException;
import com.hotel.model.Bill;
import com.hotel.model.Booking;

import java.time.LocalDate;
import java.util.List;

/**
 * BillingService — bill generation, payment processing and revenue queries.
 *
 * Tax rate: 9 % GST (configurable via TAX_RATE constant).
 */
public class BillingService {

    /** GST rate applied to all bills. */
    private static final double TAX_RATE = 0.09;

    private final BillDAO    billDAO;
    private final BookingDAO bookingDAO;

    public BillingService() {
        this.billDAO    = new BillDAO();
        this.bookingDAO = new BookingDAO();
    }

    // ── GENERATE ──────────────────────────────────────────────────────────────

    /**
     * Generates a bill for the given booking.
     * If a bill already exists, the existing bill is returned.
     *
     * @param bookingId   booking to bill
     * @param discountAmt flat discount amount (0 for none)
     * @return the created or existing Bill
     */
    public Bill generateBill(int bookingId, double discountAmt) {
        // Check for existing bill
        if (billDAO.findByBookingId(bookingId).isPresent()) {
            System.out.println("  [Info] Bill already exists for Booking #" + bookingId + ".");
            return billDAO.findByBookingId(bookingId).get();
        }

        Booking booking = bookingDAO.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking #" + bookingId + " not found."));

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED)
            throw new ValidationException("Cannot generate a bill for a cancelled booking.");

        if (discountAmt < 0)
            throw new ValidationException("Discount cannot be negative.");

        double subtotal = booking.getNights() * booking.getPricePerNight();
        double tax      = Math.round(subtotal * TAX_RATE * 100.0) / 100.0;

        if (discountAmt > subtotal)
            throw new ValidationException("Discount cannot exceed the subtotal of ₹" + subtotal + ".");

        Bill bill = new Bill(bookingId, subtotal, tax, discountAmt);
        return billDAO.create(bill);
    }

    // ── PAYMENT ───────────────────────────────────────────────────────────────

    /**
     * Records payment for a bill.
     *
     * @param billId  bill to pay
     * @param method  payment method chosen by the customer
     */
    public Bill processPayment(int billId, Bill.PaymentMethod method) {
        Bill bill = getBillById(billId);
        if (bill.getPaymentStatus() == Bill.PaymentStatus.PAID)
            throw new ValidationException("Bill #" + billId + " has already been paid.");
        if (bill.getPaymentStatus() == Bill.PaymentStatus.REFUNDED)
            throw new ValidationException("Bill #" + billId + " has been refunded — cannot accept payment.");

        billDAO.markPaid(billId, method);
        bill.setPaymentStatus(Bill.PaymentStatus.PAID);
        bill.setPaymentMethod(method);
        return bill;
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    public Bill getBillById(int billId) {
        return billDAO.findById(billId)
                .orElseThrow(() -> new EntityNotFoundException("Bill #" + billId + " not found."));
    }

    public Bill getBillByBooking(int bookingId) {
        return billDAO.findByBookingId(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("No bill found for Booking #" + bookingId + "."));
    }

    public List<Bill> getAllBills() {
        return billDAO.findAll();
    }

    public List<Bill> getPendingBills() {
        return billDAO.findPending();
    }

    // ── REPORTS ───────────────────────────────────────────────────────────────

    public double getDailyRevenue(LocalDate date) {
        return billDAO.dailyRevenue(date);
    }

    public List<Bill> getRevenueBetween(LocalDate from, LocalDate to) {
        if (to.isBefore(from))
            throw new ValidationException("End date must be on or after start date.");
        return billDAO.findPaidBetween(from, to);
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    /** Returns a formatted bill receipt string ready for console printing. */
    public String formatBillReceipt(Bill bill, Booking booking) {
        String line = "─".repeat(54);
        return "\n" + line + "\n"
             + "         HOTEL MANAGEMENT SYSTEM\n"
             + "               BILL RECEIPT\n"
             + line + "\n"
             + String.format("  Bill #       : %d%n",  bill.getBillId())
             + String.format("  Booking #    : %d%n",  bill.getBookingId())
             + String.format("  Customer     : %s%n",  bill.getCustomerName())
             + String.format("  Room         : %s (%s)%n", booking.getRoomNumber(), booking.getRoomType())
             + String.format("  Check-In     : %s%n",  booking.getCheckIn())
             + String.format("  Check-Out    : %s%n",  booking.getCheckOut())
             + String.format("  Nights       : %d%n",  booking.getNights())
             + String.format("  Rate/Night   : ₹%.2f%n", booking.getPricePerNight())
             + line + "\n"
             + String.format("  Subtotal     : ₹%.2f%n", bill.getTotalAmount())
             + String.format("  GST (9%%)     : ₹%.2f%n", bill.getTaxAmount())
             + String.format("  Discount     : ₹%.2f%n", bill.getDiscount())
             + String.format("  NET AMOUNT   : ₹%.2f%n", bill.getNetAmount())
             + line + "\n"
             + String.format("  Status       : %s%n",  bill.getPaymentStatus())
             + (bill.getPaymentMethod() != null
                ? String.format("  Method       : %s%n", bill.getPaymentMethod()) : "")
             + line + "\n";
    }
}
