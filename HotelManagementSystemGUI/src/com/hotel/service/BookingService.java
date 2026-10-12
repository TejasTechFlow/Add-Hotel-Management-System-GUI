package com.hotel.service;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.exception.EntityNotFoundException;
import com.hotel.exception.ValidationException;
import com.hotel.model.Booking;
import com.hotel.model.Room;

import java.time.LocalDate;
import java.util.List;

/**
 * BookingService — orchestrates room booking, check-in, check-out and cancellation.
 *
 * Demonstrates Polymorphism: BookingStatus transitions are handled via
 * overloaded business methods rather than direct DAO calls from the UI.
 */
public class BookingService {

    private final BookingDAO bookingDAO;
    private final RoomDAO    roomDAO;

    public BookingService() {
        this.bookingDAO = new BookingDAO();
        this.roomDAO    = new RoomDAO();
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Creates a new booking after validating dates and room availability.
     *
     * @param customerId  ID of the customer
     * @param roomId      ID of the room to book
     * @param checkIn     check-in date (must be today or future)
     * @param checkOut    check-out date (must be after checkIn)
     * @param bookedBy    user_id of the staff creating the booking
     * @return saved Booking
     */
    public Booking createBooking(int customerId, int roomId,
                                  LocalDate checkIn, LocalDate checkOut,
                                  int bookedBy) {
        // Validate dates
        if (checkIn == null || checkOut == null)
            throw new ValidationException("Check-in and check-out dates are required.");
        if (!checkOut.isAfter(checkIn))
            throw new ValidationException("Check-out must be after check-in date.");
        if (checkIn.isBefore(LocalDate.now()))
            throw new ValidationException("Check-in date cannot be in the past.");

        // Validate room exists and is physically available
        Room room = roomDAO.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room #" + roomId + " not found."));
        if (room.getStatus() == Room.RoomStatus.MAINTENANCE)
            throw new ValidationException("Room " + room.getRoomNumber() + " is under maintenance.");

        // Check for date-range conflicts
        if (!bookingDAO.isRoomAvailable(roomId, checkIn, checkOut))
            throw new ValidationException("Room " + room.getRoomNumber()
                    + " is not available for the selected dates.");

        // Persist booking
        Booking booking = new Booking(customerId, roomId, checkIn, checkOut, bookedBy);
        bookingDAO.create(booking);

        // Mark room as BOOKED
        roomDAO.updateStatus(roomId, Room.RoomStatus.BOOKED);

        return booking;
    }

    // ── CHECK-IN ──────────────────────────────────────────────────────────────

    /**
     * Performs check-in: transitions booking to CHECKED_IN.
     * Only CONFIRMED bookings can be checked in.
     */
    public Booking checkIn(int bookingId) {
        Booking booking = getById(bookingId);
        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED)
            throw new ValidationException("Only CONFIRMED bookings can be checked in. "
                    + "Current status: " + booking.getStatus());

        bookingDAO.updateStatus(bookingId, Booking.BookingStatus.CHECKED_IN);
        booking.setStatus(Booking.BookingStatus.CHECKED_IN);
        return booking;
    }

    // ── CHECK-OUT ─────────────────────────────────────────────────────────────

    /**
     * Performs check-out: transitions booking to CHECKED_OUT and frees the room.
     */
    public Booking checkOut(int bookingId) {
        Booking booking = getById(bookingId);
        if (booking.getStatus() != Booking.BookingStatus.CHECKED_IN)
            throw new ValidationException("Only CHECKED_IN bookings can be checked out. "
                    + "Current status: " + booking.getStatus());

        bookingDAO.updateStatus(bookingId, Booking.BookingStatus.CHECKED_OUT);
        roomDAO.updateStatus(booking.getRoomId(), Room.RoomStatus.AVAILABLE);
        booking.setStatus(Booking.BookingStatus.CHECKED_OUT);
        return booking;
    }

    // ── CANCEL ────────────────────────────────────────────────────────────────

    /**
     * Cancels a booking and frees the room if it was in CONFIRMED or CHECKED_IN state.
     */
    public Booking cancelBooking(int bookingId) {
        Booking booking = getById(bookingId);
        if (booking.getStatus() == Booking.BookingStatus.CHECKED_OUT)
            throw new ValidationException("Cannot cancel a booking that has already been checked out.");
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED)
            throw new ValidationException("Booking is already cancelled.");

        bookingDAO.updateStatus(bookingId, Booking.BookingStatus.CANCELLED);
        roomDAO.updateStatus(booking.getRoomId(), Room.RoomStatus.AVAILABLE);
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        return booking;
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    public Booking getById(int bookingId) {
        return bookingDAO.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking #" + bookingId + " not found."));
    }

    public List<Booking> getAllBookings() {
        return bookingDAO.findAll();
    }

    public List<Booking> getActiveBookings() {
        return bookingDAO.findActive();
    }

    public List<Booking> getBookingsByCustomer(int customerId) {
        return bookingDAO.findByCustomer(customerId);
    }

    public List<Booking> getTodaysCheckIns() {
        return bookingDAO.findTodaysCheckIns();
    }

    public List<Booking> getTodaysCheckOuts() {
        return bookingDAO.findTodaysCheckOuts();
    }

    public List<Booking> getBookingsByDate(LocalDate date) {
        return bookingDAO.findByDate(date);
    }

    /**
     * Checks whether a room is available for the given date range.
     */
    public boolean checkAvailability(int roomId, LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn))
            throw new ValidationException("Check-out must be after check-in.");
        Room room = roomDAO.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room #" + roomId + " not found."));
        if (room.getStatus() == Room.RoomStatus.MAINTENANCE) return false;
        return bookingDAO.isRoomAvailable(roomId, checkIn, checkOut);
    }
}
