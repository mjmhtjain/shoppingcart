package com.shoppingcart.controller;

import com.google.gson.Gson;
import com.shoppingcart.model.Cart;
import com.shoppingcart.model.CartItemValidationEvent;
import com.shoppingcart.model.CartItemValidationResponseEvent;
import com.shoppingcart.repository.CartRepository;
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

    private CartRepository cartRepository;
    private KafkaTemplate<String, String> kafkaTemplate;
    private Gson jsonConverter;

    @Autowired
    public CartController(CartRepository cartRepository, KafkaTemplate<String, String> kafkaTemplate, Gson jsonConverter) {
        this.cartRepository = cartRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.jsonConverter = jsonConverter;
    }

    @GetMapping("/fetchAll")
    public Flux<Cart> fetchAll() {
        return cartRepository.findAll();
    }

    @GetMapping("/fetchByCartId/{cartid}")
    public Flux<Cart> fetchByCartId(@PathVariable int cartid) {
        log.info("fetchByCartId, cartid: {}", cartid);

        return cartRepository.fetchByCartId(cartid);
    }

    @GetMapping("/update/{cartid}/{itemid}/{quantity}")
    public void updateCart(@PathVariable int cartid,
                           @PathVariable int itemid,
                           @PathVariable int quantity) {
        log.info("updateCart, cartid: {}, itemid: {}, quantity: {}", cartid, itemid, quantity);

        String cartEvent = createEvent(cartid, itemid, quantity);
        sendEventToKafka(cartEvent, cartid);
    }

    @KafkaListener(topics = validateCartResponseTopic)
    public void getFromKafka(String stringifiedEvent) {
        CartItemValidationResponseEvent cartItemValidationResponseEvent = (CartItemValidationResponseEvent)
                jsonConverter.fromJson(stringifiedEvent, CartItemValidationResponseEvent.class);

        log.info("KafkaListener: {}", cartItemValidationResponseEvent.toString());
        updateCartItemQuantity(cartItemValidationResponseEvent);
    }

    private void updateCartItemQuantity(CartItemValidationResponseEvent cartItemValidationResponseEvent) {
        if (!cartItemValidationResponseEvent.isValid()) return;

        cartRepository.fetchByCartIdItemId(cartItemValidationResponseEvent.getCartid(),
                cartItemValidationResponseEvent.getItemid())
                .log()
                .switchIfEmpty(Mono.error(new Exception("Could not find the record")))
                .flatMap(cart -> {
                    cart.setQuantity(cartItemValidationResponseEvent.getQuantity());
                    log.info("setting quantity: {}", cart);
                    return cartRepository.save(cart);
                })
                .doOnError(err -> {
                    log.info(err.toString());
                })
                .subscribe();

    }

    private String createEvent(int cartid, int itemid, int quantity) {
        CartItemValidationEvent cartEvent = new CartItemValidationEvent(cartid, itemid, quantity);

        return jsonConverter.toJson(cartEvent, CartItemValidationEvent.class);
    }

    private void sendEventToKafka(String cartEvent, int cartid) {
        CompletableFuture<SendResult<String, String>> kafkaResponse =
                kafkaTemplate
                        .send(validateCartTopic,
                                String.valueOf(cartid), //setting cartid as the key
                                cartEvent)
                        .completable();

        Mono.fromFuture(kafkaResponse)
                .doOnNext(result -> {
                    String val = result.getProducerRecord().value();
                    String key = result.getProducerRecord().key();
                    String topic = result.getProducerRecord().topic();
                    Integer partition = result.getProducerRecord().partition();

                    log.info("kafka response: {}, {}, {}, {}", key, val, topic, partition);
                })
                .subscribe();
    }
}
