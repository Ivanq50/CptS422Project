// Created by : Yevin
// Created on : Sep 28

// Last Updated by : Yevin
// Last Updated on : Sep 28

package org.cpts422.carrentalapp.web;

import jakarta.servlet.http.HttpSession;
import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.model.MembershipType;
import org.cpts422.carrentalapp.model.Rental;
import org.cpts422.carrentalapp.model.Vehicle;
import org.cpts422.carrentalapp.repo.AppUserRepository;
import org.cpts422.carrentalapp.repo.RentalRepository;
import org.cpts422.carrentalapp.repo.VehicleRepository;
import org.cpts422.carrentalapp.service.PricingService;
import org.cpts422.carrentalapp.web.cart.Cart;
import org.cpts422.carrentalapp.web.cart.CartItem;
import org.cpts422.carrentalapp.web.cart.CartItemType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
public class CartController
{

    private final Cart cart;
    private final VehicleRepository vehicles;
    private final AppUserRepository users;
    private final RentalRepository rentals;
    private final PricingService pricing;

    public CartController(Cart cart,
                          VehicleRepository vehicles,
                          AppUserRepository users,
                          RentalRepository rentals,
                          PricingService pricing)
    {
        this.cart = cart;
        this.vehicles = vehicles;
        this.users = users;
        this.rentals = rentals;
        this.pricing = pricing;
    }

    @GetMapping("/cart")
    public String cart(Model model, HttpSession session, RedirectAttributes ra)
    {
        if (session.getAttribute("userId") == null)
        {
            ra.addFlashAttribute("msg", "Please sign in to view your cart.");
            return "redirect:/login";
        }
        model.addAttribute("cart", cart);
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam long vehicleId,
                            @RequestParam int days,
                            HttpSession session,
                            RedirectAttributes ra)
    {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
        {
            ra.addFlashAttribute("msg", "Please sign in first.");
            return "redirect:/login";
        }

        if (days <= 0)
        {
            ra.addFlashAttribute("msg", "Days must be at least 1.");
            return "redirect:/vehicles";
        }

        Vehicle v = vehicles.findById(vehicleId).orElse(null);
        if (v == null)
        {
            ra.addFlashAttribute("msg", "Vehicle not found.");
            return "redirect:/vehicles";
        }
        if (!v.isAvailable())
        {
            ra.addFlashAttribute("msg", "Sorry, that vehicle is currently rented.");
            return "redirect:/vehicles";
        }

        AppUser user = users.findById(userId).orElseThrow();

        if (user.getAge() < 25 && "LUXURY".equals(v.getVehicleType().name()))
        {
            ra.addFlashAttribute("msg", "Drivers under 25 cannot rent LUXURY vehicles.");
            return "redirect:/vehicles";
        }

        double base = v.getDailyRate() * days;
        double discount = (user.getMembershipType() == MembershipType.PREMIUM) ? base * 0.10 : 0.0; // 10% off
        double surcharge = (user.getAge() < 25) ? base * 0.02 : 0.0; // +2% young renter
        double finalAmount = round2(base - discount + surcharge);

        CartItem it = new CartItem();
        it.setType(CartItemType.RENT);
        it.setVehicleId(v.getId());
        it.setVehicleLabel(v.getMake() + " " + v.getModel());
        it.setDays(days);

        it.setBaseAmount(round2(base));
        it.setDiscountAmount(round2(discount));
        it.setSurchargeAmount(round2(surcharge));
        it.setAmount(finalAmount);

        cart.addItem(it);
        ra.addFlashAttribute("msg", "Added to cart.");
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String remove(@RequestParam int index, HttpSession session, RedirectAttributes ra)
    {
        if (session.getAttribute("userId") == null)
        {
            ra.addFlashAttribute("msg", "Please sign in first.");
            return "redirect:/login";
        }
        cart.removeIndex(index);
        ra.addFlashAttribute("msg", "Item removed.");
        return "redirect:/cart";
    }

    @PostMapping("/cart/checkout-rent")
    public String checkoutRent(HttpSession session, RedirectAttributes ra) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            ra.addFlashAttribute("msg", "Please sign in first.");
            return "redirect:/login";
        }
        AppUser user = users.findById(userId).orElseThrow();

        if (user.getDriversLicenseExpiry() != null && !user.getDriversLicenseExpiry().isAfter(LocalDate.now())) {
            ra.addFlashAttribute("msg", "Your driver’s license is expired. You cannot rent a vehicle.");
            return "redirect:/cart";
        }

        if (!cart.hasRentItems()) {
            ra.addFlashAttribute("msg", "No rentals to checkout.");
            return "redirect:/cart";
        }

        long activeNow = rentals.countByUserIdAndReturnedAtIsNull(userId);
        long toAdd = cart.getItems().stream().filter(i -> i.getType() == CartItemType.RENT).count();
        int cap = (user.getMembershipType() == MembershipType.PREMIUM) ? 2 : 1;
        if (activeNow + toAdd > cap) {
            ra.addFlashAttribute("msg", "Rental limit reached for your membership. You can have up to " + cap + " active rental(s).");
            return "redirect:/cart";
        }

        for (CartItem it : cart.getItems()) {
            if (it.getType() != CartItemType.RENT) continue;
            Vehicle v = vehicles.findById(it.getVehicleId()).orElse(null);
            if (v == null) {
                ra.addFlashAttribute("msg", "A vehicle in your cart was not found.");
                return "redirect:/cart";
            }
            if (user.getAge() < 25 && v.getVehicleType().name().equals("LUXURY")) {
                ra.addFlashAttribute("msg", "Drivers under 25 cannot rent LUXURY vehicles.");
                return "redirect:/cart";
            }
            if (!v.isAvailable()) {
                ra.addFlashAttribute("msg", v.getMake() + " " + v.getModel() + " just became unavailable.");
                return "redirect:/cart";
            }
        }

        double total = round2(cart.getItems().stream()
                .filter(i -> i.getType() == CartItemType.RENT)
                .mapToDouble(CartItem::getAmount).sum());

        if (user.getWalletBalance() < total) {
            ra.addFlashAttribute("msg", "Insufficient funds. Add money on the Account page.");
            return "redirect:/cart";
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        for (CartItem it : cart.getItems()) {
            if (it.getType() != CartItemType.RENT) continue;

            Vehicle v = vehicles.findById(it.getVehicleId()).orElse(null);
            if (v == null || !v.isAvailable()) continue;

            Rental r = new Rental();
            r.setUser(user);
            r.setVehicle(v);
            r.setRentedAt(today);
            r.setExpectedReturnAt(today.plusDays(it.getDays()));
            r.setDaysPaid(it.getDays());
            r.setTotalCharge(it.getAmount());
            r.setRentedAtDateTime(now); // used for 2-hour rule
            rentals.save(r);

            v.setAvailable(false);
            vehicles.save(v);
        }

        user.setWalletBalance(round2(user.getWalletBalance() - total));
        users.save(user);

        cart.clearType(CartItemType.RENT);
        ra.addFlashAttribute("msg", "Rental checkout complete.");
        return "redirect:/my-rentals";
    }

    @PostMapping("/cart/checkout-return")
    public String checkoutReturn(HttpSession session, RedirectAttributes ra) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            ra.addFlashAttribute("msg", "Please sign in first.");
            return "redirect:/login";
        }
        AppUser user = users.findById(userId).orElseThrow();

        if (!cart.hasPenaltyItems()) {
            ra.addFlashAttribute("msg", "No penalties to pay.");
            return "redirect:/cart";
        }

        double total = round2(cart.getItems().stream()
                .filter(i -> i.getType() == CartItemType.PENALTY)
                .mapToDouble(CartItem::getAmount).sum());

        if (user.getWalletBalance() < total) {
            ra.addFlashAttribute("msg", "Insufficient funds. Add money on the Account page.");
            return "redirect:/cart";
        }

        for (CartItem it : cart.getItems()) {
            if (it.getType() != CartItemType.PENALTY) continue;

            Rental r = rentals.findById(it.getRentalId()).orElseThrow();
            r.setPenaltyCharge(it.getAmount());
            rentals.save(r);

            Vehicle v = r.getVehicle();
            v.setAvailable(true);
            vehicles.save(v);
        }

        user.setWalletBalance(round2(user.getWalletBalance() - total));
        users.save(user);

        cart.clearType(CartItemType.PENALTY);
        ra.addFlashAttribute("msg", "Penalty paid. Return complete.");
        return "redirect:/my-rentals";
    }

    private double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }
}