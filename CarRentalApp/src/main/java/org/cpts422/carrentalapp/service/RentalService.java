package org.cpts422.carrentalapp.service;

import jakarta.transaction.Transactional;
import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.model.Rental;
import org.cpts422.carrentalapp.model.Vehicle;
import org.cpts422.carrentalapp.repo.AppUserRepository;
import org.cpts422.carrentalapp.repo.RentalRepository;
import org.cpts422.carrentalapp.repo.VehicleRepository;
import org.cpts422.carrentalapp.service.error.UserNotFoundException;
import org.cpts422.carrentalapp.service.error.VehicleNotFoundException;
import org.cpts422.carrentalapp.service.error.VehicleUnavailableException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RentalService {

    private final VehicleRepository vehicleRepository;
    private final RentalRepository rentalRepository;
    private final AppUserRepository appUserRepository;
    private final PricingService pricing;

    public RentalService(VehicleRepository vehicleRepository,
                         RentalRepository rentalRepository,
                         AppUserRepository appUserRepository,
                         PricingService pricing) {
        this.vehicleRepository = vehicleRepository;
        this.rentalRepository = rentalRepository;
        this.appUserRepository = appUserRepository;
        this.pricing = pricing;
    }

    @Transactional
    public Rental createRental(Long userId, Long vehicleId, Integer days) {
        int d = (days == null) ? 1 : days.intValue();
        return createRental(userId, vehicleId, d);
    }

    @Transactional
    public Rental createRental(Long userId, Long vehicleId, int days) {
        if (days <= 0) throw new IllegalArgumentException("Days must be >= 1");

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));

        if (!vehicle.isAvailable()) throw new VehicleUnavailableException(vehicleId);

        LocalDate start = LocalDate.now();

        Rental r = new Rental();
        r.setUser(user);
        r.setVehicle(vehicle);
        r.setRentedAt(start);
        r.setExpectedReturnAt(start.plusDays(days));
        r.setReturnedAt(null);

        double charge = pricing.rentalTotal(user, vehicle, days);
        r.setDaysPaid(days);
        r.setTotalCharge(charge);

        vehicle.setAvailable(false);
        vehicleRepository.save(vehicle);

        return rentalRepository.save(r);
    }

    public List<Rental> findAllForUser(Long userId) {
        return rentalRepository.findByUserIdOrderByRentedAtDesc(userId);
    }

    public Rental getById(Long rentalId) {
        return rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid rental id: " + rentalId));
    }

    @Transactional
    public void markReturned(Rental rental, LocalDateTime now) {
        if (rental.getReturnedAt() == null) {
            rental.setReturnedAt(now.toLocalDate());
            rentalRepository.save(rental);

            Vehicle v = rental.getVehicle();
            if (!v.isAvailable()) {
                v.setAvailable(true);
                vehicleRepository.save(v);
            }
        }
    }
}