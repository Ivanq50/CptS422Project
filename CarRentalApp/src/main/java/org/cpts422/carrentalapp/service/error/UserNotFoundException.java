package org.cpts422.carrentalapp.service.error;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("User not found: id=" + id);
    }
    public UserNotFoundException(String username) {
        super("User not found: username=\"" + username + "\"");
    }
}
