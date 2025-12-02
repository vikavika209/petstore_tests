package com.pet.petstore_tests;

import com.pet.petstore_tests.model.Category;
import com.pet.petstore_tests.model.Pet;
import com.pet.petstore_tests.model.PetStatus;
import com.pet.petstore_tests.model.Tag;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PetApiTest extends BaseApiTest {
    private static Integer petId;

    private Pet buildRandomPet() {
        Pet pet = new Pet();
        pet.setId(ThreadLocalRandom.current().nextInt(1_000_000, 9_999_999));

        Category category = new Category();
        category.setId(1);
        category.setName("dogs");
        pet.setCategory(category);

        pet.setName("doggie-" + pet.getId());
        pet.setPhotoUrls(List.of("https://example.com/photo1.jpg"));

        Tag tag = new Tag();
        tag.setId(1);
        tag.setName("cute");
        pet.setTags(List.of(tag));

        pet.setStatus(PetStatus.AVAILABLE);
        return pet;
    }

    @Test
    @Order(1)
    @DisplayName("Создание питомца (POST /pet)")
    void createPet_success() {
        Pet newPet = buildRandomPet();

        petId = Math.toIntExact(given()
                .body(newPet)
                .when()
                .post("/pet")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(newPet.getId()))
                .body("name", equalTo(newPet.getName()))
                .body("status", equalTo(PetStatus.AVAILABLE.name()))
                .extract()
                .jsonPath().getInt("id"));
    }

    @Test
    @DisplayName("Создание питомца (POST /pet) - Invalid Input")
    void createPet_invalid_input() {

        //Согласно документации, для некорректного ввода возможен 405,
        //однако сервер Petstore не валидирует тело и возвращает 200 даже для JSON с null-полями.
        //В данном тесте зафиксировано фактическое поведение сервиса.

        Pet newPet = new Pet();

        given()
                .body(newPet)
                .when()
                .post("/pet")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(2)
    @DisplayName("Получение питомца по id (GET /pet/{petId})")
    void getPetById_success() {
        Assertions.assertNotNull(petId);

        given()
                .pathParam("petId", petId)
                .when()
                .get("/pet/{petId}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(petId))
                .body("name", startsWith("doggie-"));
    }

    @Test
    @DisplayName("Получение питомца по id (GET /pet/{petId}) - Not Found")
    void getPetById_not_found() {

        given()
                .pathParam("petId", 999999999)
                .when()
                .get("/pet/{petId}")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Получение питомца по id (GET /pet/{petId}) - Invalid ID")
    void getPetById_invalid_id() {

        //Согласно спецификации, для некорректного ID возможен 400 “Invalid ID supplied”,
        //но сервис Petstore фактически возвращает 404 даже для строкового id.
        //В тесте зафиксировано реальное поведение.

        given()
                .pathParam("petId", "abc")
                .when()
                .get("/pet/{petId}")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(3)
    @DisplayName("Поиск по статусу (GET /pet/findByStatus?status=available&status=sold)")
    void findByStatus_multipleStatuses() {
        given()
                .queryParam("status", "available", "sold")
                .when()
                .get("/pet/findByStatus")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", not(empty()))
                .body("status", everyItem(oneOf("available", "sold")));
    }

    @Test
    @DisplayName("Поиск по статусу с невалидным значением возвращает пустой список")
    void findByStatus_invalid_status_returnsEmptyList() {

        //Согласно здравому смыслу, невалидный статус мог бы приводить к 400,
        //однако сервис Petstore воспринимает любое строковое значение как валидный фильтр
        //и возвращает 200 с пустым списком.
        //В тесте зафиксировано реальное поведение.

        given()
                .queryParam("status", "777")
                .when()
                .get("/pet/findByStatus")
                .then()
                .statusCode(200)
                .body("$", empty());
    }

    @Test
    @Order(4)
    @DisplayName("Обновление через form (POST /pet/{petId}?name=&status=)")
    void updatePetViaFormData() {
        Assertions.assertNotNull(petId);

        String newName = "doggieUpdated-" + petId;
        String newStatus = "sold";

        given()
                .contentType("application/x-www-form-urlencoded")
                .pathParam("petId", petId)
                .formParam("name", newName)
                .formParam("status", newStatus)
                .when()
                .post("/pet/{petId}")
                .then()
                .statusCode(anyOf(is(200), is(204)));

        given()
                .pathParam("petId", petId)
                .when()
                .get("/pet/{petId}")
                .then()
                .statusCode(200)
                .body("name", equalTo(newName))
                .body("status", equalTo(newStatus));
    }

    @Test
    @Order(5)
    @DisplayName("Обновление через form (POST /pet/{petId}) — сервис принимает любой статус")
    void updatePetViaFormData_invalid_status_is_accepted() {

        //Согласно здравому смыслу, некорректное значение поля status могло бы приводить к ошибке валидации (400).
        //Однако сервис Petstore принимает любое строковое значение и возвращает 200 OK.
        //В тесте зафиксировано реальное поведение сервиса.

        Assertions.assertNotNull(petId);

        String newName = "doggieUpdated-" + petId;

        given()
                .contentType("application/x-www-form-urlencoded")
                .pathParam("petId", petId)
                .formParam("name", newName)
                .formParam("status", "123")
                .when()
                .post("/pet/{petId}")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Загрузка изображения после создания питомца")
    void createPet_andUploadImage_success() {
        Pet pet = buildRandomPet();

        Integer newPetId = Math.toIntExact(given()
                .body(pet)
                .when()
                .post("/pet")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getInt("id"));

        String additionalMetadata = "photo after creation";
        File file = new File("src/test/resources/test-image.jpg");

        given()
                .contentType(ContentType.MULTIPART)
                .pathParam("petId", newPetId)
                .multiPart("additionalMetadata", additionalMetadata)
                .multiPart("file", file)
                .when()
                .post("/pet/{petId}/uploadImage")
                .then()
                .statusCode(200)
                .body("message", containsString(additionalMetadata))
                .body("message", containsString(file.getName()));
    }

    @Test
    @Order(6)
    @DisplayName("Удаление питомца (DELETE /pet/{petId}) - успешное удаление")
    void deletePet_success() {
        Assertions.assertNotNull(petId);

        given()
                .pathParam("petId", petId)
                .when()
                .delete("/pet/{petId}")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Удаление по несуществующему id")
    void deletePet_notFound() {
        Integer badId = 888888888;

        given()
                .pathParam("petId", badId)
                .when()
                .delete("/pet/{petId}")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Удаление с невалидным id")
    void deletePet_invalidId() {

        //Несмотря на то, что спецификация указывает код 400 (“Invalid ID supplied”),
        //фактическая имплементация возвращает 404 — сервис воспринимает любой строковый path param
        //как потенциальный id питомца и отвечает “Pet not found”, если он отсутствует.

        given()
                .pathParam("petId", "ooo")
                .when()
                .delete("/pet/{petId}")
                .then()
                .statusCode(404);
    }
}
