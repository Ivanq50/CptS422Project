/*
Takes password and driver's license and validates them before saving user
*/

// Created by : Yevin
// Created on : Sep 22

// Last Updated by : Yevin
// Last Updated on : Sep 22

package org.cpts422.carrentalapp.service;

import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.repo.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService
{

    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    public AuthenticationService(AppUserRepository users, PasswordEncoder encoder)
    {
        this.users = users;
        this.encoder = encoder;
    }

    public AppUser register(AppUser u, String rawPassword)
    {
        u.setPasswordHash(encoder.encode(rawPassword));
        return users.save(u);
    }

    public AppUser authenticate(String username, String rawPassword)
    {
        var user = users.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        if (!encoder.matches(rawPassword, user.getPasswordHash()))
        {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return user;
    }
}
