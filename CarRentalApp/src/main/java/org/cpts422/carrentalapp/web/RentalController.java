package org.cpts422.carrentalapp.web;

import jakarta.servlet.http.HttpSession;
import org.cpts422.carrentalapp.repo.RentalRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RentalController {
    private final RentalRepository rentals;

    public RentalController(RentalRepository rentals) { this.rentals = rentals; }

    @GetMapping("/my_rentals")
    public String myRentals(Model model, HttpSession session) {
        Long uid = (Long) session.getAttribute("userId");
        if (uid == null) return "redirect:/login";
        model.addAttribute("rentals", rentals.findByUserId(uid));
        return "my_rentals";
    }
}
