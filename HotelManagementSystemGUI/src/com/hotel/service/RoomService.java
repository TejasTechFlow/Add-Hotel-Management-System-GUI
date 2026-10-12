package com.hotel.service;

import com.hotel.dao.RoomDAO;
import com.hotel.exception.EntityNotFoundException;
import com.hotel.exception.ValidationException;
import com.hotel.model.Room;

import java.util.List;

/**
 * RoomService — business logic layer for room management.
 * Validates input before delegating to RoomDAO.
 */
public class RoomService {

    private final RoomDAO roomDAO;

    public RoomService() {
        this.roomDAO = new RoomDAO();
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    public Room addRoom(String roomNumber, Room.RoomType type, double price,
                        int floor, String description) {
        if (roomNumber == null || roomNumber.isBlank())
            throw new ValidationException("Room number cannot be empty.");
        if (price <= 0)
            throw new ValidationException("Price must be greater than zero.");
        if (floor <= 0)
            throw new ValidationException("Floor number must be positive.");
        if (roomDAO.findByRoomNumber(roomNumber).isPresent())
            throw new ValidationException("Room number '" + roomNumber + "' already exists.");

        Room room = new Room(roomNumber.toUpperCase().trim(), type, price, floor, description);
        return roomDAO.create(room);
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    public Room getById(int roomId) {
        return roomDAO.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room #" + roomId + " not found."));
    }

    public List<Room> getAllRooms() {
        return roomDAO.findAll();
    }

    public List<Room> getAvailableRooms() {
        return roomDAO.findAvailable();
    }

    public List<Room> getAvailableByType(Room.RoomType type) {
        return roomDAO.findAvailableByType(type);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public boolean updateRoom(Room room) {
        if (room.getPricePerNight() <= 0)
            throw new ValidationException("Price must be greater than zero.");
        return roomDAO.update(room);
    }

    public boolean setRoomStatus(int roomId, Room.RoomStatus status) {
        getById(roomId); // verify exists
        return roomDAO.updateStatus(roomId, status);
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    public boolean deleteRoom(int roomId) {
        Room room = getById(roomId);
        if (room.getStatus() == Room.RoomStatus.BOOKED)
            throw new ValidationException("Cannot delete a booked room. Cancel bookings first.");
        return roomDAO.delete(roomId);
    }
}
