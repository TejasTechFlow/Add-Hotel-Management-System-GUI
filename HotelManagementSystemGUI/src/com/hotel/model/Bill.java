package com.hotel.model;

import java.time.LocalDateTime;

/**
 * Bill — represents a financial record tied to a booking.
 */
public class Bill {

    public enum PaymentStatus { PENDING, PAID, PARTIALLY_PAID, REFUNDED }
    public enum PaymentMethod { CASH, CARD, UPI, NET_BANKING }

    private int           billId;
    private int           bookingId;
    private double        totalAmount;    // subtotal (rooms × nights)
    private double        taxAmount;      // GST / service tax
    private double        discount;
    private double        netAmount;      // totalAmount + taxAmount - discount
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private LocalDateTime generatedAt;
    private LocalDateTime paidAt;

    // Denormalised display helpers
    private String customerName;
    private String roomNumber;

    // ── Constructors ─────────────────────────────────────────────────────────

    public Bill() {}

    public Bill(int bookingId, double totalAmount, double taxAmount, double discount) {
        this.bookingId    = bookingId;
        this.totalAmount  = totalAmount;
        this.taxAmount    = taxAmount;
        this.discount     = discount;
        this.netAmount    = totalAmount + taxAmount - discount;
        this.paymentStatus = PaymentStatus.PENDING;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int           getBillId()                  { return billId; }
    public void          setBillId(int v)             { this.billId = v; }

    public int           getBookingId()               { return bookingId; }
    public void          setBookingId(int v)          { this.bookingId = v; }

    public double        getTotalAmount()             { return totalAmount; }
    public void          setTotalAmount(double v)     { this.totalAmount = v; }

    public double        getTaxAmount()               { return taxAmount; }
    public void          setTaxAmount(double v)       { this.taxAmount = v; }

    public double        getDiscount()                { return discount; }
    public void          setDiscount(double v)        { this.discount = v; }

    public double        getNetAmount()               { return netAmount; }
    public void          setNetAmount(double v)       { this.netAmount = v; }

    public PaymentStatus getPaymentStatus()                    { return paymentStatus; }
    public void          setPaymentStatus(PaymentStatus v)     { this.paymentStatus = v; }

    public PaymentMethod getPaymentMethod()                    { return paymentMethod; }
    public void          setPaymentMethod(PaymentMethod v)     { this.paymentMethod = v; }

    public LocalDateTime getGeneratedAt()                      { return generatedAt; }
    public void          setGeneratedAt(LocalDateTime v)       { this.generatedAt = v; }

    public LocalDateTime getPaidAt()                           { return paidAt; }
    public void          setPaidAt(LocalDateTime v)            { this.paidAt = v; }

    public String        getCustomerName()               { return customerName; }
    public void          setCustomerName(String v)       { this.customerName = v; }

    public String        getRoomNumber()                 { return roomNumber; }
    public void          setRoomNumber(String v)         { this.roomNumber = v; }

    @Override
    public String toString() {
        return String.format("[Bill #%d] Booking #%d | Total ₹%.2f | Tax ₹%.2f | Net ₹%.2f | %s",
                billId, bookingId, totalAmount, taxAmount, netAmount, paymentStatus);
    }
}
