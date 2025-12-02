package com.pet.petstore_tests;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.time.OffsetDateTime;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StoreApiTest extends BaseApiTest {

    static Integer orderId;
    static Integer petId;

    @BeforeAll
    static void setUp() {
        orderId = ThreadLocalRandom.current().nextInt(1_000_000, 9_999_999);
        petId = ThreadLocalRandom.current().nextInt(1, 9_999_999);
    }

    @Test
    @Order(1)
    @DisplayName("Создание заказа (POST /store/order)")
    void placeOrder_Success() {

        String orderBody = """
                {
                  "id": %d,
                  "petId": %d,
                  "quantity": 2,
                  "shipDate": "%s",
                  "status": "placed",
                  "complete": true
                }
                """.formatted(orderId, petId, OffsetDateTime.now().toString());

        given()
                .body(orderBody)
                .when()
                .post("/store/order")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(orderId))
                .body("petId", equalTo(petId))
                .body("status", equalTo("placed"))
                .body("complete", equalTo(true));

    }

    @Test
    @DisplayName("Создание заказа (POST /store/order) - Invalid Order")
    void placeOrder_invalid_input() {

        //При передаче quantity в виде строки
        // сервис Petstore возвращает 500 Internal Server Error,
        //что указывает на отсутствие корректной валидации входных данных и падение сервера.
        //В тесте зафиксировано фактическое поведение сервиса.

        String orderBody = """
                {
                  "id": %d,
                  "petId": %d,
                  "quantity": "some string",
                  "shipDate": "%s",
                  "status": "placed",
                  "complete": true
                }
                """.formatted(orderId, petId, OffsetDateTime.now().toString());

        given()
                .body(orderBody)
                .when()
                .post("/store/order")
                .then()
                .statusCode(500);

    }

    @Test
    @Order(2)
    @DisplayName("Получение заказа (POST /store/order)")
    void getOrderById_Success() {

        given()
                .pathParam("orderId", orderId)
                .when()
                .get("/store/order/{orderId}")
                .then()
                .statusCode(200)
                .body("id", equalTo(orderId));
    }


    @Test
    @DisplayName("Получение несуществующего заказа возвращает 404")
    void getNonExistingOrder_returns404() {
        int nonExistingOrderId = 99999999;

        given()
                .pathParam("orderId", nonExistingOrderId)
                .when()
                .get("/store/order/{orderId}")
                .then()
                .statusCode(anyOf(is(404), is(400)));
    }

    @Test
    @DisplayName("Получение заказа по невалидному ID - Invalid ID supplied")
    void getNonExistingOrder_invalid_id() {

        //Согласно спецификации Petstore, некорректный ID должен приводить к 400 Bad Request.
        //Однако сервис Petstore не валидирует формат path-параметра,
        //воспринимая любое строковое значение как валидный ID заказа.
        //В результате сервер пытается найти заказ по строковому ID
        //и возвращает 404 Not Found, если такого заказа не существует.
        //В тесте зафиксировано реальное поведение сервиса.

        String nonValidOrderId = "some string";

        given()
                .pathParam("orderId", nonValidOrderId)
                .when()
                .get("/store/order/{orderId}")
                .then()
                .statusCode(is(404));
    }

    @Test
    @DisplayName("Проверка инвентаря (GET /store/inventory)")
    void checkInventory() {
        given()
                .when()
                .get("/store/inventory")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", aMapWithSize(greaterThan(0)))
                .body("available", notNullValue());
    }

    @Test
    @Order(3)
    @DisplayName("Удаление заказа (DELETE /store/order/{orderId})")
    void deleteOrder() {

        given()
                .pathParam("orderId", orderId)
                .when()
                .delete("/store/order/{orderId}")
                .then()
                .statusCode(anyOf(is(200), is(204)));

        given()
                .pathParam("orderId", orderId)
                .when()
                .get("/store/order/{orderId}")
                .then()
                .statusCode(anyOf(is(404), is(400)));
    }

    @Test
    @DisplayName("Удаление заказа по отсутствующему ID - Order not found")
    void deleteOrder_not_found() {

        int nonExistingOrderId = 88888888;

        given()
                .pathParam("orderId", nonExistingOrderId)
                .when()
                .delete("/store/order/{orderId}")
                .then()
                .statusCode(is(404));

    }

    @Test
    @DisplayName("Удаление заказа по невалидному ID - Invalid ID supplied")
    void deleteOrder_invalid_id() {

        //Согласно спецификации Petstore, некорректный ID должен приводить к 400 Bad Request.
        //Однако сервис Petstore не валидирует формат path-параметра,
        //воспринимая любое строковое значение как валидный ID заказа.
        //В результате сервер пытается найти заказ по строковому ID
        //и возвращает 404 Not Found, если такого заказа не существует.
        //В тесте зафиксировано реальное поведение сервиса.

        String nonExistingOrderId = "another string";

        given()
                .pathParam("orderId", nonExistingOrderId)
                .when()
                .delete("/store/order/{orderId}")
                .then()
                .statusCode(is(404));

    }
}
