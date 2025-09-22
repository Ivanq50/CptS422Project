/*
Takes password and driver's license and validates them before saving user
*/

// Created by : Yevin
// Created on : Sep 22

// Last Updated by : Yevin
// Last Updated on : Sep 22

package org.cpts422.carrentalapp.service;

import java.time.LocalDate;

import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.repo.AppUserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService
{
    private final AppUserRepository users;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthenticationService(AppUserRepository users)
    {
        this.users = users;
    }

    public boolean isLicenseExpired(LocalDate expiry)
    {
        return expiry != null && expiry.isBefore(LocalDate.now());
    }

    // Register user with rules: age>=21, license not expired, unique username & Drivers License
    public AppUser register(AppUser user, String rawPassword)
    {
        if (user.getAge() < 21)
        {
            throw new IllegalArgumentException("Age must be 21 or older to create an account.");
        }
        if (isLicenseExpired(user.getDriversLicenseExpiry()))
        {
            throw new IllegalArgumentException("Driver's license is expired.");
        }
        if (users.existsByUsername(user.getUsername()))
        {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (users.existsByDriversLicenseNumber(user.getDriversLicenseNumber()))
        {
            throw new IllegalArgumentException("Driver's license number already in use.");
        }
        user.setPasswordHash(encoder.encode(rawPassword));
        return users.save(user);
    }
}
