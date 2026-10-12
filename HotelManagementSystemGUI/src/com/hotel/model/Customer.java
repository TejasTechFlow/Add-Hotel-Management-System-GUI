package com.hotel.model;

import java.time.LocalDateTime;

/**
 * Customer — represents a hotel guest.
 */
public class Customer {

    private int           customerId;
    private String        name;
    private String        contact;
    private String        email;
    private String        address;
    private String        idProof;        // document type
    private String        idNumber;       // document number
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructors ─────────────────────────────────────────────────────────

    public Customer() {}

    public Customer(String name, String contact, String email,
                    String address, String idProof, String idNumber) {
        this.name     = name;
        this.contact  = contact;
        this.email    = email;
        this.address  = address;
        this.idProof  = idProof;
        this.idNumber = idNumber;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int    getCustomerId()         { return customerId; }
    public void   setCustomerId(int v)    { this.customerId = v; }

    public String getName()              { return name; }
    public void   setName(String v)      { this.name = v; }

    public String getContact()           { return contact; }
    public void   setContact(String v)   { this.contact = v; }

    public String getEmail()             { return email; }
    public void   setEmail(String v)     { this.email = v; }

    public String getAddress()           { return address; }
    public void   setAddress(String v)   { this.address = v; }

    public String getIdProof()           { return idProof; }
    public void   setIdProof(String v)   { this.idProof = v; }

    public String getIdNumber()          { return idNumber; }
    public void   setIdNumber(String v)  { this.idNumber = v; }

    public LocalDateTime getCreatedAt()                { return createdAt; }
    public void          setCreatedAt(LocalDateTime v) { this.createdAt = v; }

    public LocalDateTime getUpdatedAt()                { return updatedAt; }
    public void          setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }

    @Override
    public String toString() {
        return String.format("[Customer #%d] %-20s | Ph: %-12s | %s", customerId, name, contact, email);
    }
}
