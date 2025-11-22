package org.cpts422.carrentalapp.Integration;

import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.model.MembershipType;
import org.cpts422.carrentalapp.model.Rental;
import org.cpts422.carrentalapp.model.Vehicle;
import org.cpts422.carrentalapp.model.VehicleType;
import org.cpts422.carrentalapp.repo.AppUserRepository;
import org.cpts422.carrentalapp.repo.VehicleRepository;
import org.cpts422.carrentalapp.service.RentalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReturnVehicleSetsVehicleAsAvailableTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private RentalService rentalService;

    @Test
    void returnVehicleSetsVehicleAsAvailable() {
        AppUser user = new AppUser();
        user.setUsername("return_available_user");
        user.setPasswordHash("hash");
        user.setAge(30);
        user.setDriversLicenseNumber("DL-RA-1");
        user.setDriversLicenseExpiry(LocalDate.now().plusYears(5));
        user.setMembershipType(MembershipType.STANDARD);
        user.setWalletBalance(200.0);
        user = appUserRepository.save(user);

        Vehicle vehicle = new Vehicle("Honda", "Civic", VehicleType.ECONOMY, 35.0, true);
        vehicle = vehicleRepository.save(vehicle);

        Rental rental = rentalService.createRental(user.getId(), vehicle.getId(), 3);

        Vehicle vehicleAfterRent = vehicleRepository.findById(vehicle.getId()).orElseThrow();
        assertFalse(vehicleAfterRent.isAvailable(),
                "Vehicle should be unavailable after being rented");

        rentalService.markReturned(rental, LocalDateTime.now());

        Vehicle vehicleAfterReturn = vehicleRepository.findById(vehicle.getId()).orElseThrow();

        assertTrue(vehicleAfterReturn.isAvailable(),
                "Vehicle should be available again after returning the rental");
    }
}
