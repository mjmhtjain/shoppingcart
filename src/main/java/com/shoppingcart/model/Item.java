package com.shoppingcart.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Document
public class Item {

    @Id
    private String Id;

    private String name;

    public Item() {
    }

    public Item(String id, String name) {
        Id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Id.equals(item.Id) && name.equals(item.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Id, name);
    }

    //getters setters
    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
