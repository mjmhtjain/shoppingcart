package com.shoppingcart.repository;

import com.shoppingcart.model.Cart;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface CartRepository extends
        ReactiveCrudRepository<Cart, Integer> {

    @Query("SELECT * FROM cart WHERE cartid = :cartid")
    Flux<Cart> fetchByCartId(int cartid);
}
