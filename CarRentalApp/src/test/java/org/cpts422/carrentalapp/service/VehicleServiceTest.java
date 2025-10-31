package org.cpts422.carrentalapp.service;

import org.cpts422.carrentalapp.model.Vehicle;
import org.cpts422.carrentalapp.model.VehicleType;
import org.cpts422.carrentalapp.repo.VehicleRepository;
import org.cpts422.carrentalapp.service.error.VehicleNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {
    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle vehicle1;
    private Vehicle vehicle2;
    private List<Vehicle> allVehicles;
    private List<Vehicle> availableVehicles;

    @BeforeEach
    void setUp() {
        vehicle1 = new Vehicle("Toyota", "Camry", VehicleType.SEDAN, 50.0, true);
        vehicle2 = new Vehicle("Honda", "Civic", VehicleType.ECONOMY, 40.0, false);

        allVehicles = Arrays.asList(vehicle1, vehicle2);
        availableVehicles = Collections.singletonList(vehicle1);
    }

    @Test
    void list_shouldReturnAllVehicles_whenAvailableIsNull() {
        when(vehicleRepository.findAll()).thenReturn(allVehicles);

        List<Vehicle> result = vehicleService.list(null);

        assertEquals(2, result.size());
        assertEquals(allVehicles, result);
        verify(vehicleRepository).findAll(); // Verify this branch was called
        verify(vehicleRepository, never()).findByAvailable(anyBoolean()); // Verify other branch was not
    }

    @Test
    void list_shouldReturnOnlyAvailableVehicles_whenAvailableIsTrue() {
        when(vehicleRepository.findByAvailable(true)).thenReturn(availableVehicles);

        List<Vehicle> result = vehicleService.list(true);

        assertEquals(1, result.size());
        assertEquals(availableVehicles, result);
        verify(vehicleRepository, never()).findAll(); // Verify other branch was not
        verify(vehicleRepository).findByAvailable(true); // Verify this branch was called
    }

    @Test
    void list_shouldReturnOnlyUnavailableVehicles_whenAvailableIsFalse() {
        List<Vehicle> unavailableVehicles = Collections.singletonList(vehicle2);
        when(vehicleRepository.findByAvailable(false)).thenReturn(unavailableVehicles);

        List<Vehicle> result = vehicleService.list(false);

        assertEquals(1, result.size());
        assertEquals(unavailableVehicles, result);
        verify(vehicleRepository, never()).findAll(); // Verify other branch was not
        verify(vehicleRepository).findByAvailable(false); // Verify this branch was called
    }

    @Test
    void get_shouldReturnVehicle_whenFound() {
        Long vehicleId = 1L;
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle1));

        Vehicle result = vehicleService.get(vehicleId);

        assertNotNull(result);
        assertEquals(vehicle1, result);
        verify(vehicleRepository).findById(vehicleId);
    }

    @Test
    void get_shouldThrowVehicleNotFoundException_whenNotFound() {
        Long vehicleId = 99L; // An ID that doesn't exist
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

        VehicleNotFoundException exception = assertThrows(VehicleNotFoundException.class, () -> {
            vehicleService.get(vehicleId);
        });

        assertTrue(exception.getMessage().contains("Vehicle not found: " + vehicleId));

        verify(vehicleRepository).findById(vehicleId);
    }
}