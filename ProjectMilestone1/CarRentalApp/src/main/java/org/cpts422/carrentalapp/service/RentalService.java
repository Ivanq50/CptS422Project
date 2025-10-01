package org.cpts422.carrentalapp.service;

import org.cpts422.carrentalapp.model.*;
import org.cpts422.carrentalapp.repo.AppUserRepository;
import org.cpts422.carrentalapp.repo.VehicleRepository;
import org.cpts422.carrentalapp.repo.RentalRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RentalService {

    private final VehicleRepository vehicleRepository;
    private final RentalRepository rentalRepository;
    private final AppUserRepository appUserRepository;

    public RentalService(VehicleRepository vehicleRepository,
                         RentalRepository rentalRepository,
                         AppUserRepository appUserRepository) {
        this.vehicleRepository = vehicleRepository;
        this.rentalRepository = rentalRepository;
        this.appUserRepository = appUserRepository;
    }

    public List<Vehicle> getAvailableVehicles() {
        return vehicleRepository.findByAvailable(true);
    }

    public void rentVehicle(Long userId, Long vehicleId, int rentalDays) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        if (!vehicle.isAvailable()) {
            throw new IllegalStateException("Vehicle is not available");
        }
        if (user.getAge() < 25 && vehicle.getVehicleType() == VehicleType.LUXURY) {
            throw new IllegalStateException("User is not eligible to rent a luxury vehicle");
        }

        LocalDate start = LocalDate.now();
        LocalDate due = start.plusDays(rentalDays);

        Rental rental = new Rental();
        rental.setUser(user);
        rental.setVehicle(vehicle);
        rental.setRentedAt(start);
        rental.setExpectedReturnAt(due);
        rental.setReturnedAt(null);
        rental.setTotalCharge(calculateRentalCharge(user, vehicle, rentalDays));

        // mark car unavailable & save both
        vehicle.setAvailable(false);
        vehicleRepository.save(vehicle);
        rentalRepository.save(rental);
    }

    public List<Rental> getMyRentals(Long userId) {
        return rentalRepository.findByUserIdAndReturnedAtIsNull(userId);
    }

    public void returnVehicle(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Rental not found"));

        rental.setReturnedAt(LocalDate.now());

        double lateFee = calculateLateFee(rental);
        rental.setTotalCharge(rental.getTotalCharge() + lateFee);
        rental.setPenaltyCharge(lateFee);

        Vehicle vehicle = rental.getVehicle();
        vehicle.setAvailable(true);

        vehicleRepository.save(vehicle);
        rentalRepository.save(rental);
    }

    private double calculateRentalCharge(AppUser user, Vehicle vehicle, int rentalDays) {
        double total = vehicle.getDailyRate() * rentalDays;
        if (user.getMembershipType() == MembershipType.PREMIUM) {
            total *= 0.90; // 10% off
        }
        if (user.getAge() >= 21 && user.getAge() <= 24) {
            total *= 1.02; // 2% young renter fee
        }
        return total;
    }

    private double calculateLateFee(Rental rental) {
        LocalDate due = rental.getExpectedReturnAt();
        LocalDate returned = rental.getReturnedAt();

        long overdueDays = ChronoUnit.DAYS.between(due, returned);
        if (overdueDays <= 0) return 0;

        AppUser user = rental.getUser();

        if (user.getMembershipType() == MembershipType.PREMIUM && overdueDays == 1) {
            return 0; // 1-day grace for premium
        }

        double penaltyRate = (user.getMembershipType() == MembershipType.PREMIUM) ? 0.25 : 0.50;
        return overdueDays * (rental.getVehicle().getDailyRate() * penaltyRate);
    }
}
