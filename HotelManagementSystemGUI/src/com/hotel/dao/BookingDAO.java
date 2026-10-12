package com.hotel.dao;

import com.hotel.exception.DatabaseException;
import com.hotel.model.Booking;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * BookingDAO — CRUD + business queries for the {@code bookings} table.
 */
public class BookingDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    public Booking create(Booking b) {
        String sql = "INSERT INTO bookings (customer_id, room_id, check_in, check_out, status, booked_by) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt   (1, b.getCustomerId());
            ps.setInt   (2, b.getRoomId());
            ps.setDate  (3, Date.valueOf(b.getCheckIn()));
            ps.setDate  (4, Date.valueOf(b.getCheckOut()));
            ps.setString(5, b.getStatus().name());
            ps.setInt   (6, b.getBookedBy());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) b.setBookingId(keys.getInt(1));
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create booking: " + e.getMessage(), e);
        }
        return b;
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    /** Fetch a single booking with customer and room info joined. */
    public Optional<Booking> findById(int bookingId) {
        String sql = buildDetailQuery("b.booking_id = ?");
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapDetailRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Error finding booking: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<Booking> findAll() {
        List<Booking> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(buildDetailQuery(null) + " ORDER BY b.check_in DESC")) {
            while (rs.next()) list.add(mapDetailRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching bookings: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Booking> findByCustomer(int customerId) {
        return fetchWithFilter("b.customer_id = ?", customerId);
    }

    public List<Booking> findActive() {
        List<Booking> list = new ArrayList<>();
        String sql = buildDetailQuery("b.status IN ('CONFIRMED','CHECKED_IN')") + " ORDER BY b.check_in";
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapDetailRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching active bookings: " + e.getMessage(), e);
        }
        return list;
    }

    /** Today's check-ins. */
    public List<Booking> findTodaysCheckIns() {
        return fetchWithDateFilter("b.check_in", LocalDate.now());
    }

    /** Today's check-outs. */
    public List<Booking> findTodaysCheckOuts() {
        return fetchWithDateFilter("b.check_out", LocalDate.now());
    }

    /**
     * Returns true if the given room has an active booking that overlaps
     * with [checkIn, checkOut).
     */
    public boolean isRoomAvailable(int roomId, LocalDate checkIn, LocalDate checkOut) {
        String sql = "SELECT COUNT(*) FROM bookings "
                   + "WHERE room_id = ? AND status IN ('CONFIRMED','CHECKED_IN') "
                   + "AND NOT (check_out <= ? OR check_in >= ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt  (1, roomId);
            ps.setDate (2, Date.valueOf(checkIn));
            ps.setDate (3, Date.valueOf(checkOut));
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) == 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error checking availability: " + e.getMessage(), e);
        }
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public boolean updateStatus(int bookingId, Booking.BookingStatus status) {
        String sql = "UPDATE bookings SET status=? WHERE booking_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt   (2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update booking status: " + e.getMessage(), e);
        }
    }

    public boolean update(Booking b) {
        String sql = "UPDATE bookings SET check_in=?, check_out=? WHERE booking_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(b.getCheckIn()));
            ps.setDate(2, Date.valueOf(b.getCheckOut()));
            ps.setInt (3, b.getBookingId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update booking: " + e.getMessage(), e);
        }
    }

    // ── Reports ───────────────────────────────────────────────────────────────

    /** All bookings created / checked-in on a specific date. */
    public List<Booking> findByDate(LocalDate date) {
        return fetchWithDateFilter("DATE(b.created_at)", date);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String buildDetailQuery(String whereClause) {
        String sql = "SELECT b.*, c.name AS customer_name, c.contact, "
                   + "r.room_number, r.room_type, r.price_per_night "
                   + "FROM bookings b "
                   + "JOIN customers c ON b.customer_id = c.customer_id "
                   + "JOIN rooms r     ON b.room_id     = r.room_id";
        if (whereClause != null && !whereClause.isEmpty()) sql += " WHERE " + whereClause;
        return sql;
    }

    private List<Booking> fetchWithFilter(String col, int id) {
        List<Booking> list = new ArrayList<>();
        String sql = buildDetailQuery(col + " = ?") + " ORDER BY b.check_in DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapDetailRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching bookings: " + e.getMessage(), e);
        }
        return list;
    }

    private List<Booking> fetchWithDateFilter(String dateCol, LocalDate date) {
        List<Booking> list = new ArrayList<>();
        String sql = buildDetailQuery(dateCol + " = ?") + " ORDER BY b.booking_id";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapDetailRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching bookings by date: " + e.getMessage(), e);
        }
        return list;
    }

    private Booking mapDetailRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId  (rs.getInt   ("booking_id"));
        b.setCustomerId (rs.getInt   ("customer_id"));
        b.setRoomId     (rs.getInt   ("room_id"));
        b.setCheckIn    (rs.getDate  ("check_in").toLocalDate());
        b.setCheckOut   (rs.getDate  ("check_out").toLocalDate());
        b.setStatus     (Booking.BookingStatus.valueOf(rs.getString("status")));
        b.setBookedBy   (rs.getInt   ("booked_by"));
        b.setCustomerName(rs.getString("customer_name"));
        b.setRoomNumber  (rs.getString("room_number"));
        b.setRoomType    (rs.getString("room_type"));
        b.setPricePerNight(rs.getDouble("price_per_night"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) b.setCreatedAt(ts.toLocalDateTime());
        return b;
    }
}
