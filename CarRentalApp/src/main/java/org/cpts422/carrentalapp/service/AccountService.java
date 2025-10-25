package org.cpts422.carrentalapp.service;

import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.repo.AppUserRepository;
import org.cpts422.carrentalapp.service.error.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final AppUserRepository users;

    public AccountService(AppUserRepository users) { this.users = users; }

    public AppUser getById(Long id) {
        return users.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public AppUser getByUsername(String username) {
        return users.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
    }

    @Transactional
    public void addFunds(Long userId, double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Enter a positive amount.");
        AppUser u = getById(userId);
        u.setWalletBalance(round2(u.getWalletBalance() + amount));
        users.save(u);
    }

    @Transactional
    public void debit(Long userId, double amount) {
        AppUser u = getById(userId);
        double next = round2(u.getWalletBalance() - amount);
        if (next < -1e-9) { // disallow negatives
            throw new org.cpts422.carrentalapp.service.error.InsufficientFundsException(amount, u.getWalletBalance());
        }
        u.setWalletBalance(next);
        users.save(u);
    }

    private double round2(double x) { return Math.round(x * 100.0) / 100.0; }
}
