package com.hotel.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Booking — represents a room reservation.
 * Contains denormalised display fields (customer name, room number) for convenience.
 */
public class Booking {

    public enum BookingStatus { CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED }

    private int           bookingId;
    private int           customerId;
    private int           roomId;
    private LocalDate     checkIn;
    private LocalDate     checkOut;
    private BookingStatus status;
    private int           bookedBy;        // user_id
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Denormalised display helpers (populated by DAO joins)
    private String customerName;
    private String roomNumber;
    private String roomType;
    private double pricePerNight;

    // ── Constructors ─────────────────────────────────────────────────────────

    public Booking() {}

    public Booking(int customerId, int roomId, LocalDate checkIn, LocalDate checkOut, int bookedBy) {
        this.customerId = customerId;
        this.roomId     = roomId;
        this.checkIn    = checkIn;
        this.checkOut   = checkOut;
        this.bookedBy   = bookedBy;
        this.status     = BookingStatus.CONFIRMED;
    }

    // ── Business helpers ──────────────────────────────────────────────────────

    /** Number of nights between check-in and check-out. */
    public long getNights() {
        if (checkIn == null || checkOut == null) return 0;
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    /** Estimated total before tax, based on pricePerNight. */
    public double calculateSubtotal() {
        return getNights() * pricePerNight;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int           getBookingId()             { return bookingId; }
    public void          setBookingId(int v)        { this.bookingId = v; }

    public int           getCustomerId()            { return customerId; }
    public void          setCustomerId(int v)       { this.customerId = v; }

    public int           getRoomId()                { return roomId; }
    public void          setRoomId(int v)           { this.roomId = v; }

    public LocalDate     getCheckIn()               { return checkIn; }
    public void          setCheckIn(LocalDate v)    { this.checkIn = v; }

    public LocalDate     getCheckOut()              { return checkOut; }
    public void          setCheckOut(LocalDate v)   { this.checkOut = v; }

    public BookingStatus getStatus()                    { return status; }
    public void          setStatus(BookingStatus v)     { this.status = v; }

    public int           getBookedBy()              { return bookedBy; }
    public void          setBookedBy(int v)         { this.bookedBy = v; }

    public LocalDateTime getCreatedAt()                  { return createdAt; }
    public void          setCreatedAt(LocalDateTime v)   { this.createdAt = v; }

    public LocalDateTime getUpdatedAt()                  { return updatedAt; }
    public void          setUpdatedAt(LocalDateTime v)   { this.updatedAt = v; }

    public String        getCustomerName()            { return customerName; }
    public void          setCustomerName(String v)    { this.customerName = v; }

    public String        getRoomNumber()              { return roomNumber; }
    public void          setRoomNumber(String v)      { this.roomNumber = v; }

    public String        getRoomType()                { return roomType; }
    public void          setRoomType(String v)        { this.roomType = v; }

    public double        getPricePerNight()           { return pricePerNight; }
    public void          setPricePerNight(double v)   { this.pricePerNight = v; }

    @Override
    public String toString() {
        return String.format(
            "[Booking #%d] %s | Room %s (%s) | %s → %s | %d nights | %s",
            bookingId, customerName, roomNumber, roomType,
            checkIn, checkOut, getNights(), status);
    }
}
