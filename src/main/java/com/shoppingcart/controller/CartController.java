package com.shoppingcart.controller;

import com.shoppingcart.model.Cart;
import com.shoppingcart.model.Item;
import com.shoppingcart.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/api")
public class CartController {

    private static Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    CartRepository cartRepository;

    @GetMapping
    public Flux<Cart> getAllCart() {
        return cartRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Cart>> getCart(@PathVariable String id) {
        logger.info("received id:" + id);

        return cartRepository.findById(id)
                .map(p -> ResponseEntity.ok(p))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/add/{id}")
    public Mono<Cart> addItem(@RequestBody Item item,
                              @PathVariable String id) {
        logger.info("Item: {}, \n Id:{}", item, id);

        return cartRepository.findById(id)
                .flatMap(cart -> {
                    List<Item> list = cart.getItemList();
                    if (list == null) {
                        list = new ArrayList<>();
                    }

                    list.add(item);
                    cart.setItemList(list);
                    logger.info("Updated Cart: {}", cart);
                    return cartRepository.save(cart);
                });
    }


    @PutMapping("/remove/{id}")
    public Mono<Cart> removeItem(@RequestBody Item item,
                                 @PathVariable String id) {
        logger.info("Item: {}, \n Id:{}", item, id);

        return cartRepository.findById(id)
                .flatMap(cart -> {
                    List<Item> list = cart.getItemList();
                    if (list == null) return Mono.just(cart);

                    list.removeIf(cartItem -> cartItem.getId().equals(item.getId()));

                    cart.setItemList(list);
                    return cartRepository.save(cart);
                });
    }

}
