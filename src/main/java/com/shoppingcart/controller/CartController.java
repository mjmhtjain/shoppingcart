package com.shoppingcart.controller;

import com.google.gson.Gson;
import com.shoppingcart.model.Cart;
import com.shoppingcart.model.CartItemValidationEvent;
import com.shoppingcart.model.CartItemValidationResponseEvent;
import com.shoppingcart.repository.CartRepository;
import com.shoppingcart.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    Logger log = LoggerFactory.getLogger(CartController.class);
    private final String validateCartResponseTopic = "validate_cart_response_topic";
    private final String validateCartTopic = "validate_cart_topic";
    private final String validateCartTopic_Key = "validate_cart_topic_key";

    @Autowired
    private CartService cartService;

    @Autowired
    private Gson jsonConverter;

    @GetMapping("/fetchAll")
    public Flux<Cart> fetchAll() {
        log.info("fetchAll");
        return cartService.findAll();
    }

    @GetMapping("/fetchByCartId/{cartid}")
    public Flux<Cart> fetchByCartId(@PathVariable int cartid) {
        log.info("fetchByCartId, cartid: {}", cartid);

        return cartService.fetchByCartId(cartid);
    }

    @GetMapping("/update/{cartid}/{itemid}/{quantity}")
    public Mono<Boolean> updateCart(@PathVariable int cartid,
                                    @PathVariable int itemid,
                                    @PathVariable int quantity) {
        log.info("updateCart with cartid: {}, itemid: {}, quantity: {}", cartid, itemid, quantity);

        return cartService.updateCart(cartid, itemid, quantity);
    }

    @KafkaListener(topics = validateCartResponseTopic)
    public void validatedCartResponse(String stringifiedEvent) {
        log.info("KafkaListener from validateCartResponseTopic: {}", stringifiedEvent);
        CartItemValidationResponseEvent cartItemValidationResponseEvent = (CartItemValidationResponseEvent)
                jsonConverter.fromJson(stringifiedEvent, CartItemValidationResponseEvent.class);

        cartService.updateCartItemQuantity(cartItemValidationResponseEvent);
    }

}
