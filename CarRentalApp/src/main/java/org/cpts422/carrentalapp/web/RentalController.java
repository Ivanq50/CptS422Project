package org.cpts422.carrentalapp.web;

import jakarta.servlet.http.HttpSession;
import org.cpts422.carrentalapp.service.RentalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RentalController {
    private final RentalService rentalService;

    // Constructor makes a new rental controller with  a RentalService
    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    // Handles GET requests for the "/cars" page. This will display the available vehicles.
    @GetMapping("/cars")
    public String availableCars(Model model) {
        model.addAttribute("vehicles", rentalService.getAvailableVehicles());
        return "cars";
    }

    @PostMapping("/rent")
    public String rentCar(@RequestParam Long vehicleId,@RequestParam int rentalDays, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        rentalService.rentVehicle(userId, vehicleId, rentalDays);
        return "redirect:/my-rentals";
    }

    @GetMapping("/my-rentals")
    public String myRentals(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("rentals", rentalService.getMyRentals(userId));
        return "my-rentals";
    }

    @PostMapping("/return")
    public String returnRental(@RequestParam Long rentalId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        rentalService.returnVehicle(rentalId);
        return "redirect:/my-rentals";
    }
}