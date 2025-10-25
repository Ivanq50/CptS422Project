package org.cpts422.carrentalapp.web;

import jakarta.servlet.http.HttpSession;
import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.model.Rental;
import org.cpts422.carrentalapp.model.Vehicle;
import org.cpts422.carrentalapp.service.*;
import org.cpts422.carrentalapp.service.error.InsufficientFundsException;
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

        if (days <= 0) {
            ra.addFlashAttribute("error", "Days must be >= 1");
            return "redirect:/vehicles";
        }

        carts.addRentLine(cart, user, v, days);
        ra.addFlashAttribute("msg", "Added to cart.");
        return "redirect:/cart";
    }

    @PostMapping("/checkout-rent")
    public String checkoutRent(HttpSession session, RedirectAttributes ra) {
        Long uid = (Long) session.getAttribute("userId");
        if (uid == null) return "redirect:/login";

        double amount = carts.total(cart);
        try {
            accounts.debit(uid, amount);
        } catch (org.cpts422.carrentalapp.service.error.InsufficientFundsException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/cart";
        }

        cart.getItems().stream()
                .filter(i -> i.getType().name().equals("RENT"))
                .forEach(it -> rentals.createRental(uid, it.getVehicleId(), it.getDays()));

        cart.clearType(org.cpts422.carrentalapp.web.cart.CartItemType.RENT);
        ra.addFlashAttribute("msg", "Rental(s) confirmed.");
        return "redirect:/my-rentals";
    }


    @GetMapping("/add-penalty")
    public String addPenaltyGet(@RequestParam Long rentalId,
                                RedirectAttributes ra,
                                HttpSession session) {
        return addPenalty(rentalId, ra, session);
    }

    @PostMapping("/add-penalty")
    public String addPenalty(@RequestParam Long rentalId,
                             RedirectAttributes ra,
                             HttpSession session) {
        Long uid = (Long) session.getAttribute("userId");
        if (uid == null) return "redirect:/login";
        AppUser user = accounts.getById(uid);

        Rental r = rentals.getById(rentalId);
        double penalty = pricing.computeLatePenalty(r, user, pricing.now());

        if (penalty <= 0.0) {
            rentals.markReturned(r, pricing.now());
            ra.addFlashAttribute("msg", "Vehicle returned. No penalty due.");
            return "redirect:/my-rentals";
        }

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
        try {
            accounts.debit(uid, amount);
        } catch (InsufficientFundsException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/cart";
        }

        cart.clearType(org.cpts422.carrentalapp.web.cart.CartItemType.PENALTY);

        Rental r = rentals.getById(rentalId);
        rentals.markReturned(r, pricing.now());

        ra.addFlashAttribute("msg", "Penalty paid and vehicle returned.");
        return "redirect:/my-rentals";
    }

    @PostMapping("/remove/{idx}")
    public String remove(@PathVariable int idx) {
        cart.removeIndex(idx);
        return "redirect:/cart";
    }
}