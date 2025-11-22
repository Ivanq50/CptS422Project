package org.cpts422.carrentalapp.Integration;

import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.model.MembershipType;
import org.cpts422.carrentalapp.model.Rental;
import org.cpts422.carrentalapp.model.Vehicle;

import org.cpts422.carrentalapp.service.CartService;
import org.cpts422.carrentalapp.service.PricingService;
import org.cpts422.carrentalapp.web.cart.Cart;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class CartPricingIntegrationTest {

    @Autowired
    CartService cartService;

    @Autowired
    PricingService pricingService;

    @Test
    void cartAndPricingMatchingTotals() {
        Cart cart = new Cart();
        AppUser user = new AppUser();
        user.setMembershipType(MembershipType.PREMIUM);
        user.setAge(22);

        Vehicle v = new Vehicle();
        v.setDailyRate(100.0);

        cartService.addRentLine(cart, user, v, 3);

        double expected = pricingService.rentalTotal(user, v, 3);
        double actual = cart.getItems().get(0).getAmount();

        assertEquals(expected, actual);
    }

    @Test
    void standardAdultUserPricingMatches() {
        Cart cart = new Cart();
        AppUser user = new AppUser();
        user.setMembershipType(MembershipType.STANDARD);
        user.setAge(30);

        Vehicle v = new Vehicle();
        v.setDailyRate(80.0);

        cartService.addRentLine(cart, user, v, 5);

        double expected = pricingService.rentalTotal(user, v, 5);
        double actual = cart.getItems().get(0).getAmount();

        assertEquals(expected, actual);
    }

    @Test
    void premiumAdultUserPricingMatches() {
        Cart cart = new Cart();
        AppUser user = new AppUser();
        user.setMembershipType(MembershipType.PREMIUM);
        user.setAge(30);

        Vehicle v = new Vehicle();
        v.setDailyRate(120.0);

        cartService.addRentLine(cart, user, v, 2);

        assertEquals(
                pricingService.rentalTotal(user, v, 2),
                cart.getItems().get(0).getAmount()
        );
    }

    @Test
    void youngStandardUserPricingMatches() {
        Cart cart = new Cart();
        AppUser user = new AppUser();
        user.setMembershipType(MembershipType.STANDARD);
        user.setAge(21);

        Vehicle v = new Vehicle();
        v.setDailyRate(50.0);

        cartService.addRentLine(cart, user, v, 4);

        assertEquals(
                pricingService.rentalTotal(user, v, 4),
                cart.getItems().get(0).getAmount()
        );
    }

    @Test
    void cartAndPricingMatchingPenaltyTotals() {
        Cart cart = new Cart();

        AppUser user = new AppUser();
        user.setMembershipType(MembershipType.PREMIUM);
        user.setAge(30);

        Vehicle v = new Vehicle();
        v.setDailyRate(100.0);

        Rental rental = new Rental();
        rental.setUser(user);
        rental.setVehicle(v);
        rental.setDaysPaid(2);
        rental.setRentedAt(LocalDate.now().minusDays(4));

        double penalty = pricingService.penaltyForReturn(rental, LocalDateTime.now());

        cartService.addPenaltyLine(cart, rental, penalty);

        assertEquals(penalty, cart.getItems().get(0).getAmount());
    }

}
