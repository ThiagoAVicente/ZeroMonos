package org.hw1;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hw1.data.ServiceRequestRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hw1.data.User;
import org.hw1.data.UserRepository;
import org.hw1.service.UserService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ServiceRequestIT {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRequestIT.class);

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ServiceRequestRepository serviceRequestRepository;
    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        User user = new User();
        user.setName("Test User");
        user.setPassword(UserService.hashPassword("passord"));
        userRepository.save(user);

    }

    @AfterEach
    void resetDb() {
        userRepository.deleteAll();
        serviceRequestRepository.deleteAll();
    }

    @Test
    void testCreateServiceRequest_Success() {
        String requestBody = "{"
                + "\"user\": \"Test User\","
                + "\"municipality\": \"Lisboa\","
                + "\"requestedDate\": \"2026-11-04\","
                + "\"timeSlot\": \"10:00\","
                + "\"description\": \"Mesa velha\""
                + "}";

        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/requests")
        .then()
            .statusCode(200)
            .body("municipality.name", equalTo("Lisboa"))
            .body("description", equalTo("Mesa velha"))
            .body("token", notNullValue());
    }

    @Test
    void testCreateServiceRequest_InvalidMunicipio() {
        String requestBody = "{"
                + "\"user\": \"Test User\","
                + "\"municipality\": \"MunicípioInexistente\","
                + "\"requestedDate\": \"2026-11-01\","
                + "\"timeSlot\": \"10:00\","
                + "\"description\": \"Teste inválido\""
                + "}";

        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/requests")
        .then()
            .statusCode(400);
    }

    @Test
    void testCreateServiceRequest_InvalidUser() {
        String requestBody = "{"
                + "\"user\": \"UtilizadorInexistente\","
                + "\"municipality\": \"Lisboa\","
                + "\"requestedDate\": \"2026-11-01\","
                + "\"timeSlot\": \"10:00\","
                + "\"description\": \"Teste inválido\""
                + "}";

        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/requests")
        .then()
            .statusCode(400);
    }

    @Test
    void testCreateServiceRequest_InvalidDateFormat() {
        String requestBody = "{"
                + "\"user\": \"Test User\","
                + "\"municipality\": \"Lisboa\","
                + "\"requestedDate\": \"01-11-2026\","
                + "\"timeSlot\": \"10:00\","
                + "\"description\": \"Data inválida\""
                + "}";

        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/requests")
        .then()
            .statusCode(400);
    }

    @Test
    void testCreateServiceRequest_InvalidTimeFormat() {
        String requestBody = "{"
                + "\"user\": \"Test User\","
                + "\"municipality\": \"Lisboa\","
                + "\"requestedDate\": \"2026-11-01\","
                + "\"timeSlot\": \"10h\","
                + "\"description\": \"Hora inválida\""
                + "}";

        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/requests")
        .then()
            .statusCode(400);
    }

    @Test
    void testCreateServiceRequest_Conflict() {
        String requestBody = "{"
                + "\"user\": \"Test User\","
                + "\"municipality\": \"Porto\","
                + "\"requestedDate\": \"2025-11-03\","
                + "\"timeSlot\": \"11:00\","
                + "\"description\": \"Primeiro pedido\""
                + "}";

        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/requests")
        .then()
            .statusCode(200);

        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/requests")
        .then()
            .statusCode(409);
    }

    @Test
    void testGetRequestsByMunicipality_NotFound() {
        given()
            .port(port)
            .queryParam("municipality", "UnknownCity")
        .when()
            .get("/requests")
        .then()
            .statusCode(404);
    }

    @Test
    void testCancelServiceRequest_NotFound() {
        given()
            .port(port)
        .when()
            .delete("/requests/invalidtoken")
        .then()
            .statusCode(404);
    }

    @Test
    void testUpdateServiceRequestStatus_NotFound() {
        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body("\"ASSIGNED\"")
        .when()
            .put("/requests/invalidtoken/status")
        .then()
            .statusCode(404);
    }

    @Test
    void testGetMunicipalities() {
        given()
            .port(port)
        .when()
            .get("/requests/municipalities")
        .then()
            .statusCode(200)
            .body("$", not(empty()));
    }

    @Test
    void testGetRequestsByUser_Success() {
        // First create a service request
        String requestBody = "{"
                + "\"user\": \"Test User\","
                + "\"municipality\": \"Lisboa\","
                + "\"requestedDate\": \"2026-11-05\","
                + "\"timeSlot\": \"14:00\","
                + "\"description\": \"Test item\""
                + "}";

        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/requests")
        .then()
            .statusCode(200);

        // Now get requests by user
        given()
            .port(port)
        .when()
            .get("/requests/user/Test User")
        .then()
            .statusCode(200)
            .body("$", not(empty()))
            .body("[0].user.name", equalTo("Test User"))
            .body("[0].municipality.name", equalTo("Lisboa"));
    }

    @Test
    void testGetRequestsByUser_NotFound() {
        given()
            .port(port)
        .when()
            .get("/requests/user/NonExistentUser")
        .then()
            .statusCode(404);
    }

    @Test
    void testGetRequestsByUser_EmptyList() {
        // User exists but has no requests
        given()
            .port(port)
        .when()
            .get("/requests/user/Test User")
        .then()
            .statusCode(200)
            .body("$", empty());
    }
}
