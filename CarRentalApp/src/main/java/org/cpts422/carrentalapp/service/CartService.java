package org.cpts422.carrentalapp.service;

import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.model.MembershipType;
import org.cpts422.carrentalapp.model.Rental;
import org.cpts422.carrentalapp.model.Vehicle;
import org.cpts422.carrentalapp.web.cart.Cart;
import org.cpts422.carrentalapp.web.cart.CartItem;
import org.cpts422.carrentalapp.web.cart.CartItemType;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    private final PricingService pricing;

    public CartService(PricingService pricing) { this.pricing = pricing; }

    public void addRentLine(Cart cart, AppUser user, Vehicle v, int days) {
        double base = v.getDailyRate() * days;
        double discount = (user.getMembershipType() == MembershipType.PREMIUM) ? base * 0.10 : 0.0;
        double surcharge = (user.getAge() < 25) ? base * 0.02 : 0.0;
        double finalAmount = round2(base - discount + surcharge);

        CartItem it = new CartItem();
        it.setType(CartItemType.RENT);
        it.setVehicleId(v.getId());
        it.setVehicleLabel(v.getMake() + " " + v.getModel());
        it.setDays(days);
        it.setAmount(finalAmount);
        cart.addItem(it);
    }

    public void addPenaltyLine(Cart cart, Rental rental, double penalty) {
        CartItem it = new CartItem();
        it.setType(CartItemType.PENALTY);
        it.setRentalId(rental.getId());
        it.setVehicleLabel(rental.getVehicle().getMake() + " " + rental.getVehicle().getModel() + " (late return)");
        it.setAmount(round2(penalty));
        cart.addItem(it);
    }

    public double total(Cart cart) {
        return cart.total();
    }

    private double round2(double x) { return Math.round(x * 100.0) / 100.0; }
}
