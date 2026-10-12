package com.hotel.service;

import com.hotel.dao.CustomerDAO;
import com.hotel.exception.EntityNotFoundException;
import com.hotel.exception.ValidationException;
import com.hotel.model.Customer;

import java.util.List;

/**
 * CustomerService — business logic layer for customer management.
 */
public class CustomerService {

    private final CustomerDAO customerDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    public Customer addCustomer(String name, String contact, String email,
                                 String address, String idProof, String idNumber) {
        validateName(name);
        validateContact(contact);
        Customer customer = new Customer(name.trim(), contact.trim(),
                email == null ? "" : email.trim(),
                address == null ? "" : address.trim(),
                idProof == null ? "" : idProof.trim(),
                idNumber == null ? "" : idNumber.trim());
        return customerDAO.create(customer);
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    public Customer getById(int customerId) {
        return customerDAO.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer #" + customerId + " not found."));
    }

    public List<Customer> getAllCustomers() {
        return customerDAO.findAll();
    }

    public List<Customer> search(String keyword) {
        if (keyword == null || keyword.isBlank())
            throw new ValidationException("Search keyword cannot be empty.");
        return customerDAO.search(keyword.trim());
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public boolean updateCustomer(Customer customer) {
        validateName(customer.getName());
        validateContact(customer.getContact());
        return customerDAO.update(customer);
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    public boolean deleteCustomer(int customerId) {
        getById(customerId); // verify exists
        return customerDAO.delete(customerId);
    }

    // ── Validation helpers ────────────────────────────────────────────────────

    private void validateName(String name) {
        if (name == null || name.isBlank())
            throw new ValidationException("Customer name cannot be empty.");
    }

    private void validateContact(String contact) {
        if (contact == null || !contact.matches("[6-9]\\d{9}"))
            throw new ValidationException("Contact must be a valid 10-digit Indian mobile number.");
    }
}
