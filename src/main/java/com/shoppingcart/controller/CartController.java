package com.shoppingcart.controller;

import com.shoppingcart.model.Cart;
import com.shoppingcart.model.Item;
import com.shoppingcart.repository.CartRepository;
import com.shoppingcart.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RestController
@RequestMapping("/api")
public class CartController {

    private static Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    CartRepository cartRepository;

    @Autowired
    ItemRepository itemRepository;

    @GetMapping
    public Flux<Cart> getAllCart() {
        return cartRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Cart> getCart(@PathVariable String id) {
        logger.info("received id:" + id);

        return cartRepository.findById(id);
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

    @PutMapping("/itemIncr")
    public Mono<Cart> incrQuantity(
            @RequestParam String cartId,
            @RequestParam String itemId,
            @RequestParam int quantity
    ) {

        logger.info("cartId: {}, \n itemId:{}, \n quantity:{}", cartId, itemId, quantity);

        Mono<Cart> updatedCart = updateCartItemQuantity(cartId, itemId, quantity);
//        inventoryConfirmation(updatedCart);

        return updatedCart;
    }

    //confirm inventory on a separate thread
    private void itemQuantityIncrementConfirmation(Mono<Cart> updatedCart) {

    }


    private Mono<Cart> updateCartItemQuantity(String cartId, String itemId, int incrQuantity) {

        return cartRepository.findById(cartId)
                .flatMap(cart -> {
                    List<Item> itemList = cart.getItemList();

                    for (int i = 0; i < itemList.size(); i++) {
                        if (itemList.get(i).getId().equals(itemId)) {
                            itemList.get(i).setQuantity(itemList.get(i).getQuantity() + incrQuantity);
                        }
                    }

                    return Mono.just(cart);
                })
                .flatMap(modCart -> cartRepository.save(modCart));
    }
}
