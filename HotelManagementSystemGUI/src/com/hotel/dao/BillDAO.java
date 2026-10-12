package com.hotel.dao;

import com.hotel.exception.DatabaseException;
import com.hotel.model.Bill;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * BillDAO — CRUD operations for the {@code bills} table.
 */
public class BillDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    public Bill create(Bill bill) {
        String sql = "INSERT INTO bills (booking_id, total_amount, tax_amount, discount, net_amount, payment_status) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt   (1, bill.getBookingId());
            ps.setDouble(2, bill.getTotalAmount());
            ps.setDouble(3, bill.getTaxAmount());
            ps.setDouble(4, bill.getDiscount());
            ps.setDouble(5, bill.getNetAmount());
            ps.setString(6, bill.getPaymentStatus().name());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) bill.setBillId(keys.getInt(1));
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create bill: " + e.getMessage(), e);
        }
        return bill;
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    public Optional<Bill> findById(int billId) {
        String sql = buildDetailQuery("bl.bill_id = ?");
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, billId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Error finding bill: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<Bill> findByBookingId(int bookingId) {
        String sql = buildDetailQuery("bl.booking_id = ?");
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Error finding bill: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<Bill> findAll() {
        List<Bill> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(buildDetailQuery(null) + " ORDER BY bl.generated_at DESC")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching bills: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Bill> findPending() {
        List<Bill> list = new ArrayList<>();
        String sql = buildDetailQuery("bl.payment_status = 'PENDING'");
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching pending bills: " + e.getMessage(), e);
        }
        return list;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public boolean markPaid(int billId, Bill.PaymentMethod method) {
        String sql = "UPDATE bills SET payment_status='PAID', payment_method=?, paid_at=NOW() "
                   + "WHERE bill_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, method.name());
            ps.setInt   (2, billId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to mark bill as paid: " + e.getMessage(), e);
        }
    }

    public boolean updateDiscount(int billId, double discount) {
        String sql = "UPDATE bills SET discount=?, net_amount=(total_amount + tax_amount - ?) WHERE bill_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setDouble(1, discount);
            ps.setDouble(2, discount);
            ps.setInt   (3, billId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update discount: " + e.getMessage(), e);
        }
    }

    // ── Revenue reporting ──────────────────────────────────────────────────────

    /** Total revenue (net_amount of PAID bills) for a given date. */
    public double dailyRevenue(LocalDate date) {
        String sql = "SELECT COALESCE(SUM(net_amount), 0) FROM bills "
                   + "WHERE payment_status='PAID' AND DATE(paid_at) = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getDouble(1);
        } catch (SQLException e) {
            throw new DatabaseException("Error computing daily revenue: " + e.getMessage(), e);
        }
    }

    /** All paid bills between two dates (inclusive). */
    public List<Bill> findPaidBetween(LocalDate from, LocalDate to) {
        List<Bill> list = new ArrayList<>();
        String sql = buildDetailQuery("bl.payment_status='PAID' AND DATE(bl.paid_at) BETWEEN ? AND ?");
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching revenue report: " + e.getMessage(), e);
        }
        return list;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String buildDetailQuery(String where) {
        String sql = "SELECT bl.*, c.name AS customer_name, r.room_number "
                   + "FROM bills bl "
                   + "JOIN bookings b ON bl.booking_id = b.booking_id "
                   + "JOIN customers c ON b.customer_id = c.customer_id "
                   + "JOIN rooms r     ON b.room_id     = r.room_id";
        if (where != null && !where.isEmpty()) sql += " WHERE " + where;
        return sql;
    }

    private Bill mapRow(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setBillId       (rs.getInt   ("bill_id"));
        b.setBookingId    (rs.getInt   ("booking_id"));
        b.setTotalAmount  (rs.getDouble("total_amount"));
        b.setTaxAmount    (rs.getDouble("tax_amount"));
        b.setDiscount     (rs.getDouble("discount"));
        b.setNetAmount    (rs.getDouble("net_amount"));
        b.setPaymentStatus(Bill.PaymentStatus.valueOf(rs.getString("payment_status")));
        String pm = rs.getString("payment_method");
        if (pm != null) b.setPaymentMethod(Bill.PaymentMethod.valueOf(pm));
        Timestamp gen = rs.getTimestamp("generated_at");
        if (gen != null) b.setGeneratedAt(gen.toLocalDateTime());
        Timestamp paid = rs.getTimestamp("paid_at");
        if (paid != null) b.setPaidAt(paid.toLocalDateTime());
        b.setCustomerName(rs.getString("customer_name"));
        b.setRoomNumber  (rs.getString("room_number"));
        return b;
    }
}
