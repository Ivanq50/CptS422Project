package org.cpts422.carrentalapp.web;

import jakarta.servlet.http.HttpSession;
import org.cpts422.carrentalapp.service.RentalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RentalController {

    private final RentalService rentalService;

    public RentalController(RentalService rentalService) { this.rentalService = rentalService; }

    @GetMapping("/my-rentals")
    public String myRentals(Model model, HttpSession session) {
        Long uid = (Long) session.getAttribute("userId");
        if (uid == null) return "redirect:/login";
        model.addAttribute("rentals", rentalService.findAllForUser(uid));
        return "my_rentals";
    }
}
