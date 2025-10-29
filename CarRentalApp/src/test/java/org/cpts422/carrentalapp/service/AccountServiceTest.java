package org.cpts422.carrentalapp.service;

import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.repo.AppUserRepository;
import org.cpts422.carrentalapp.service.error.InsufficientFundsException;
import org.cpts422.carrentalapp.service.error.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock AppUserRepository users;
    @InjectMocks AccountService service;

    @Test
    void getByIdReturnsUser() {
        AppUser u = new AppUser();
        when(users.findById(7L)).thenReturn(Optional.of(u));

        AppUser out = service.getById(7L);

        assertSame(u, out);
        verify(users).findById(7L);
    }

    @Test
    void getByIdThrowsExceptionWhenUserIsMissing() {
        when(users.findById(9L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.getById(9L));
        verify(users).findById(9L);
    }

    @Test
    void getByUsernameReturnsUsername() {
        AppUser u = new AppUser();
        when(users.findByUsername("Test")).thenReturn(Optional.of(u));

        AppUser out = service.getByUsername("Test");

        assertSame(u, out);
        verify(users).findByUsername("Test");
    }

    @Test
    void getByUsernameThrowsExceptionWhenMissing() {
        when(users.findByUsername("Test")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.getByUsername("Test"));
        verify(users).findByUsername("Test");
    }

    @Test
    void addFundsRejectsNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> service.addFunds(1L, 0.0));
        assertThrows(IllegalArgumentException.class, () -> service.addFunds(1L, -5.0));
        verifyNoInteractions(users);
    }

    @Test
    void addFundsRoundsAndSaves() {
        AppUser u = new AppUser();
        u.setWalletBalance(10.10); // 10.10 + 0.105 = 10.205 = 10.21
        when(users.findById(1L)).thenReturn(Optional.of(u));
        when(users.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        service.addFunds(1L, 0.105);

        assertEquals(10.21, u.getWalletBalance(), 1e-9);
        verify(users).findById(1L);
        verify(users).save(u);
    }

    @Test
    void debitThrowsExceptionForInsufficientFunds() {
        AppUser u = new AppUser();
        u.setWalletBalance(5.00);
        when(users.findById(2L)).thenReturn(Optional.of(u));

        assertThrows(InsufficientFundsException.class, () -> service.debit(2L, 10.0));
        verify(users).findById(2L);
        verify(users, never()).save(any());
    }

    @Test
    void debitSubtractsFundsAndSaves() {
        AppUser u = new AppUser();
        u.setWalletBalance(20.00);
        when(users.findById(3L)).thenReturn(Optional.of(u));
        when(users.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        service.debit(3L, 7.337); // 20 - 7.337 = 12.663 -> round2 = 12.66

        assertEquals(12.66, u.getWalletBalance(), 1e-9);
        verify(users).findById(3L);
        verify(users).save(u);
    }
}
