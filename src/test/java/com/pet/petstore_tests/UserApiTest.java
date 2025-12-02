package com.pet.petstore_tests;

import com.pet.petstore_tests.model.User;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserApiTest extends BaseApiTest {

    private static User userOne = new User();
    private static User userTwo = new User();
    private  static User userThree = new User();
    private static User userFour = new User();
    private static User userFive = new User();

    private static User.UserBuilder baseUser() {
        return User.builder()
                .firstName("User")
                .userStatus(1);
    }

    @BeforeAll
    static void beforeAll() {
        userOne = baseUser()
                .id(1001)
                .username("UserOne")
                .lastName("One")
                .email("user1@example.com")
                .password("pass1")
                .phone("+111111111")
                .build();

        userTwo = baseUser()
                .id(1002)
                .username("UserTwo")
                .lastName("Two")
                .email("user2@example.com")
                .password("pass2")
                .phone("+222222222")
                .build();

        userThree = baseUser()
                .id(1003)
                .username("UserThree")
                .lastName("Three")
                .email("user3@example.com")
                .password("pass3")
                .phone("+333333333")
                .build();

        userFour = baseUser()
                .id(1004)
                .username("UserFour")
                .lastName("Four")
                .email("user4@example.com")
                .password("pass4")
                .phone("+444444444")
                .build();

        userFive = baseUser()
                .id(1005)
                .username("UserFive")
                .lastName("Five")
                .email("user5@example.com")
                .password("pass5")
                .phone("+555555555")
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("Создание пользователей массивом (POST /user/createWithArray)")
    void createUsersWithArray() {
        var user1 = Map.of(
                "id", userOne.getId(),
                "username", userOne.getUsername(),
                "firstName", userOne.getFirstName(),
                "lastName", userOne.getLastName(),
                "email", userOne.getEmail(),
                "password", userOne.getPassword(),
                "phone", userOne.getPhone(),
                "userStatus", userOne.getUserStatus()
        );

        var user2 = Map.of(
                "id", userTwo.getId(),
                "username", userTwo.getUsername(),
                "firstName", userTwo.getFirstName(),
                "lastName", userTwo.getLastName(),
                "email", userTwo.getEmail(),
                "password", userTwo.getPassword(),
                "phone", userTwo.getPhone(),
                "userStatus", userTwo.getUserStatus()
        );

        given()
                .body(List.of(user1, user2))
                .when()
                .post("/user/createWithArray")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .contentType(anyOf(is(ContentType.JSON.toString()), containsString("application/json")));
    }

    @Test
    @Order(2)
    @DisplayName("Получение пользователя по username (GET /user/{username})")
    void getUserByUsername() {

        given()
                .pathParam("username", userOne.getUsername())
                .when()
                .get("/user/{username}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("username", equalTo(userOne.getUsername()))
                .body("email", equalTo("user1@example.com"));

    }

    @Test
    @DisplayName("Получение пользователя по несуществующему username - User not found")
    void getUserByUsername_notFound() {
        String notFound = "not_found";

        given()
                .pathParam("username", notFound)
                .when()
                .get("/user/{username}")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Получение пользователя по невалидному username - Invalid username supplied")
    void getUserByUsername_invalidUsername() {

        //Согласно спецификации Petstore, некорректный username должен приводить к 400 Bad Request.
        //Однако сервис Petstore не валидирует формат path-параметра,
        //воспринимая любое значение как валидный username
        //и возвращает 404 Not Found, если такого пользователя не существует.
        //В тесте зафиксировано реальное поведение сервиса.

        String notValidUsername = "!@#$%^&";

        given()
                .pathParam("username", notValidUsername)
                .when()
                .get("/user/{username}")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(3)
    @DisplayName("Обновление пользователя (PUT /user/{username})")
    void updateUser_success() {

        String updatedFirstName = "new_first_name_1";
        String updatedLastName = "new_last_name_1";
        String updatedEmail = "new_user1@example.com";

        String updateUserBody = """
                {
                  "id": %d,
                  "username": "%s",
                  "firstName": "%s",
                  "lastName": "%s",
                  "email": "%s",
                  "password": "%s",
                  "phone": "%s",
                  "userStatus": 1
                }
                """.formatted(
                userOne.getId(),
                userOne.getUsername(),
                updatedFirstName,
                updatedLastName,
                updatedEmail,
                userOne.getPassword(),
                userOne.getPhone()
        );

        given()
                .pathParam("username", userOne.getUsername())
                .body(updateUserBody)
                .when()
                .put("/user/{username}")
                .then()
                .statusCode(200);

        given()
                .pathParam("username", userOne.getUsername())
                .when()
                .get("/user/{username}")
                .then()
                .statusCode(200)
                .body("id", equalTo(userOne.getId()))
                .body("username", equalTo(userOne.getUsername()))
                .body("firstName", equalTo(updatedFirstName))
                .body("lastName", equalTo(updatedLastName))
                .body("email", equalTo(updatedEmail));
    }

    @Test
    @DisplayName("Обновление пользователя c несуществующим username - User not found")
    void updateUser_notFound() {

        //Несмотря на название статуса в Swagger (“User not found” для PUT /user/{username}),
        //сервис Petstore не возвращает 404.
        //Запрос PUT /user/{username} фактически работает как upsert:
        //создаёт пользователя, если он отсутствует.
        //В тесте зафиксировано это фактическое поведение.

        String notFoundUsername = "not_found";
        String updatedFirstName = "new_first_name_1";
        String updatedLastName = "new_last_name_1";
        String updatedEmail = "new_user1@example.com";

        String updateUserBody = """
                {
                  "id": %d,
                  "username": "%s",
                  "firstName": "%s",
                  "lastName": "%s",
                  "email": "%s",
                  "password": "%s",
                  "phone": "%s",
                  "userStatus": 1
                }
                """.formatted(
                userOne.getId(),
                notFoundUsername,
                updatedFirstName,
                updatedLastName,
                updatedEmail,
                userOne.getPassword(),
                userOne.getPhone()
        );

        given()
                .pathParam("username", notFoundUsername)
                .body(updateUserBody)
                .when()
                .put("/user/{username}")
                .then()
                .statusCode(200);

        given()
                .pathParam("username", notFoundUsername)
                .when()
                .get("/user/{username}")
                .then()
                .statusCode(200)
                .body("username", equalTo(notFoundUsername))
                .body("firstName", equalTo(updatedFirstName))
                .body("lastName", equalTo(updatedLastName))
                .body("email", equalTo(updatedEmail));
    }

    @Test
    @DisplayName("Обновление пользователя c невалидным username - Invalid user supplied")
    void updateUser_invalidUsername() {

        //Несмотря на название статуса в Swagger (“Invalid user supplied” для PUT /user/{username}),
        //сервис Petstore не возвращает 400.
        //Запрос PUT /user/{username} фактически работает как upsert:
        //создаёт пользователя с невалидным username.
        //В тесте зафиксировано это фактическое поведение.

        String notValidUsername = "!@#$%^&";
        String updatedFirstName = "new_first_name_1";
        String updatedLastName = "new_last_name_1";
        String updatedEmail = "new_user1@example.com";

        String updateUserBody = """
                {
                  "id": %d,
                  "username": "%s",
                  "firstName": "%s",
                  "lastName": "%s",
                  "email": "%s",
                  "password": "%s",
                  "phone": "%s",
                  "userStatus": 1
                }
                """.formatted(
                userOne.getId(),
                notValidUsername,
                updatedFirstName,
                updatedLastName,
                updatedEmail,
                userOne.getPassword(),
                userOne.getPhone()
        );

        given()
                .pathParam("username", notValidUsername)
                .body(updateUserBody)
                .when()
                .put("/user/{username}")
                .then()
                .statusCode(200);

        given()
                .pathParam("username", notValidUsername)
                .when()
                .get("/user/{username}")
                .then()
                .statusCode(200)
                .body("firstName", equalTo(updatedFirstName))
                .body("lastName", equalTo(updatedLastName))
                .body("email", equalTo(updatedEmail));
    }

    @Test
    @Order(4)
    @DisplayName("Удаление существующего пользователя - 200 OK")
    void deleteUser_success() {

        given()
                .pathParam("username", userOne.getUsername())
                .when()
                .delete("/user/{username}")
                .then()
                .statusCode(200);

        given()
                .pathParam("username", userOne.getUsername())
                .when()
                .get("/user/{username}")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Удаление несуществующего пользователя - User not found")
    void deleteUser_notFound() {

        String unknownUser = "unknownUser";

        given()
                .pathParam("username", unknownUser)
                .when()
                .delete("/user/{username}")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Удаление с некорректным username - Invalid user supplied")
    void deleteUser_invalidUsername() {

        //Согласно спецификации Petstore, некорректный username должен приводить к 400 Bad Request.
        //Однако сервис Petstore не валидирует формат path-параметра,
        //воспринимая любое значение как валидный username
        //и возвращает 404 Not Found, если такого пользователя не существует.
        //В тесте зафиксировано реальное поведение сервиса.

        String invalidUsername = "!@#$%^&";

        given()
                .pathParam("username", invalidUsername)
                .when()
                .delete("/user/{username}")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(5)
    @DisplayName("Логин пользователя с query-параметрами (GET /user/login?username=&password=)")
    void loginUser() {
        given()
                .queryParam("username", userTwo.getUsername())
                .queryParam("password", userTwo.getPassword())
                .when()
                .get("/user/login")
                .then()
                .statusCode(200)
                .body("message", containsString("logged in user session"));
    }

    @Test
    @DisplayName("Логин пользователя с query-параметрами - Invalid username/password supplied")
    void loginUser_invalid_password() {

       //Согласно спецификации Swagger, при некорректных учетных данных
        //сервис должен возвращать статус 400 ("Invalid username/password supplied").
        //Однако фактически Petstore не валидирует логин и пароль и всегда
        //отправляет ответ со статусом 200, независимо от корректности данных.
        //В тесте зафиксировано реальное поведение.

        String invalidPassword = "!@#$%^&";

        given()
                .queryParam("username", userTwo.getUsername())
                .queryParam("password", invalidPassword)
                .when()
                .get("/user/login")
                .then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("message", containsString("logged in user session"));
    }

    @Test
    @DisplayName("Логаут пользователя (GET /user/logout)")
    void logoutUser_success() {

        given()
                .when()
                .get("/user/logout")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("code", equalTo(200))
                .body("type", notNullValue())
                .body("message", notNullValue());
    }

    @Test
    @DisplayName("Создание пользователей списком (POST /user/createWithArray)")
    void createUsersWithArray_success() {

        String usersArrayBody = """
            [
              {
                "id": %d,
                "username": "%s",
                "firstName": "%s",
                "lastName": "%s",
                "email": "%s",
                "password": "%s",
                "phone": "%s",
                "userStatus": 1
              },
              {
                "id": %d,
                "username": "%s",
                "firstName": "%s",
                "lastName": "%s",
                "email": "%s",
                "password": "%s",
                "phone": "%s",
                "userStatus": 1
              }
            ]
            """.formatted(
                userThree.getId(), userThree.getUsername(), userThree.getFirstName(), userThree.getLastName(),
                userThree.getEmail(), userThree.getPassword(), userThree.getPhone(),
                userFour.getId(), userFour.getUsername(), userFour.getFirstName(), userFour.getLastName(),
                userFour.getEmail(), userFour.getPassword(), userFour.getPhone()
        );

        given()
                .contentType(ContentType.JSON)
                .body(usersArrayBody)
                .when()
                .post("/user/createWithArray")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("code", equalTo(200))
                .body("type", notNullValue())
                .body("message", notNullValue());
    }

    @Test
    @DisplayName("Создание пользователя (POST /user) - successful operation")
    void createUser_success() {

        String userBody = """
            {
              "id": %d,
              "username": "%s",
              "firstName": "%s",
              "lastName": "%s",
              "email": "%s",
              "password": "%s",
              "phone": "%s",
              "userStatus": 1
            }
            """.formatted(
                userFive.getId(),
                userFive.getUsername(),
                userFive.getFirstName(),
                userFive.getLastName(),
                userFive.getEmail(),
                userFive.getPassword(),
                userFive.getPhone()
        );

        given()
                .contentType(ContentType.JSON)
                .body(userBody)
                .when()
                .post("/user")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("code", equalTo(200))
                .body("type", notNullValue())
                .body("message", equalTo(String.valueOf(userFive.getId())));
    }
}
