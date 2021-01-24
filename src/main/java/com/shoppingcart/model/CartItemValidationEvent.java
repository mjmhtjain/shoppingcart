package com.shoppingcart.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartItemValidationEvent {
    private int cartid;
    private int itemid;
    private int quantity;
}
