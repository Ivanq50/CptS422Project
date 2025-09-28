// Created by : Yevin
// Created on : Sep 28

// Last Updated by : Yevin
// Last Updated on : Sep 28

package org.cpts422.carrentalapp.web.cart;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Cart
{
    private final List<CartItem> items = new ArrayList<>();

    public List<CartItem> getItems() { return items; }

    public void addItem(CartItem it)
    {
        if (it.getType() == CartItemType.PENALTY)
        {
            items.removeIf(x -> x.getType()==CartItemType.PENALTY && Objects.equals(x.getRentalId(), it.getRentalId()));
        }

        items.add(it);
    }

    public void removeIndex(int idx) { if (idx>=0 && idx<items.size()) items.remove(idx); }
    public void clearType(CartItemType t) { items.removeIf(i -> i.getType()==t); }

    public double total() { return Math.round(items.stream().mapToDouble(CartItem::getAmount).sum() * 100.0) / 100.0; }
    public boolean hasRentItems() { return items.stream().anyMatch(i -> i.getType()==CartItemType.RENT); }
    public boolean hasPenaltyItems() { return items.stream().anyMatch(i -> i.getType()==CartItemType.PENALTY); }
    public int count() { return items.size(); }
}

