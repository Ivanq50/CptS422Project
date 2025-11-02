package org.cpts422.carrentalapp.service;


import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.repo.AppUserRepository;
import org.cpts422.carrentalapp.service.error.InsufficientFundsException;
import org.cpts422.carrentalapp.service.error.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    private AppUser user;

    @BeforeEach
    void setUp() {
        user = new AppUser();
        user.setUsername("tytyruss");
        user.setWalletBalance(100.0);
    }

    @Test
    void testGetByIdFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        AppUser result = accountService.getById(1L);

        assertEquals(user, result);
        verify(userRepository).findById(1L);
    }

    @Test
    void testGetByIdNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> accountService.getById(2L));
    }

    @Test
    void testGetByUsernameFound() {
        when(userRepository.findByUsername("tytyruss")).thenReturn(Optional.of(user));

        AppUser result = accountService.getByUsername("tytyruss");

        assertEquals(user, result);
        verify(userRepository).findByUsername("tytyruss");
    }

    @Test
    void testGetByUsernameNotFound() {
        when(userRepository.findByUsername("DNE")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> accountService.getByUsername("DNE"));
    }

    @Test
    void testAddFundsPositive() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        accountService.addFunds(1L, 50.0);

        assertEquals(150.0, user.getWalletBalance());
        verify(userRepository).save(user);
    }

    @Test
    void testAddFundsZeroOrNegative() {
        assertThrows(IllegalArgumentException.class, () -> accountService.addFunds(1L, 0));
        assertThrows(IllegalArgumentException.class, () -> accountService.addFunds(1L, -10));
    }


    @Test
    void testDebitBalance() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        accountService.debit(1L, 50.0);

        assertEquals(50.0, user.getWalletBalance());
        verify(userRepository).save(user);
    }

    @Test
    void testDebitExactBalance() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        accountService.debit(1L, 100.0);

        assertEquals(0.0, user.getWalletBalance());
        verify(userRepository).save(user);
    }

    @Test
    void testDebitInsufficientFunds() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(InsufficientFundsException.class, () -> accountService.debit(1L, 150.0));
        verify(userRepository, never()).save(any());
    }
}