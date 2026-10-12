package com.hotel.model;

import java.time.LocalDateTime;

/**
 * Room — represents a hotel room entity.
 * Demonstrates Encapsulation.
 */
public class Room {

    public enum RoomType   { SINGLE, DOUBLE, SUITE, DELUXE }
    public enum RoomStatus { AVAILABLE, BOOKED, MAINTENANCE }

    private int           roomId;
    private String        roomNumber;
    private RoomType      roomType;
    private double        pricePerNight;
    private RoomStatus    status;
    private int           floorNumber;
    private String        description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructors ─────────────────────────────────────────────────────────

    public Room() {}

    public Room(String roomNumber, RoomType roomType, double pricePerNight,
                int floorNumber, String description) {
        this.roomNumber    = roomNumber;
        this.roomType      = roomType;
        this.pricePerNight = pricePerNight;
        this.floorNumber   = floorNumber;
        this.description   = description;
        this.status        = RoomStatus.AVAILABLE;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int         getRoomId()            { return roomId; }
    public void        setRoomId(int v)       { this.roomId = v; }

    public String      getRoomNumber()           { return roomNumber; }
    public void        setRoomNumber(String v)   { this.roomNumber = v; }

    public RoomType    getRoomType()          { return roomType; }
    public void        setRoomType(RoomType v){ this.roomType = v; }

    public double      getPricePerNight()          { return pricePerNight; }
    public void        setPricePerNight(double v)  { this.pricePerNight = v; }

    public RoomStatus  getStatus()             { return status; }
    public void        setStatus(RoomStatus v) { this.status = v; }

    public int         getFloorNumber()          { return floorNumber; }
    public void        setFloorNumber(int v)     { this.floorNumber = v; }

    public String      getDescription()          { return description; }
    public void        setDescription(String v)  { this.description = v; }

    public LocalDateTime getCreatedAt()                  { return createdAt; }
    public void          setCreatedAt(LocalDateTime v)   { this.createdAt = v; }

    public LocalDateTime getUpdatedAt()                  { return updatedAt; }
    public void          setUpdatedAt(LocalDateTime v)   { this.updatedAt = v; }

    public boolean isAvailable() { return status == RoomStatus.AVAILABLE; }

    @Override
    public String toString() {
        return String.format("Room %-5s | %-8s | Floor %d | ₹%8.2f/night | %-12s | %s",
                roomNumber, roomType, floorNumber, pricePerNight, status, description);
    }
}
