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
        double base = round2(v.getDailyRate() * days);

        double discount = 0.0;
        if (user.getMembershipType() == MembershipType.PREMIUM) {
            discount = round2(base * 0.10);
        }

        double surcharge = 0.0;
        if (user.getAge() < 25) {
            surcharge = round2(base * 0.02);
        }

        double finalAmount = pricing.rentalTotal(user, v, days);

        CartItem it = new CartItem();
        it.setType(CartItemType.RENT);
        it.setVehicleId(v.getId());
        it.setVehicleLabel(v.getMake() + " " + v.getModel());
        it.setDays(days);

        // breakdown for UI stays intact
        it.setBaseAmount(base);
        it.setDiscountAmount(discount);
        it.setSurchargeAmount(surcharge);
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
        return round2(cart.getItems().stream()
                .mapToDouble(i -> i.getAmount() == null ? 0.0 : i.getAmount())
                .sum());
    }

    private double round2(double x) { return Math.round(x * 100.0) / 100.0; }
}
