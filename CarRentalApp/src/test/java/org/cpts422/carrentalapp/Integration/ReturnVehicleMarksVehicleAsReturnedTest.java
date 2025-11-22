package org.cpts422.carrentalapp.Integration;

import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.model.MembershipType;
import org.cpts422.carrentalapp.model.Rental;
import org.cpts422.carrentalapp.model.Vehicle;
import org.cpts422.carrentalapp.model.VehicleType;
import org.cpts422.carrentalapp.repo.AppUserRepository;
import org.cpts422.carrentalapp.repo.RentalRepository;
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
class ReturnVehicleMarksVehicleAsReturnedTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private RentalService rentalService;

    @Test
    void returnVehicleMarksVehicleAsReturned() {
        AppUser user = new AppUser();
        user.setUsername("return_mark_user");
        user.setPasswordHash("hash");
        user.setAge(30);
        user.setDriversLicenseNumber("DL-RM-1");
        user.setDriversLicenseExpiry(LocalDate.now().plusYears(5));
        user.setMembershipType(MembershipType.STANDARD);
        user.setWalletBalance(200.0);
        user = appUserRepository.save(user);

        Vehicle vehicle = new Vehicle("Toyota", "Corolla", VehicleType.SEDAN, 40.0, true);
        vehicle = vehicleRepository.save(vehicle);

        Rental rental = rentalService.createRental(user.getId(), vehicle.getId(), 2);

        LocalDateTime now = LocalDateTime.now();
        rentalService.markReturned(rental, now);

        Rental returnedRental = rentalRepository.findById(rental.getId()).orElseThrow();

        assertNotNull(returnedRental.getReturnedAt(),
                "returnedAt should be set after returning the vehicle");
        assertEquals(now.toLocalDate(), returnedRental.getReturnedAt(),
                "returnedAt should match the return date passed to markReturned");
    }
}
