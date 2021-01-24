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
    private int id;

    @Column("cartid")
    private int cartId;

    @Column("itemid")
    private int itemId;

    @Column("quantity")
    private int quantity;

    @Column("itemMetaData")
    private String itemMetaData;
}
