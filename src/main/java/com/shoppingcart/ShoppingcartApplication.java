package com.shoppingcart;


import com.shoppingcart.model.Cart;
import com.shoppingcart.model.Item;
import com.shoppingcart.repository.CartRepository;
import com.shoppingcart.repository.ItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@SpringBootApplication
public class ShoppingcartApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoppingcartApplication.class, args);
    }

    @Bean
    CommandLineRunner init(ReactiveMongoOperations operations, CartRepository cartRepository,
                           ItemRepository itemRepository) {

        return args -> {
            Flux<Cart> saveCarts = Flux.just(
                    new Cart(null, "Cart1", new ArrayList<>()),
                    new Cart(null, "Cart2", new ArrayList<>()),
                    new Cart(null, "Cart3", new ArrayList<>()))
                    .flatMap(cart -> cartRepository.save(cart));

            Flux<Item> saveItems = Flux.just(
                    new Item(null, "Item1", 12),
                    new Item(null, "Item2", 13),
                    new Item(null, "Item3", 14))
                    .flatMap(item -> itemRepository.save(item));

            operations.collectionExists(Cart.class)
                    .flatMap(exists -> exists ? operations.dropCollection(Cart.class) : Mono.just(exists))
                    .thenMany(operations.createCollection(Cart.class))
                    .thenMany(saveCarts)
                    .thenMany(cartRepository.findAll())
                    .subscribe(s -> {
                        System.out.println(s);
                    });

            operations.collectionExists(Item.class)
                    .flatMap(exists -> exists ? operations.dropCollection(Item.class) : Mono.just(exists))
                    .thenMany(operations.createCollection(Item.class))
                    .thenMany(saveItems)
                    .thenMany(itemRepository.findAll())
                    .subscribe(s -> {
                        System.out.println(s);
                    });
        };
    }
}