package com.shoppingcart;


import com.shoppingcart.model.Cart;
import com.shoppingcart.repository.CartRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class ShoppingcartApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoppingcartApplication.class, args);
    }

    @Bean
    CommandLineRunner init(ReactiveMongoOperations operations, CartRepository cartRepository) {

        return args -> {
            Flux<Cart> cartFlux = Flux.just(
                    new Cart("12", "Cart1"),
                    new Cart("13", "Cart2"),
                    new Cart("14", "Cart3"))
                    .flatMap(cart -> cartRepository.save(cart));

            operations.collectionExists(Cart.class)
                    .flatMap(exists -> exists ? operations.dropCollection(Cart.class) : Mono.just(exists))
                    .thenMany(operations.createCollection(Cart.class))
                    .thenMany(cartFlux)
                    .thenMany(cartRepository.findAll())
                    .subscribe(s -> {
                        System.out.println(s);
                    });
        };
    }
}