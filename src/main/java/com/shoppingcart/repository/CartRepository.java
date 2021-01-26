package com.shoppingcart.repository;

import com.shoppingcart.model.Cart;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartRepository extends
        ReactiveCrudRepository<Cart, Integer> {

    @Query("TRUNCATE TABLE cart RESTART IDENTITY;")
    Mono<Void> truncate();

    @Query("SELECT * FROM cart WHERE cartid = :cartid")
    Flux<Cart> fetchByCartId(int cartid);

    @Query("SELECT * FROM cart WHERE cartid = :cartid and itemid = :itemid")
    Mono<Cart> fetchByCartIdItemId(int cartid, int itemid);
}
