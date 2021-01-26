package com.shoppingcart.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("cart")
public class Cart {
    @Id
    @Column("id")
    private Long id;

    @Column("cartid")
    private int cartId;

    @Column("itemid")
    private int itemId;

    @Column("quantity")
    private int quantity;

    @Column("itemmetadata")
    private String itemMetaData;

    public Cart(int cartId, int itemId, int quantity, String itemMetaData) {
        this.cartId = cartId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.itemMetaData = itemMetaData;
    }
}
