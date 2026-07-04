package com.volunteer.registration.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Utility class for input validation and sanitization.
 * Provides methods to validate and sanitize user inputs to prevent injection attacks.
 */
@Component
public class InputValidator {

    // Regex patterns for validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[\\d\\s\\-\\(\\)\\+]*$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_-]{3,100}$"
    );

    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9\\s\\-_]*$"
    );

    /**
     * Validates email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        if (email.length() > 255) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates phone number format
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Phone is optional
        }
        if (phone.length() > 20) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validates username format
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (username.length() > 100) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Validates string length
     */
    public static boolean isValidLength(String value, int minLength, int maxLength) {
        if (value == null) {
            return minLength == 0;
        }
        int length = value.trim().length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Sanitizes HTML content by escaping special characters
     * Prevents XSS attacks when displaying user input
     */
    public static String sanitizeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;");
    }

    /**
     * Sanitizes string input to prevent injection attacks
     * Removes or escapes potentially dangerous characters
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        return input.trim()
            .replaceAll("[<>\"'%;()&+]", "")
            .substring(0, Math.min(input.length(), 1000)); // Limit length
    }

    /**
     * Validates that string contains only safe characters
     */
    public static boolean isSafeString(String value, int maxLength) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        if (value.length() > maxLength) {
            return false;
        }
        // Check for common injection patterns
        String lowerValue = value.toLowerCase();
        return !lowerValue.contains("<script")
            && !lowerValue.contains("javascript:")
            && !lowerValue.contains("onerror=")
            && !lowerValue.contains("onclick=")
            && !lowerValue.contains("union select")
            && !lowerValue.contains("drop table")
            && !lowerValue.contains("insert into")
            && !lowerValue.contains("update set");
    }

    /**
     * Validates that a string is alphanumeric with common separators
     */
    public static boolean isAlphanumeric(String value) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        if (value.length() > 500) {
            return false;
        }
        return ALPHANUMERIC_PATTERN.matcher(value).matches();
    }

    /**
     * Truncates string to maximum length
     */
    public static String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    /**
     * Normalizes and validates a date string
     */
    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return false;
        }
        // Simple validation for ISO format dates (YYYY-MM-DD)
        return dateStr.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }
}
