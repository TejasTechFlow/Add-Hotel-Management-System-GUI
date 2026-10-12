package com.hotel.service;

import com.hotel.dao.UserDAO;
import com.hotel.exception.ValidationException;
import com.hotel.model.User;
import com.hotel.util.PasswordUtil;

import java.util.Optional;

/**
 * AuthService — handles login, logout and session state.
 * Demonstrates the Service layer pattern (business logic separate from DAO).
 */
public class AuthService {

    private static AuthService  instance;
    private final  UserDAO      userDAO;
    private        User         currentUser;   // null when logged out

    private AuthService() {
        this.userDAO = new UserDAO();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) instance = new AuthService();
        return instance;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Attempts to log in with the given credentials.
     *
     * @return the authenticated User
     * @throws ValidationException if credentials are invalid
     */
    public User login(String username, String password) {
        if (username == null || username.isBlank())
            throw new ValidationException("Username cannot be empty.");
        if (password == null || password.isBlank())
            throw new ValidationException("Password cannot be empty.");

        Optional<User> opt = userDAO.findByUsername(username.trim());
        if (opt.isEmpty())
            throw new ValidationException("Invalid username or password.");

        User user = opt.get();
        if (!PasswordUtil.verify(password, user.getPasswordHash()))
            throw new ValidationException("Invalid username or password.");

        this.currentUser = user;
        System.out.println("\n  Welcome, " + user.getFullName() + "! (" + user.getRole() + ")");
        return user;
    }

    /** Logs out the current user. */
    public void logout() {
        if (currentUser != null) {
            System.out.println("\n  Goodbye, " + currentUser.getFullName() + "!");
            currentUser = null;
        }
    }

    /** Returns the currently logged-in user, or null. */
    public User getCurrentUser() {
        return currentUser;
    }

    /** Returns true if a user is currently logged in. */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Asserts that the current user is an ADMIN.
     * @throws ValidationException if not admin
     */
    public void requireAdmin() {
        if (!isLoggedIn())   throw new ValidationException("Please log in first.");
        if (!currentUser.isAdmin()) throw new ValidationException("Admin privileges required.");
    }

    /**
     * Asserts that a user is logged in (admin or staff).
     * @throws ValidationException if not logged in
     */
    public void requireLogin() {
        if (!isLoggedIn()) throw new ValidationException("Please log in first.");
    }
}
