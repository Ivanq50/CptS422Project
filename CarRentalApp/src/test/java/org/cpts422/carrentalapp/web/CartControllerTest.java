package org.cpts422.carrentalapp.web;

import org.cpts422.carrentalapp.model.*;
import org.cpts422.carrentalapp.service.*;
import org.cpts422.carrentalapp.web.cart.Cart;
import org.cpts422.carrentalapp.web.cart.CartItem;
import org.cpts422.carrentalapp.web.cart.CartItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.cpts422.carrentalapp.service.error.InsufficientFundsException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartControllerTest {

    @InjectMocks
    CartController controller;

    @Mock
    CartService cartService;

    @Mock
    AccountService accountService;

    @Mock
    VehicleService vehicleService;

    @Mock
    RentalService rentalService;

    @Mock
    PricingService pricingService;

    @Mock
    Model model;

    @Mock
    RedirectAttributes ra;

    Cart cart;
    MockHttpSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cart = new Cart();
        session = new MockHttpSession();

        controller = new CartController(cart, cartService, accountService, vehicleService, rentalService, pricingService);
    }

    @Test
    void showCart() {
        List<CartItem> items = new ArrayList<>();
        when(cartService.total(cart)).thenReturn(50.0);

        String view = controller.show(model);

        verify(model).addAttribute("cart", cart);
        verify(model).addAttribute("total", 50.0);
        assertEquals("cart", view);
    }

    @Test
    void addRentRedirectToLoginWhenNoUser() {
        String view = controller.addRent(session, 1L, 3, ra);
        assertEquals("redirect:/login", view);
    }

    @Test
    void addRentNegativeDays() {
        session.setAttribute("userId", 1L);
        AppUser user = new AppUser();
        when(accountService.getById(1L)).thenReturn(user);
        Vehicle vehicle = new Vehicle();
        when(vehicleService.get(1L)).thenReturn(vehicle);

        String view = controller.addRent(session, 1L, 0, ra);

        verify(ra).addFlashAttribute("error", "Days must be >= 1");
        assertEquals("redirect:/vehicles", view);
    }

    @Test
    void addRentSuccess() {
        session.setAttribute("userId", 1L);
        AppUser user = new AppUser();
        Vehicle vehicle = new Vehicle();
        when(accountService.getById(1L)).thenReturn(user);
        when(vehicleService.get(1L)).thenReturn(vehicle);

        String view = controller.addRent(session, 1L, 3, ra);

        verify(cartService).addRentLine(cart, user, vehicle, 3);
        verify(ra).addFlashAttribute("msg", "Added to cart.");
        assertEquals("redirect:/cart", view);
    }

    @Test
    void checkoutRentRedirectToLoginWhenNoUser() {
        String view = controller.checkoutRent(session, ra);
        assertEquals("redirect:/login", view);
    }

    @Test
    void checkoutRentInsufficientFunds() throws InsufficientFundsException {
        session.setAttribute("userId", 1L);
        when(cartService.total(cart)).thenReturn(100.0);
        doThrow(new InsufficientFundsException(100.0, 50.0)).when(accountService).debit(1L, 100.0);

        String view = controller.checkoutRent(session, ra);

        verify(ra).addFlashAttribute(eq("error"), anyString());
        assertEquals("redirect:/cart", view);
    }

    @Test
    void checkoutRentSuccess() throws InsufficientFundsException {
        session.setAttribute("userId", 1L);
        CartItem item = new CartItem();
        item.setType(CartItemType.RENT);
        item.setVehicleId(1L);
        item.setDays(2);
        cart.addItem(item);

        when(cartService.total(cart)).thenReturn(50.0);

        String view = controller.checkoutRent(session, ra);

        verify(accountService).debit(1L, 50.0);
        rentalService.createRental(1L, 2L, null);
        assertEquals("redirect:/my-rentals", view);
    }

    @Test
    void removeItem() {
        cart.addItem(new CartItem());
        String view = controller.remove(0);
        assertEquals("redirect:/cart", view);
        assertEquals(0, cart.getItems().size());
    }

    @Test
    void addPenaltyRedirectToLogin() {
        String view = controller.addPenalty(1L, ra, session);
        assertEquals("redirect:/login", view);
    }

    @Test
    void addPenaltyNoPenalty() {
        session.setAttribute("userId", 1L);
        AppUser user = new AppUser();
        when(accountService.getById(1L)).thenReturn(user);
        Rental rental = new Rental();
        when(rentalService.getById(1L)).thenReturn(rental);
        when(pricingService.computeLatePenalty(rental, user, pricingService.now())).thenReturn(0.0);

        String view = controller.addPenalty(1L, ra, session);

        verify(rentalService).markReturned(rental, pricingService.now());
        verify(ra).addFlashAttribute("msg", "Vehicle returned. No penalty due.");
        assertEquals("redirect:/my-rentals", view);
    }

    @Test
    void addPenaltyWithPenalty() {
        session.setAttribute("userId", 1L);
        AppUser user = new AppUser();
        when(accountService.getById(1L)).thenReturn(user);
        Rental rental = new Rental();
        when(rentalService.getById(1L)).thenReturn(rental);
        when(pricingService.computeLatePenalty(rental, user, pricingService.now())).thenReturn(20.0);

        String view = controller.addPenalty(1L, ra, session);

        verify(cartService).addPenaltyLine(cart, rental, 20.0);
        verify(ra).addFlashAttribute("msg", "Late return penalty added to cart.");
        assertEquals("redirect:/cart", view);
    }

    @Test
    void checkoutPenaltyAndReturnRedirectToLogin() {
        String view = controller.checkoutPenaltyAndReturn(1L, ra, session);
        assertEquals("redirect:/login", view);
    }

    @Test
    void checkoutPenaltyAndReturnInsufficientFunds() throws InsufficientFundsException {
        session.setAttribute("userId", 1L);
        when(cartService.total(cart)).thenReturn(100.0);
        doThrow(new InsufficientFundsException(100.0, 50.0)).when(accountService).debit(1L, 100.0);

        String view = controller.checkoutPenaltyAndReturn(1L, ra, session);

        verify(ra).addFlashAttribute(eq("error"), anyString());
        assertEquals("redirect:/cart", view);
    }

    @Test
    void checkoutPenaltyAndReturnSuccess() throws InsufficientFundsException {
        session.setAttribute("userId", 1L);
        when(cartService.total(cart)).thenReturn(50.0);
        Rental rental = new Rental();
        when(rentalService.getById(1L)).thenReturn(rental);

        // Add a penalty item to cart so we can check it gets cleared
        CartItem penaltyItem = new CartItem();
        penaltyItem.setType(CartItemType.PENALTY);
        cart.addItem(penaltyItem);

        String view = controller.checkoutPenaltyAndReturn(1L, ra, session);

        verify(accountService).debit(1L, 50.0);
        verify(rentalService).markReturned(rental, pricingService.now());
        verify(ra).addFlashAttribute("msg", "Penalty paid and vehicle returned.");

        assertTrue(cart.getItems().isEmpty());

        assertEquals("redirect:/my-rentals", view);
    }

    @Test
    void addPenaltyGetDelegatesToAddPenalty() {
        CartController spyController = spy(controller);
        doReturn("redirect:/somewhere")
                .when(spyController)
                .addPenalty(anyLong(), any(), any());

        String result = spyController.addPenaltyGet(1L, ra, session);

        verify(spyController).addPenalty(1L, ra, session);
        assertEquals("redirect:/somewhere", result);
    }

}
