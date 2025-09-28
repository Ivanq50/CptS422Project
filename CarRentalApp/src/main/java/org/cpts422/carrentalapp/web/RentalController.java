package org.cpts422.carrentalapp.web;

import jakarta.servlet.http.HttpSession;
import org.cpts422.carrentalapp.model.Rental;
import org.cpts422.carrentalapp.model.Vehicle;
import org.cpts422.carrentalapp.repo.RentalRepository;
import org.cpts422.carrentalapp.repo.VehicleRepository;
import org.cpts422.carrentalapp.service.PricingService;
import org.cpts422.carrentalapp.web.cart.Cart;
import org.cpts422.carrentalapp.web.cart.CartItem;
import org.cpts422.carrentalapp.web.cart.CartItemType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class RentalController {

    private final RentalRepository rentals;
    private final VehicleRepository vehicles;
    private final PricingService pricing;
    private final Cart cart;

    public RentalController(RentalRepository rentals,
                            VehicleRepository vehicles,
                            PricingService pricing,
                            Cart cart) {
        this.rentals = rentals;
        this.vehicles = vehicles;
        this.pricing = pricing;
        this.cart = cart;
    }

    @GetMapping("/my-rentals")
    public String myRentals(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        List<Rental> list = rentals.findByUserIdAndReturnedAtIsNull(userId);
        model.addAttribute("rentals", list);
        return "my_rentals";
    }

    @PostMapping("/rentals/{id}/start-return")
    public String startReturn(@PathVariable Long id, RedirectAttributes ra) {
        Rental r = rentals.findById(id).orElseThrow();
        if (r.getReturnedAt() != null) {
            ra.addFlashAttribute("msg", "This rental has already been returned.");
            return "redirect:/my-rentals";
        }

        LocalDateTime now = pricing.now();
        double penalty = pricing.penaltyForReturn(r, now);

        if (penalty <= 0.0) {
            r.setReturnedAt(now.toLocalDate());
            r.setReturnedAtDateTime(now);
            rentals.save(r);

            Vehicle v = r.getVehicle();
            v.setAvailable(true);
            vehicles.save(v);

            ra.addFlashAttribute("msg", "Returned on time — no penalty.");
            return "redirect:/my-rentals";
        }

        CartItem it = new CartItem();
        it.setType(CartItemType.PENALTY);
        it.setRentalId(r.getId());
        it.setVehicleLabel(r.getVehicle().getMake() + " " + r.getVehicle().getModel() + " (late return)");
        it.setAmount(penalty);
        cart.addItem(it);
        r.setReturnedAt(now.toLocalDate());
        r.setReturnedAtDateTime(now);
        rentals.save(r);

        ra.addFlashAttribute("msg", "Late return penalty added to cart. Please pay to complete.");
        return "redirect:/cart";
    }
}