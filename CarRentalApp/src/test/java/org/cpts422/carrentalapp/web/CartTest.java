package org.cpts422.carrentalapp.web;

import org.cpts422.carrentalapp.web.cart.Cart;
import org.cpts422.carrentalapp.web.cart.CartItem;
import org.cpts422.carrentalapp.web.cart.CartItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class CartTest
{
    private Cart cart;
    private CartItem rentItem;
    private CartItem penaltyItem1;
    private CartItem penaltyItem2;


    @BeforeEach
    void setup() {
        cart = new Cart();

        rentItem = new CartItem();
        rentItem.setType(CartItemType.RENT);
        rentItem.setAmount(100.0);

        penaltyItem1 = new CartItem();
        penaltyItem1.setType(CartItemType.PENALTY);
        penaltyItem1.setRentalId(1L);
        penaltyItem1.setAmount(50.0);

        penaltyItem2 = new CartItem();
        penaltyItem2.setType(CartItemType.PENALTY);
        penaltyItem2.setRentalId(2L);
        penaltyItem2.setAmount(25.0);
    }

    @Test
    void addItemRentNormal() {
        cart.addItem(rentItem);
        assertEquals(1, cart.count());
        assertTrue(cart.hasRentItems());
        assertFalse(cart.hasPenaltyItems());
    }

    @Test
    void hasRentItemsWhenNoRentItems() {
        cart.addItem(penaltyItem1);
        assertFalse(cart.hasRentItems());
    }

    @Test
    void addItemReplacePenalty() {
        cart.addItem(penaltyItem1);
        cart.addItem(penaltyItem2);
        assertEquals(2, cart.count());

        CartItem duplicatePenalty = new CartItem();
        duplicatePenalty.setType(CartItemType.PENALTY);
        duplicatePenalty.setRentalId(1L);
        duplicatePenalty.setAmount(99.0);

        cart.addItem(duplicatePenalty);
        assertEquals(2, cart.count());
        assertTrue(cart.hasPenaltyItems());
    }

    @Test
    void removeIndexRemoveValidIndex() {
        cart.addItem(rentItem);
        cart.addItem(penaltyItem1);

        cart.removeIndex(0);
        assertEquals(1, cart.count());
        assertTrue(cart.hasPenaltyItems());
    }

    @Test
    void removeIndexInvalid() {
        cart.addItem(rentItem);
        cart.removeIndex(-1);
        cart.removeIndex(5);
        assertEquals(1, cart.count());
    }

    @Test
    void clearTypeRemoveAll() {
        cart.addItem(rentItem);
        cart.addItem(penaltyItem1);
        cart.addItem(penaltyItem2);

        cart.clearType(CartItemType.PENALTY);
        assertEquals(1, cart.count());
        assertTrue(cart.hasRentItems());
        assertFalse(cart.hasPenaltyItems());
    }

     @Test
    void totalRoundToTwoDecimals() {
        CartItem i1 = new CartItem();
        i1.setAmount(10.555);
        i1.setType(CartItemType.RENT);
        CartItem i2 = new CartItem();
        i2.setAmount(5.333);
        i2.setType(CartItemType.RENT);

        cart.addItem(i1);
        cart.addItem(i2);

        assertEquals(15.89, cart.total());
    }

    @Test
    void countNumberOfItems() {
        cart.addItem(rentItem);
        cart.addItem(penaltyItem1);
        assertEquals(2, cart.count());
    }
}
