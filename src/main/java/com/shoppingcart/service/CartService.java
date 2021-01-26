package com.shoppingcart.service;

import com.shoppingcart.model.Cart;
import com.shoppingcart.model.CartItemValidationResponseEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartService {
    Flux<Cart> findAll();

    Flux<Cart> fetchByCartId(int cartid);

    Mono<Boolean> updateCart(int cartid, int itemid, int quantity);

    void updateCartItemQuantity(CartItemValidationResponseEvent cartItemValidationResponseEvent);
}
