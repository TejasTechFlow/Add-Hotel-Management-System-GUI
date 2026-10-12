package com.hotel.dao;

import com.hotel.exception.DatabaseException;
import com.hotel.model.Room;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * RoomDAO — CRUD operations for the {@code rooms} table.
 */
public class RoomDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    public Room create(Room room) {
        String sql = "INSERT INTO rooms (room_number, room_type, price_per_night, floor_number, description) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType().name());
            ps.setDouble(3, room.getPricePerNight());
            ps.setInt   (4, room.getFloorNumber());
            ps.setString(5, room.getDescription());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) room.setRoomId(keys.getInt(1));
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add room: " + e.getMessage(), e);
        }
        return room;
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    public Optional<Room> findById(int roomId) {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching room: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<Room> findByRoomNumber(String roomNumber) {
        String sql = "SELECT * FROM rooms WHERE room_number = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, roomNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching room: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<Room> findAll() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms ORDER BY floor_number, room_number";
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching rooms: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Room> findAvailable() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE status = 'AVAILABLE' ORDER BY room_type, price_per_night";
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching available rooms: " + e.getMessage(), e);
        }
        return list;
    }

    /** Returns available rooms of a specific type. */
    public List<Room> findAvailableByType(Room.RoomType type) {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE status='AVAILABLE' AND room_type=? ORDER BY price_per_night";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, type.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching rooms by type: " + e.getMessage(), e);
        }
        return list;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public boolean update(Room room) {
        String sql = "UPDATE rooms SET room_type=?, price_per_night=?, floor_number=?, description=? "
                   + "WHERE room_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, room.getRoomType().name());
            ps.setDouble(2, room.getPricePerNight());
            ps.setInt   (3, room.getFloorNumber());
            ps.setString(4, room.getDescription());
            ps.setInt   (5, room.getRoomId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update room: " + e.getMessage(), e);
        }
    }

    public boolean updateStatus(int roomId, Room.RoomStatus status) {
        String sql = "UPDATE rooms SET status=? WHERE room_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt   (2, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update room status: " + e.getMessage(), e);
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    public boolean delete(int roomId) {
        String sql = "DELETE FROM rooms WHERE room_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete room (may have bookings): " + e.getMessage(), e);
        }
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private Room mapRow(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setRoomId       (rs.getInt   ("room_id"));
        r.setRoomNumber   (rs.getString("room_number"));
        r.setRoomType     (Room.RoomType.valueOf(rs.getString("room_type")));
        r.setPricePerNight(rs.getDouble ("price_per_night"));
        r.setStatus       (Room.RoomStatus.valueOf(rs.getString("status")));
        r.setFloorNumber  (rs.getInt   ("floor_number"));
        r.setDescription  (rs.getString("description"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) r.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) r.setUpdatedAt(updated.toLocalDateTime());
        return r;
    }
}
