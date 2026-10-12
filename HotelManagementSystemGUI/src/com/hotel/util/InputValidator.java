package com.hotel.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * InputValidator — validation helpers used by the service layer.
 * (Console Scanner methods removed for GUI version; only pure validation kept.)
 */
public class InputValidator {

    public  static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private InputValidator() {}

    /** Returns true if the string is a valid 10-digit Indian mobile number. */
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("[6-9]\\d{9}");
    }

    /** Returns true if the string is a valid e-mail (or blank). */
    public static boolean isValidEmail(String email) {
        return email == null || email.isBlank()
               || email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    }

    /** Parses a date string in yyyy-MM-dd format, returning null on failure. */
    public static LocalDate parseDate(String s) {
        try { return LocalDate.parse(s, DATE_FMT); }
        catch (DateTimeParseException e) { return null; }
    }
}
