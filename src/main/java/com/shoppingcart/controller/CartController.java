package com.shoppingcart.controller;

import com.shoppingcart.model.Cart;
import com.shoppingcart.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;


@RestController
@RequestMapping("/api")
public class CartController {

    private static Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    CartRepository cartRepository;

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Cart>> getProduct(@PathVariable String id) {
        logger.info("received id:" + id);

        return cartRepository.findById(id)
                .map(p -> ResponseEntity.ok(p))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Flux<Cart> getAllProduct() {
        return cartRepository.findAll();
    }

}
