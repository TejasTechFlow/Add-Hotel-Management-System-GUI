package com.hotel.dao;

import com.hotel.exception.DatabaseException;
import com.hotel.model.User;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * UserDAO — Data Access Object for the {@code users} table.
 * All SQL interactions for user management live here.
 */
public class UserDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Inserts a new user and sets the generated user_id on the object.
     *
     * @param user User with passwordHash already set (SHA-256 hex)
     * @return the saved User with its DB-assigned ID
     */
    public User create(User user) {
        String sql = "INSERT INTO users (username, password, role, full_name, email, phone) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole().name());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getPhone());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) user.setUserId(keys.getInt(1));
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create user: " + e.getMessage(), e);
        }
        return user;
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    /** Finds a user by username. Returns empty Optional if not found. */
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = 1";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    /** Returns all active users. */
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE is_active = 1 ORDER BY user_id";
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching users: " + e.getMessage(), e);
        }
        return list;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    /** Updates full_name, email, phone and role of an existing user. */
    public boolean update(User user) {
        String sql = "UPDATE users SET full_name=?, email=?, phone=?, role=? WHERE user_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getRole().name());
            ps.setInt   (5, user.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update user: " + e.getMessage(), e);
        }
    }

    /** Updates password hash for the given user_id. */
    public boolean updatePassword(int userId, String newHash) {
        String sql = "UPDATE users SET password=? WHERE user_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt   (2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update password: " + e.getMessage(), e);
        }
    }

    // ── DELETE (soft) ─────────────────────────────────────────────────────────

    /** Soft-deletes a user by setting is_active = 0. */
    public boolean delete(int userId) {
        String sql = "UPDATE users SET is_active=0 WHERE user_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete user: " + e.getMessage(), e);
        }
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId      (rs.getInt   ("user_id"));
        u.setUsername    (rs.getString("username"));
        u.setPasswordHash(rs.getString("password"));
        u.setRole        (User.Role.valueOf(rs.getString("role")));
        u.setFullName    (rs.getString("full_name"));
        u.setEmail       (rs.getString("email"));
        u.setPhone       (rs.getString("phone"));
        u.setActive      (rs.getBoolean("is_active"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime());
        return u;
    }
}
