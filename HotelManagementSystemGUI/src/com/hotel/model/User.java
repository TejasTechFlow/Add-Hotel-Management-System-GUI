package com.hotel.model;

import java.time.LocalDateTime;

/**
 * User — represents an admin or staff member who can log in to the system.
 * Demonstrates Encapsulation: all fields are private with getters/setters.
 */
public class User {

    public enum Role { ADMIN, STAFF }

    private int           userId;
    private String        username;
    private String        passwordHash;   // stored as SHA-256 hex
    private Role          role;
    private String        fullName;
    private String        email;
    private String        phone;
    private boolean       active;
    private LocalDateTime createdAt;

    // ── Constructors ─────────────────────────────────────────────────────────

    public User() {}

    public User(String username, String passwordHash, Role role, String fullName,
                String email, String phone) {
        this.username     = username;
        this.passwordHash = passwordHash;
        this.role         = role;
        this.fullName     = fullName;
        this.email        = email;
        this.phone        = phone;
        this.active       = true;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int           getUserId()       { return userId; }
    public void          setUserId(int v)  { this.userId = v; }

    public String        getUsername()        { return username; }
    public void          setUsername(String v){ this.username = v; }

    public String        getPasswordHash()           { return passwordHash; }
    public void          setPasswordHash(String v)   { this.passwordHash = v; }

    public Role          getRole()       { return role; }
    public void          setRole(Role v) { this.role = v; }

    public String        getFullName()        { return fullName; }
    public void          setFullName(String v){ this.fullName = v; }

    public String        getEmail()        { return email; }
    public void          setEmail(String v){ this.email = v; }

    public String        getPhone()        { return phone; }
    public void          setPhone(String v){ this.phone = v; }

    public boolean       isActive()         { return active; }
    public void          setActive(boolean v){ this.active = v; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void          setCreatedAt(LocalDateTime v){ this.createdAt = v; }

    public boolean isAdmin() { return role == Role.ADMIN; }

    @Override
    public String toString() {
        return String.format("[User #%d] %-15s %-8s %s", userId, username, role, fullName);
    }
}
