package org.cpts422.carrentalapp.controller;

import org.cpts422.carrentalapp.model.Rental;
import org.cpts422.carrentalapp.model.Vehicle;
import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.repo.RentalRepository;
import org.cpts422.carrentalapp.repo.VehicleRepository;
import org.cpts422.carrentalapp.repo.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/vehicles")
public class VehicleController{

    private final VehicleRepository vehicleRepository;
    private final RentalRepository rentalRepository;
    private final AppUserRepository appUserRepository;

    @Autowired
    public VehicleController(VehicleRepository vehicleRepository, RentalRepository rentalRepository, AppUserRepository appUserRepository) {
        this.vehicleRepository = vehicleRepository;
        this.rentalRepository = rentalRepository;
        this.appUserRepository = appUserRepository;
    }

    @GetMapping
    public String browseVehicles(Model model) {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        model.addAttribute("vehicles", vehicles);
        return "vehicles";
    }

    @GetMapping("/user/{userId}/rentals")
    public List<Rental> getUserRentals(@PathVariable Long userId) {
        // Fetch the user
        AppUser user = appUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch rentals for that user where returnDate is null
        List<Rental> rentals = rentalRepository.findByUserAndReturnDateIsNull(user);

        return rentals;
    }

    // Process rental
    @PostMapping("/rent/{id}")
    public String rentVehicle(@PathVariable Long id,
                              @RequestParam Long userId,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate rentalDate,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expectedReturnDate,
                              Model model) {

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        // Check if vehicle is available
        if (!vehicle.isAvailable()) {
            model.addAttribute("error", "Vehicle is not available for rent");
            return "rentVehicle";
        }

        // TODO: Add age/license/membership checks here

        // Create rental
        Rental rental = new Rental();
        rental.setUser(user);
        rental.setVehicle(vehicle);
        rental.setRentalDate(rentalDate);
        rental.setExpectedReturnDate(expectedReturnDate);
        rental.setReturnDate(null); // Not returned yet
        rental.setTotalCharge(0.0); // Calculate later

        rentalRepository.save(rental);

        // Mark vehicle as unavailable
        vehicle.setAvailable(false);
        vehicleRepository.save(vehicle);

        return "redirect:/vehicles";
    }
}