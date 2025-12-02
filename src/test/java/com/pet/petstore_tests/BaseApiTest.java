package com.pet.petstore_tests;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

public class BaseApiTest {
    protected static RequestSpecification requestSpec;

    @BeforeAll
    static void setup() {
        String baseUri = System.getProperty("petstore.baseUri",
                "https://petstore.swagger.io/v2");

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(baseUri)
                .setContentType("application/json")
                .log(LogDetail.ALL)
                .build();

        RestAssured.requestSpecification = requestSpec;
    }
}
