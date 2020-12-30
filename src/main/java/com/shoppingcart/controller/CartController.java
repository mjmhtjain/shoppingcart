package com.shoppingcart.controller;

import com.shoppingcart.model.Cart;
import com.shoppingcart.model.Item;
import com.shoppingcart.repository.CartRepository;
import com.shoppingcart.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public Mono<ResponseEntity<String>> incrQuantity(
            @RequestParam String cartId,
            @RequestParam String itemId,
            @RequestParam int incrQuantity
    ) {
        logger.info("cartId: {}, itemId: {}, quantity: {}", cartId, itemId, incrQuantity);

        Mono<Boolean> validInventoryCheck = checkInventory(cartId, itemId, incrQuantity);

        Mono<ResponseEntity<String>> responseEntityMono = validInventoryCheck
                .flatMap(resp -> {
                    if (resp) {
                        return updateCartItemQuantity(cartId, itemId, incrQuantity)
                                .flatMap(cart -> {
                                    if (cart != null) {
                                        return Mono.just(ResponseEntity.ok("inventory added"));
                                    }

                                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                            .body("inventory check failed"));
                                });
                    }

                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("inventory check failed"));
                });


        return responseEntityMono;
    }

    private Mono<Cart> updateCartItemQuantity(

    ) {
        return Mono.just(
                new Cart(null, "Cart_Temp", new ArrayList<>())
        );
    }

    private Mono<Boolean> checkInventory(
            String cartId,
            String itemId,
            int incrQuantity
    ) {
        Mono<Boolean> defaultResponse = null;

        Mono<Integer> cartItemQuantity = cartRepository.findById(cartId)
                .map(cart -> cart.getItemList().stream()
                        .filter(item -> item.getId() == itemId)
                        .map(item -> item.getQuantity())
                        .findFirst()
                        .orElse(-1)
                );

        Mono<Integer> inventoryItemQuantity = itemRepository.findById(itemId)
                .flatMap(item -> Mono.just(item.getQuantity()));

        Mono<Integer> diff =
                inventoryItemQuantity.mergeWith(
                        cartItemQuantity.mergeWith(Mono.just(incrQuantity))
                                .reduce(0, Integer::sum)
                                .map(i -> i * -1)
                )
                        .reduce(0, Integer::sum);


        defaultResponse = diff.flatMap(i -> i >= 0 ? Mono.just(true) : Mono.just(false));

        return defaultResponse;
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
