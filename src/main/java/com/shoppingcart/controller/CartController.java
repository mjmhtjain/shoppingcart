package com.shoppingcart.controller;

import com.shoppingcart.model.Cart;
import com.shoppingcart.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    Logger log = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartRepository cartRepository;

    @GetMapping("/fetchAll")
    public Flux<Cart> fetchAll() {
        return cartRepository.findAll();
    }

    @GetMapping("/fetchByCartId/{cartid}")
    public Flux<Cart> fetchByCartId(@PathVariable int cartid) {
        log.info("fetchByCartId, cartid: {}", cartid);

        return cartRepository.fetchByCartId(cartid);
    }
}
