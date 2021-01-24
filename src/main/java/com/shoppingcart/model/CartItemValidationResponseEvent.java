package com.shoppingcart.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartItemValidationResponseEvent {
    private int cartid;
    private int itemid;
    private int quantity;
    private boolean isValid;
}
