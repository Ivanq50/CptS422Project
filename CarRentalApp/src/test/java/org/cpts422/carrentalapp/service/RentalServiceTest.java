package org.cpts422.carrentalapp.service;

import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.model.Rental;
import org.cpts422.carrentalapp.model.Vehicle;
import org.cpts422.carrentalapp.model.VehicleType;
import org.cpts422.carrentalapp.repo.AppUserRepository;
import org.cpts422.carrentalapp.repo.RentalRepository;
import org.cpts422.carrentalapp.repo.VehicleRepository;
import org.cpts422.carrentalapp.service.error.UserNotFoundException;
import org.cpts422.carrentalapp.service.error.VehicleNotFoundException;
import org.cpts422.carrentalapp.service.error.VehicleUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private PricingService pricingService;

    @InjectMocks
    private RentalService rentalService;

    private AppUser testUser;
    private Vehicle testVehicleAvail;
    private Vehicle testVehicleUnavail;
    private Rental testRental;

    @BeforeEach
    void setUp() {
        testUser = new AppUser();
        testUser.setUsername("testUser");

        testVehicleAvail = new Vehicle("Toyota","Camry", VehicleType.SEDAN, 50.0, true);
        testVehicleUnavail = new Vehicle("Honda", "Civic", VehicleType.ECONOMY, 40.0, false);

        testRental = new Rental();
        testRental.setUser(testUser);
        testRental.setVehicle(testVehicleUnavail);
        testRental.setRentedAt(LocalDate.now().minusDays(5));
        testRental.setExpectedReturnAt(LocalDate.now().minusDays(2));
    }

    @Test
    void test_createRental_zeroDays() {
        Long userId = 1L;
        Long vehicleId = 101L;
        int days = 0;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> rentalService.createRental(userId, vehicleId, days));

        assertEquals("Days must be >= 1", exception.getMessage());
        verifyNoInteractions(appUserRepository, vehicleRepository, rentalRepository, pricingService);
    }

    @Test
    void test_createRental_negativeDays() {
        Long userId = 1L;
        Long vehicleId = 101L;
        int days = -1;

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> rentalService.createRental(userId, vehicleId, days));

        assertEquals("Days must be >= 1", e.getMessage());
        verifyNoInteractions(appUserRepository, vehicleRepository, rentalRepository, pricingService);
    }

    @Test
    void test_createRental_userNotFound() {
        Long userId = 99L;
        Long vehicleId = 101L;
        int days = 3;

        when(appUserRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> rentalService.createRental(userId, vehicleId, days));

        verify(appUserRepository).findById(userId);
        verifyNoInteractions(vehicleRepository, rentalRepository, pricingService);
    }

    @Test
    void test_createRental_vehicleNotFound() {
        Long userId = 1L;
        Long vehicleId = 999L;
        int days = 3;
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

        assertThrows(VehicleNotFoundException.class, () -> rentalService.createRental(userId, vehicleId, days));

        verify(appUserRepository).findById(userId);
        verify(vehicleRepository).findById(vehicleId);
        verifyNoInteractions(rentalRepository, pricingService);
    }

    @Test
    void test_createRental_vehicleUnavailable() {
        Long userId = 1L;
        Long vehicleId = 102L;
        int days = 3;
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(testVehicleUnavail));

        assertThrows(VehicleUnavailableException.class, () -> rentalService.createRental(userId, vehicleId, days));

        verify(appUserRepository).findById(userId);
        verify(vehicleRepository).findById(vehicleId);
        verifyNoInteractions(rentalRepository, pricingService);
    }

    @Test
    void test_createRental_success() {
        Long userId = 1L;
        Long vehicleId = 101L;
        int days = 5;
        double expectedCharge = 250.0;

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(testVehicleAvail));
        when(pricingService.rentalTotal(testUser, testVehicleAvail, days)).thenReturn(expectedCharge);

        when(rentalRepository.save(any(Rental.class))).thenAnswer(i -> i.getArgument(0));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));

        ArgumentCaptor<Rental> rentalCaptor = ArgumentCaptor.forClass(Rental.class);
        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);

        Rental createdRental = rentalService.createRental(userId, vehicleId, days);

        assertNotNull(createdRental);
        assertEquals(testUser, createdRental.getUser());
        assertEquals(testVehicleAvail, createdRental.getVehicle());
        assertEquals(LocalDate.now(), createdRental.getRentedAt());
        assertEquals(LocalDate.now().plusDays(days), createdRental.getExpectedReturnAt());
        assertNull(createdRental.getReturnedAt());
        assertEquals(days, createdRental.getDaysPaid());
        assertEquals(expectedCharge, createdRental.getTotalCharge());

        verify(appUserRepository).findById(userId);
        verify(vehicleRepository).findById(vehicleId);
        verify(pricingService).rentalTotal(testUser, testVehicleAvail, days);

        verify(vehicleRepository).save(vehicleCaptor.capture());
        assertFalse(vehicleCaptor.getValue().isAvailable());

        verify(rentalRepository).save(rentalCaptor.capture());
        assertEquals(createdRental, rentalCaptor.getValue());
    }

    @Test
    void test_createRental_daysOverload_success() {
        Long userId = 1L;
        Long vehicleId = 101L;
        Integer days = 5;
        double expectedCharge = 250.0;

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(testVehicleAvail));
        when(pricingService.rentalTotal(testUser, testVehicleAvail, days)).thenReturn(expectedCharge);
        when(rentalRepository.save(any(Rental.class))).thenAnswer(i -> i.getArgument(0));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));

        Rental createdRental = rentalService.createRental(userId, vehicleId, days);

        assertNotNull(createdRental);
        assertEquals(days, createdRental.getDaysPaid());

        verify(appUserRepository).findById(userId);
        verify(vehicleRepository).findById(vehicleId);
        verify(pricingService).rentalTotal(testUser, testVehicleAvail, days);
        verify(vehicleRepository).save(any(Vehicle.class));
        verify(rentalRepository).save(any(Rental.class));
    }

    @Test
    void test_createRental_daysOverload_OneDayIfNull() {
        Long userId = 1L;
        Long vehicleId = 101L;
        Integer days = null;
        int defaultDays = 1;
        double expectedCharge = 50.0;

        when(appUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(testVehicleAvail));
        when(pricingService.rentalTotal(testUser, testVehicleAvail, defaultDays)).thenReturn(expectedCharge);
        when(rentalRepository.save(any(Rental.class))).thenAnswer(i -> i.getArgument(0));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));

        Rental createdRental = rentalService.createRental(userId, vehicleId, days);

        assertNotNull(createdRental);
        assertEquals(defaultDays, createdRental.getDaysPaid());

        verify(pricingService).rentalTotal(testUser, testVehicleAvail, defaultDays);
        verify(rentalRepository).save(any(Rental.class));
    }

    @Test
    void findAllForUser_shouldReturnRentals() {
        Long userId = 1L;
        List<Rental> expectedRentals = Collections.singletonList(testRental);
        when(rentalRepository.findByUserIdOrderByRentedAtDesc(userId)).thenReturn(expectedRentals);

        List<Rental> actualRentals = rentalService.findAllForUser(userId);

        assertEquals(expectedRentals, actualRentals);
        verify(rentalRepository).findByUserIdOrderByRentedAtDesc(userId);
    }

    @Test
    void getById_shouldReturnRental_whenFound() {
        Long rentalId = 501L;
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(testRental));

        Rental actualRental = rentalService.getById(rentalId);

        assertEquals(testRental, actualRental);
        verify(rentalRepository).findById(rentalId);
    }

    @Test
    void getById_shouldThrowException_whenNotFound() {
        Long rentalId = 999L; // Non-existent ID
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            rentalService.getById(rentalId);
        });
        assertTrue(exception.getMessage().contains("Invalid rental id: " + rentalId));
        verify(rentalRepository).findById(rentalId);
    }

    @Test
    void markReturned_shouldDoNothing_whenAlreadyReturned() {
        LocalDateTime now = LocalDateTime.now();
        testRental.setReturnedAt(LocalDate.now().minusDays(1)); // Mark as already returned

        rentalService.markReturned(testRental, now);

        // Verify that no save operations were called because it was already returned
        verify(rentalRepository, never()).save(any(Rental.class));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void markReturned_shouldUpdateRentalAndVehicle_whenNotReturnedAndVehicleUnavailable() {
        LocalDateTime returnTime = LocalDateTime.now();
        // Ensure rental is not returned yet
        testRental.setReturnedAt(null);
        // Ensure vehicle is marked as unavailable (consistent with being rented)
        testRental.getVehicle().setAvailable(false);

        ArgumentCaptor<Rental> rentalCaptor = ArgumentCaptor.forClass(Rental.class);
        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);

        rentalService.markReturned(testRental, returnTime);

        // Verify rentalRepository.save was called once
        verify(rentalRepository, times(1)).save(rentalCaptor.capture());
        Rental savedRental = rentalCaptor.getValue();
        assertEquals(returnTime.toLocalDate(), savedRental.getReturnedAt()); // Check if return date is set

        // Verify vehicleRepository.save was called once
        verify(vehicleRepository, times(1)).save(vehicleCaptor.capture());
        Vehicle savedVehicle = vehicleCaptor.getValue();
        assertTrue(savedVehicle.isAvailable()); // Vehicle should now be available
    }

    @Test
    void markReturned_shouldUpdateRentalOnly_whenNotReturnedAndVehicleAlreadyAvailable() {
        LocalDateTime returnTime = LocalDateTime.now();
        // Ensure rental is not returned yet
        testRental.setReturnedAt(null);
        // Ensure vehicle is *already* marked as available (edge case test)
        Vehicle vehicle = testRental.getVehicle();
        vehicle.setAvailable(true); // Explicitly set to available

        ArgumentCaptor<Rental> rentalCaptor = ArgumentCaptor.forClass(Rental.class);

        rentalService.markReturned(testRental, returnTime);

        // Verify rentalRepository.save was called once
        verify(rentalRepository, times(1)).save(rentalCaptor.capture());
        Rental savedRental = rentalCaptor.getValue();
        assertEquals(returnTime.toLocalDate(), savedRental.getReturnedAt());

        // Verify vehicleRepository.save was NOT called
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }
}