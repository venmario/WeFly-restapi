package com.example.wefly_app.util;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Getter
@Component
public class PasswordValidatorUtil {
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[@$!%*?&#].*");
    private static final Pattern LENGTH_PATTERN = Pattern.compile(".{8,}");
    public String message;

    public boolean validatePassword(String password) {
        StringBuilder error = new StringBuilder();

        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            error.append("Password must contain at least one lowercase letter.\n");
        }
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            error.append("Password must contain at least one uppercase letter.\n");
        }
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            error.append("Password must contain at least one digit.\n");
        }
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            error.append("Password must contain at least one special character.\n");
        }
        if (!LENGTH_PATTERN.matcher(password).matches()) {
            error.append("Password must be at least 8 characters long.\n");
        }

        if (error.length() != 0) {
            this.message = error.toString();
            return false;
        }
        return true;
    }
}
