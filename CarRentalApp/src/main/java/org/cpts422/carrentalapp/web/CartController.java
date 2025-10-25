package org.cpts422.carrentalapp.web;

import jakarta.servlet.http.HttpSession;
import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.model.Rental;
import org.cpts422.carrentalapp.model.Vehicle;
import org.cpts422.carrentalapp.service.*;
import org.cpts422.carrentalapp.web.cart.Cart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final Cart cart;
    private final CartService carts;
    private final AccountService accounts;
    private final VehicleService vehicles;
    private final RentalService rentals;
    private final PricingService pricing;

    public CartController(Cart cart, CartService carts, AccountService accounts,
                          VehicleService vehicles, RentalService rentals, PricingService pricing) {
        this.cart = cart; this.carts = carts; this.accounts = accounts;
        this.vehicles = vehicles; this.rentals = rentals; this.pricing = pricing;
    }

    @GetMapping
    public String show(Model model) {
        model.addAttribute("cart", cart);
        model.addAttribute("total", carts.total(cart));
        return "cart";
    }

    @PostMapping("/add-rent")
    public String addRent(HttpSession session,
                          @RequestParam Long vehicleId,
                          @RequestParam int days,
                          RedirectAttributes ra) {
        Long uid = (Long) session.getAttribute("userId");
        if (uid == null) return "redirect:/login";

        AppUser user = accounts.getById(uid);
        Vehicle v = vehicles.get(vehicleId);

        if (days <= 0) { ra.addFlashAttribute("msg", "Days must be >= 1"); return "redirect:/vehicles"; }
        if (!Boolean.TRUE.equals(v.getAvailable())) { ra.addFlashAttribute("msg", "Vehicle not available"); return "redirect:/vehicles"; }

        carts.addRentLine(cart, user, v, days);
        ra.addFlashAttribute("msg", "Added to cart.");
        return "redirect:/cart";
    }

    @PostMapping("/checkout-rent")
    public String checkoutRent(HttpSession session, RedirectAttributes ra) {
        Long uid = (Long) session.getAttribute("userId");
        if (uid == null) return "redirect:/login";

        double amount = carts.total(cart);
        accounts.debit(uid, amount);

        cart.getItems().stream().filter(i -> i.getType().name().equals("RENT")).forEach(it -> {
            rentals.createRental(uid, it.getVehicleId(), it.getDays());
        });

        cart.clearType(org.cpts422.carrentalapp.web.cart.CartItemType.RENT);
        ra.addFlashAttribute("msg", "Rental(s) confirmed.");
        return "redirect:/my_rentals";
    }

    @PostMapping("/add-penalty")
    public String addPenalty(@RequestParam Long rentalId, RedirectAttributes ra, HttpSession session) {
        Long uid = (Long) session.getAttribute("userId");
        if (uid == null) return "redirect:/login";
        AppUser user = accounts.getById(uid);

        Rental r = rentals.getById(rentalId);
        double penalty = pricing.computeLatePenalty(r, user, pricing.now());
        if (penalty <= 0) { ra.addFlashAttribute("msg", "No penalty due."); return "redirect:/cart"; }

        carts.addPenaltyLine(cart, r, penalty);
        ra.addFlashAttribute("msg", "Late return penalty added to cart.");
        return "redirect:/cart";
    }

    @PostMapping("/checkout-penalty-and-return")
    public String checkoutPenaltyAndReturn(@RequestParam Long rentalId,
                                           RedirectAttributes ra,
                                           HttpSession session) {
        Long uid = (Long) session.getAttribute("userId");
        if (uid == null) return "redirect:/login";

        double amount = carts.total(cart);
        accounts.debit(uid, amount);
        cart.clearType(org.cpts422.carrentalapp.web.cart.CartItemType.PENALTY);

        Rental r = rentals.getById(rentalId);
        rentals.markReturned(r, pricing.now());

        ra.addFlashAttribute("msg", "Penalty paid and vehicle returned.");
        return "redirect:/my_rentals";
    }

    @PostMapping("/remove/{idx}")
    public String remove(@PathVariable int idx) {
        cart.removeIndex(idx);
        return "redirect:/cart";
    }
}
