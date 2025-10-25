package org.cpts422.carrentalapp.service.error;
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Object idOrName) { super("User not found: " + idOrName); }
}
