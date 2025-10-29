package org.cpts422.carrentalapp.service;

import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.model.MembershipType;
import org.cpts422.carrentalapp.model.Vehicle;
import org.cpts422.carrentalapp.model.Rental;
import org.cpts422.carrentalapp.repo.AppUserRepository;
import org.cpts422.carrentalapp.service.error.InsufficientFundsException;
import org.cpts422.carrentalapp.service.error.UserNotFoundException;
import org.cpts422.carrentalapp.web.cart.Cart;
import org.cpts422.carrentalapp.web.cart.CartItem;
import org.cpts422.carrentalapp.web.cart.CartItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {
    @Mock
    private PricingService pricingService; // for future use

    @InjectMocks
    private CartService cartService;

    private Cart cart;
    private AppUser user;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        cart = new Cart();

        user = new AppUser();
        user.setAge(30);
        user.setMembershipType(MembershipType.STANDARD);

        vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setMake("Toyota");
        vehicle.setModel("Camry");
        vehicle.setDailyRate(50.0);
    }

    @Test
    void testAddRentLineStandardUserAdult() {
        cartService.addRentLine(cart, user, vehicle, 3);

        assertEquals(1, cart.getItems().size());

        CartItem it = cart.getItems().get(0);
        assertEquals(CartItemType.RENT, it.getType());
        assertEquals(vehicle.getId(), it.getVehicleId());
        assertEquals("Toyota Camry", it.getVehicleLabel());
        assertEquals(3, it.getDays());
        assertEquals(150.0, it.getBaseAmount());
        assertEquals(0.0, it.getDiscountAmount());
        assertEquals(0.0, it.getSurchargeAmount());
        assertEquals(150.0, it.getAmount());
    }

    @Test
    void testAddRentLinePremiumUserUnder25() {
        user.setMembershipType(MembershipType.PREMIUM);
        user.setAge(22);

        cartService.addRentLine(cart, user, vehicle, 4);

        CartItem it = cart.getItems().get(0);
        assertEquals(200.0, it.getBaseAmount());
        assertEquals(20.0, it.getDiscountAmount());
        assertEquals(4.0, it.getSurchargeAmount());
        assertEquals(184.0, it.getAmount());
    }

    @Test
    void testAddPenaltyLine() {
        Rental rental = new Rental();
        rental.setId(1L);
        rental.setVehicle(vehicle);

        cartService.addPenaltyLine(cart, rental, 25.0);

        CartItem it = cart.getItems().get(0);
        assertEquals(CartItemType.PENALTY, it.getType());
        assertEquals(rental.getId(), it.getRentalId());
        assertEquals("Toyota Camry (late return)", it.getVehicleLabel());
        assertEquals(25.0, it.getAmount());
    }

     @Test
    void testTotalEmptyCart() {
        assertEquals(0.0, cartService.total(cart));
    }

    @Test
    void testTotalWithItems() {
        CartItem item1 = new CartItem();
        item1.setAmount(50.0);
        cart.addItem(item1);

        CartItem item2 = new CartItem();
        item2.setAmount(25.0);
        cart.addItem(item2);

        CartItem item3 = new CartItem();
        item3.setAmount(null); // should count as 0
        cart.addItem(item3);

        assertEquals(75.0, cartService.total(cart));
    }

}
