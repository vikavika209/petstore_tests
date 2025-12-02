package com.pet.petstore_tests.model;

import lombok.Data;

@Data
public class Order {
    private Integer id;
    private Integer petId;
    private Integer quantity;
    private String shipDate;
    private OrderStatus orderStatus;
    private Boolean complete;
}
