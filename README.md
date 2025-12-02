Petstore API Autotests
======================

Проект автотестов для публичного сервиса Swagger Petstore:
https://petstore.swagger.io

Технологии и стек
-----------------
* Java 17+
* JUnit 5
* Rest-Assured
* Lombok
* Maven

Структура проекта
-----------------
```text
src
└─ test
   └─ java
      └─ com.pet.petstore_tests
         ├─ BaseApiTest.java
         ├─ PetApiTest.java
         ├─ UserApiTest.java
         └─ StoreApiTest.java
```

Описание
--------
Проект содержит набор функциональных UI‑независимых тестов,
проверяющих основные методы API сущностей:

* Pet
* User
* Store

Проверяются как позитивные сценарии, так и фактическое поведение API
при некорректных данных (если отличается от спецификации Swagger, тесты фиксируют реальность).

Запуск тестов
-------------
1. Установить Java 17 и Maven
2. Перейти в корень проекта
3. Выполнить команду:

```bash
   mvn clean test
```

Файлы тестов:
-------------
* PetApiTest   — CRUD‑операции с питомцами
* UserApiTest  — создание, логин, обновление и удаление пользователей
* StoreApiTest — операции заказа и инвентаризация