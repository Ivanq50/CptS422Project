package org.cpts422.carrentalapp.service;

import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.model.MembershipType;
import org.cpts422.carrentalapp.repo.AppUserRepository;
import org.cpts422.carrentalapp.service.error.DriversLicenseTakenException;
import org.cpts422.carrentalapp.service.error.DuplicateUsernameException;
import org.cpts422.carrentalapp.web.datatransfers.RegistrationForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock AppUserRepository users;
    @Mock PasswordEncoder encoder;

    @InjectMocks AuthenticationService service;

    @Test
    void UsernameNotFoundThrowsException() {
        when(users.findByUsername("Test")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.authenticate("Test", "pwd"));

        assertTrue(ex.getMessage() == null || ex.getMessage().toLowerCase().contains("username"));
        verify(users).findByUsername("Test");
        verifyNoMoreInteractions(users, encoder);
    }

    @Test
    void WrongPasswordThrowsException() {
        AppUser u = new AppUser();
        u.setUsername("Test");
        u.setPasswordHash("HASH");
        when(users.findByUsername("Test")).thenReturn(Optional.of(u));
        when(encoder.matches("bad", "HASH")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.authenticate("Test", "bad"));

        assertTrue(ex.getMessage() == null || ex.getMessage().toLowerCase().contains("password"));
        verify(users).findByUsername("Test");
        verify(encoder).matches("bad", "HASH");
        verifyNoMoreInteractions(users, encoder);
    }

    @Test
    void authenticateSuccessReturnsUser() {
        AppUser u = new AppUser();
        u.setUsername("Test");
        u.setPasswordHash("HASH");
        when(users.findByUsername("Test")).thenReturn(Optional.of(u));
        when(encoder.matches("ok", "HASH")).thenReturn(true);

        AppUser out = service.authenticate("Test", "ok");

        assertSame(u, out);
        verify(users).findByUsername("Test");
        verify(encoder).matches("ok", "HASH");
        verifyNoMoreInteractions(users, encoder);
    }

    @Test
    void UserPasswordHashesAndSaves() {
        AppUser u = new AppUser();
        when(encoder.encode("password")).thenReturn("ENC");
        when(users.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        AppUser saved = service.register(u, "password");

        assertEquals("ENC", saved.getPasswordHash());
        verify(encoder).encode("password");
        verify(users).save(u);
        verifyNoMoreInteractions(users, encoder);
    }

    private RegistrationForm baseForm() {
        RegistrationForm f = new RegistrationForm();
        f.setUsername("u1");
        f.setPassword("pw-12345678");
        f.setAge(30);
        f.setDriversLicenseNumber("DL123");
        f.setDriversLicenseExpiry(LocalDate.of(2030, 1, 1));
        return f;
    }

    @Test
    void registerDuplicateUsernameThrowsException() {
        RegistrationForm f = baseForm();
        when(users.existsByUsername("u1")).thenReturn(true);

        assertThrows(DuplicateUsernameException.class, () -> service.register(f));

        verify(users).existsByUsername("u1");
        verifyNoMoreInteractions(users, encoder);
    }

    @Test
    void registerDuplicateDriversLicenseThrowsException() {
        RegistrationForm f = baseForm();
        when(users.existsByUsername("u1")).thenReturn(false);
        when(users.existsByDriversLicenseNumber("DL123")).thenReturn(true);

        assertThrows(DriversLicenseTakenException.class, () -> service.register(f));

        verify(users).existsByUsername("u1");
        verify(users).existsByDriversLicenseNumber("DL123");
        verifyNoMoreInteractions(users, encoder);
    }

    @Test
    void registerUserWithNoMembershipDefaultsToStandard() {
        RegistrationForm f = baseForm();
        f.setMembershipType(null);

        when(users.existsByUsername("u1")).thenReturn(false);
        when(users.existsByDriversLicenseNumber("DL123")).thenReturn(false);
        when(encoder.encode("pw-12345678")).thenReturn("ENC");

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        when(users.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        AppUser saved = service.register(f);

        AppUser created = captor.getValue();
        assertEquals("u1", created.getUsername());
        assertEquals(30, created.getAge());
        assertEquals("DL123", created.getDriversLicenseNumber());
        assertEquals(LocalDate.of(2030,1,1), created.getDriversLicenseExpiry());
        assertEquals(MembershipType.STANDARD, created.getMembershipType());
        assertEquals(0.0, created.getWalletBalance(), 0.0001);
        assertEquals("ENC", saved.getPasswordHash());

        verify(users).existsByUsername("u1");
        verify(users).existsByDriversLicenseNumber("DL123");
        verify(encoder).encode("pw-12345678");
        verify(users).save(any(AppUser.class));
    }

    @Test
    void registerUserWithMembershipRetainsMembershipType() {
        RegistrationForm f = baseForm();
        f.setMembershipType(MembershipType.PREMIUM);

        when(users.existsByUsername("u1")).thenReturn(false);
        when(users.existsByDriversLicenseNumber("DL123")).thenReturn(false);
        when(encoder.encode("pw-12345678")).thenReturn("ENC");
        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        when(users.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.register(f);

        AppUser created = captor.getValue();
        assertEquals(MembershipType.PREMIUM, created.getMembershipType());
        assertEquals(0.0, created.getWalletBalance(), 0.0001);

        verify(users).existsByUsername("u1");
        verify(users).existsByDriversLicenseNumber("DL123");
        verify(encoder).encode("pw-12345678");
        verify(users).save(any(AppUser.class));
    }
}
