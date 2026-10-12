package com.hotel.dao;

import com.hotel.exception.DatabaseException;
import com.hotel.model.Customer;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CustomerDAO — CRUD operations for the {@code customers} table.
 */
public class CustomerDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    public Customer create(Customer c) {
        String sql = "INSERT INTO customers (name, contact, email, address, id_proof, id_number) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getContact());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getAddress());
            ps.setString(5, c.getIdProof());
            ps.setString(6, c.getIdNumber());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) c.setCustomerId(keys.getInt(1));
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add customer: " + e.getMessage(), e);
        }
        return c;
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    public Optional<Customer> findById(int customerId) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Error finding customer: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<Customer> findAll() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY name";
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching customers: " + e.getMessage(), e);
        }
        return list;
    }

    /** Search by name or contact (LIKE). */
    public List<Customer> search(String keyword) {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE name LIKE ? OR contact LIKE ? OR email LIKE ?";
        String k   = "%" + keyword + "%";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, k);
            ps.setString(2, k);
            ps.setString(3, k);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Error searching customers: " + e.getMessage(), e);
        }
        return list;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public boolean update(Customer c) {
        String sql = "UPDATE customers SET name=?, contact=?, email=?, address=?, id_proof=?, id_number=? "
                   + "WHERE customer_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getContact());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getAddress());
            ps.setString(5, c.getIdProof());
            ps.setString(6, c.getIdNumber());
            ps.setInt   (7, c.getCustomerId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update customer: " + e.getMessage(), e);
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    public boolean delete(int customerId) {
        String sql = "DELETE FROM customers WHERE customer_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Cannot delete customer (may have bookings): " + e.getMessage(), e);
        }
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt   ("customer_id"));
        c.setName      (rs.getString("name"));
        c.setContact   (rs.getString("contact"));
        c.setEmail     (rs.getString("email"));
        c.setAddress   (rs.getString("address"));
        c.setIdProof   (rs.getString("id_proof"));
        c.setIdNumber  (rs.getString("id_number"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
        return c;
    }
}
