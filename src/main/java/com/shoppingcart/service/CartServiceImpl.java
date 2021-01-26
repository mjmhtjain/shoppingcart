package com.shoppingcart.service;

import com.google.gson.Gson;
import com.shoppingcart.model.Cart;
import com.shoppingcart.model.CartItemValidationEvent;
import com.shoppingcart.model.CartItemValidationResponseEvent;
import com.shoppingcart.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@Service
public class CartServiceImpl implements CartService {
    Logger log = LoggerFactory.getLogger(CartServiceImpl.class);
    private final String validateCartResponseTopic = "validate_cart_response_topic";
    private final String validateCartTopic = "validate_cart_topic";
    private final String validateCartTopic_Key = "validate_cart_topic_key";

    private CartRepository cartRepository;
    private KafkaTemplate<String, String> kafkaTemplate;
    private Gson jsonConverter;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, KafkaTemplate<String, String> kafkaTemplate, Gson jsonConverter) {
        this.cartRepository = cartRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.jsonConverter = jsonConverter;
    }

    @Override
    public Flux<Cart> findAll() {
        return cartRepository.findAll();
    }

    @Override
    public Flux<Cart> fetchByCartId(int cartid) {
        return cartRepository.fetchByCartId(cartid);
    }

    @Override
    public Mono<Cart> fetchByCartIdItemId(int cartid, int itemid) {
        return cartRepository.fetchByCartIdItemId(cartid, itemid);
    }

    @Override
    public Mono<Boolean> updateCart(int cartid, int itemid, int quantity) {
        CartItemValidationEvent cartItemValidationEvent =
                new CartItemValidationEvent(cartid, itemid, quantity);

        CompletableFuture<SendResult<String, String>> kafkaResponse =
                kafkaTemplate
                        .send(validateCartTopic,
                                String.valueOf(cartid), //setting cartid as the key
                                jsonConverter.toJson(cartItemValidationEvent, CartItemValidationEvent.class))
                        .completable();

        return Mono.fromFuture(kafkaResponse)
                .doOnNext(result -> {
                    String val = result.getProducerRecord().value();
                    String key = result.getProducerRecord().key();
                    String topic = result.getProducerRecord().topic();
                    Integer partition = result.getProducerRecord().partition();

                    log.info("kafka Event Sent: {}, {}, {}, {}", key, val, topic, partition);
                })
                .thenReturn(true);
    }

    @Override
    public void updateCartItemQuantity(CartItemValidationResponseEvent cartItemValidationResponseEvent) {
        Cart defaultCart = new Cart(-1, -1, -1, "DefaultCartObject");
        if (!cartItemValidationResponseEvent.isValid()) return;

        cartRepository.fetchByCartIdItemId(cartItemValidationResponseEvent.getCartid(),
                cartItemValidationResponseEvent.getItemid())
                .defaultIfEmpty(defaultCart)
                .flatMap(cart -> {
                    Mono<Cart> persistedCart;

                    if (cart.getCartId() == -1) {
                        log.info("cart entry does not exist");
                        persistedCart = insertIntoCart(cartItemValidationResponseEvent);
                    } else {
                        log.info("updating cart");
                        cart.setQuantity(cartItemValidationResponseEvent.getQuantity());
                        persistedCart = cartRepository.save(cart);
                    }

                    return persistedCart;
                })
                .doOnNext(cart -> log.info("updated cart: {}", cart))
                .subscribe();
    }

    private Mono<Cart> insertIntoCart(CartItemValidationResponseEvent cartItemValidationResponseEvent) {
        Cart cart = new Cart(cartItemValidationResponseEvent.getCartid(),

                cartItemValidationResponseEvent.getItemid(),
                cartItemValidationResponseEvent.getQuantity(),
                "item" + cartItemValidationResponseEvent.getItemid());

        return cartRepository.save(cart);
    }

}
