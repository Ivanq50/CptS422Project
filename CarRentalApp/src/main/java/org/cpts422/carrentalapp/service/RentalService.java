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

    // Constructor makes a new RentalService with all the repos to be used.
    public RentalService(VehicleRepository vehicleRepository, RentalRepository rentalRepository, AppUserRepository appUserRepository) {
        this.vehicleRepository = vehicleRepository;
        this.rentalRepository = rentalRepository;
        this.appUserRepository = appUserRepository;
    }

    // Fetches the list of all the vehicles that are available.
    public List<Vehicle> getAvailableVehicles() {
        return vehicleRepository.findByAvailable(true);
    }

    // This method processes the vehicle rental request.
    public void rentVehicle(Long userId, Long vehicleId, int rentalDays) {
        // Find the user and vehicle from the DB, throws an exception if they aren't found.
        AppUser user = appUserRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        // Checks for rental eligibility.
        if (!vehicle.isAvailable()) {
            throw new IllegalStateException("Vehicle is not available!");
        }
        if (user.getAge() < 25 && vehicle.getVehicleType() == VehicleType.LUXURY) {
            throw new IllegalStateException("User is not eligible to rent a luxury vehicle!");
        }

        // Creates and saves a new rental record.
        Rental rental = new Rental();
        rental.setUser(user);
        rental.setVehicle(vehicle);
        rental.setRentalDate(LocalDate.now());
        rental.setExpectedReturnDate(LocalDate.now().plusDays(rentalDays));
        rental.setTotalCharge(calculateRentalCharge(user, vehicle, rentalDays));

        // Update the vehicles availability.
        vehicle.setAvailable(false);
        vehicleRepository.save(vehicle);
        rentalRepository.save(rental);
    }

    // Returns a list of active rentals for the specific user.
    public List<Rental> getMyRentals(Long userId) {
        return rentalRepository.findByUserAndReturnDateIsNull(userId);
    }

    // Process the vehicle return.
    public void returnVehicle(Long rentalId) {
        // Finds the rental record.
        Rental rental = rentalRepository.findById(rentalId).orElseThrow(() -> new IllegalArgumentException("Rental not found!"));
        rental.setReturnDate(LocalDate.now());

        // Calculate and add any fees that are applicable.
        double lateFee = calculateLateFee(rental);
        rental.setTotalCharge(rental.getTotalCharge() + lateFee);

        // Make the vehicle status available after being returned.
        Vehicle vehicle = rental.getVehicle();
        vehicle.setAvailable(true);
        vehicleRepository.save(vehicle);
        rentalRepository.save(rental);
    }

    // Calculates the total charge based on the user details (membership, age, etc.).
    private double calculateRentalCharge(AppUser user, Vehicle vehicle, int rentalDays) {
        double total = vehicle.getDailyRate() * rentalDays;

        if (user.getMembershipType() == MembershipType.PREMIUM) {
            total *= 0.90; // 10% discount for premium members.
        }
        if (user.getAge() >= 21 && user.getAge() <= 24) {
            total *= 1.02; // 2% young renter fee.
        }
        return total;
    }

    // Calculate the late fees for the rental.
    private double calculateLateFee(Rental rental) {
        // Determine the number of days that are overdue.
        long overdueDays = ChronoUnit.DAYS.between(rental.getExpectedReturnDate(), rental.getReturnDate());

        // No fee is applied if it is returned on time or early.
        if (overdueDays <= 0) {
            return 0;
        }

        AppUser user = rental.getUser();
        if (user.getMembershipType() == MembershipType.PREMIUM && overdueDays == 1) {
            return 0; // Grace period for premium members
        }

        double penaltyRate = 0.50; // Standard rate for standard members
        if (user.getMembershipType() == MembershipType.PREMIUM) {
            penaltyRate = 0.25; // 25% for premium members
        }
        return overdueDays * (rental.getVehicle().getDailyRate() * penaltyRate);
    }
}