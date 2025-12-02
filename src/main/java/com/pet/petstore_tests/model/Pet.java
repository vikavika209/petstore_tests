package com.pet.petstore_tests.model;

import lombok.Data;

import java.util.List;

@Data
public class Pet {
    private Integer id;
    private Category category;
    private String name;
    private List<String> photoUrls;
    private List<Tag> tags;
    private PetStatus status;
}
