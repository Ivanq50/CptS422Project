package org.cpts422.carrentalapp.service;

import org.cpts422.carrentalapp.model.*;
import org.cpts422.carrentalapp.repo.AppUserRepository;
import org.cpts422.carrentalapp.repo.RentalRepository;
import org.cpts422.carrentalapp.repo.VehicleRepository;
import org.cpts422.carrentalapp.service.error.UserNotFoundException;
import org.cpts422.carrentalapp.service.error.VehicleNotFoundException;
import org.cpts422.carrentalapp.service.error.VehicleUnavailableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class RentalService {
    private final VehicleRepository vehicles;
    private final AppUserRepository users;
    private final RentalRepository rentals;
    private final PricingService pricing;

    public RentalService(VehicleRepository vehicles, AppUserRepository users,
                         RentalRepository rentals, PricingService pricing) {
        this.vehicles = vehicles; this.users = users; this.rentals = rentals; this.pricing = pricing;
    }

    @Transactional
    public Rental createRental(Long userId, Long vehicleId, int days) {
        AppUser user = users.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Vehicle v = vehicles.findById(vehicleId).orElseThrow(() -> new VehicleNotFoundException(vehicleId));

        if (!Boolean.TRUE.equals(v.getAvailable())) throw new VehicleUnavailableException(vehicleId);

        LocalDate today = pricing.today();
        Rental r = new Rental();
        r.setUser(user);
        r.setVehicle(v);
        r.setRentedAt(today);
        r.setExpectedReturnAt(today.plusDays(days));

        v.setAvailable(false);
        vehicles.save(v);
        return rentals.save(r);
    }

    @Transactional
    public double computeLatePenalty(Rental r, AppUser user, LocalDateTime now) {
        return pricing.computeLatePenalty(r, user, now);
    }

    @Transactional
    public void markReturned(Rental r, LocalDateTime now) {
        r.setReturnedAt(now.toLocalDate());
        r.setReturnedAtDateTime(now);
        rentals.save(r);

        Vehicle v = r.getVehicle();
        v.setAvailable(true);
        vehicles.save(v);
    }

    public Rental getById(Long rentalId) {
        return rentals.findById(rentalId).orElseThrow(() -> new IllegalArgumentException("Invalid rental id " + rentalId));
    }
}
