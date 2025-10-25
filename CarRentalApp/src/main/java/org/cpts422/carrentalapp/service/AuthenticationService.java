package org.cpts422.carrentalapp.service;

import jakarta.transaction.Transactional;
import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.model.MembershipType;
import org.cpts422.carrentalapp.repo.AppUserRepository;
import org.cpts422.carrentalapp.service.error.DriversLicenseTakenException;
import org.cpts422.carrentalapp.service.error.DuplicateUsernameException;
import org.cpts422.carrentalapp.web.datatransfers.RegistrationForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    public AuthenticationService(AppUserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public AppUser register(AppUser u, String rawPassword) {
        u.setPasswordHash(encoder.encode(rawPassword));
        return users.save(u);
    }

    public AppUser authenticate(String username, String rawPassword) {
        AppUser user = users.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        if (!encoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return user;
    }

    @Transactional
    public AppUser register(RegistrationForm form) {
        String username = form.getUsername();
        String dl       = form.getDriversLicenseNumber();

        if (users.existsByUsername(username)) {
            throw new DuplicateUsernameException(username);
        }
        if (users.existsByDriversLicenseNumber(dl)) {
            throw new DriversLicenseTakenException(dl);
        }

        AppUser u = new AppUser();
        u.setUsername(username);
        u.setAge(form.getAge());
        u.setDriversLicenseNumber(dl);
        u.setDriversLicenseExpiry(form.getDriversLicenseExpiry());
        u.setMembershipType(form.getMembershipType() == null
                ? MembershipType.STANDARD
                : form.getMembershipType());
        u.setWalletBalance(0.0);

        return register(u, form.getPassword());
    }
}