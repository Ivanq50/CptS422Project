package org.cpts422.carrentalapp.service.error;
public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException(String username) { super("Username already taken: " + username); }
}
