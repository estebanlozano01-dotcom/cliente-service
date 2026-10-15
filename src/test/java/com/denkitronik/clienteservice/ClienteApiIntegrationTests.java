package com.denkitronik.clienteservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClienteApiIntegrationTests {

    private static final String BASE_URL = "/api/v1/cliente-service";
    private static final String VALID_CLIENTE = """
            {
              "nombre": "Nora",
              "apellido": "Lopez",
              "email": "nora.lopez.unique@example.com",
              "foto": "nora.jpg",
              "region": { "id": 1 }
            }
            """;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    void listsSeededClientsPagesAndRegions() throws Exception {
        JsonNode all = json(get("/clientes").getBody());
        assertEquals(12, all.size());

        JsonNode firstPage = json(get("/clientes/page/0").getBody());
        assertEquals(4, firstPage.path("content").size());
        assertTrue(firstPage.has("totalElements"));

        JsonNode emptyPage = json(get("/clientes/page/100").getBody());
        assertTrue(emptyPage.path("content").isEmpty());

        assertEquals(8, json(get("/clientes/regiones").getBody()).size());
    }

    @Test
    @Order(2)
    void getsSeededClient() throws Exception {
        ResponseEntity<String> response = get("/clientes/1");

        assertEquals(200, response.getStatusCode().value());
        JsonNode cliente = json(response.getBody());
        assertEquals(1, cliente.path("id").asInt());
        assertFalse(cliente.path("createAt").isNull());
    }

    @Test
    @Order(3)
    void missingClientHasStructured404() throws Exception {
        ResponseEntity<String> response = get("/clientes/999");

        assertEquals(404, response.getStatusCode().value());
        JsonNode error = json(response.getBody());
        assertEquals(404, error.path("status").asInt());
        assertEquals("Not Found", error.path("error").asText());
        assertTrue(error.path("mensaje").asText().contains("999"));
        assertTrue(error.path("timestamp").isTextual());
    }

    @Test
    @Order(4)
    void invalidPostHasStructured400() throws Exception {
        ResponseEntity<String> response = post("/clientes",
                """
                {"nombre":"","apellido":"","email":"no-email","region":null}
                """);

        assertEquals(400, response.getStatusCode().value());
        JsonNode error = json(response.getBody());
        assertEquals(400, error.path("status").asInt());
        assertTrue(error.path("mensaje").asText().contains("nombre"));
    }

    @Test
    @Order(5)
    void malformedJsonHasStructured400() throws Exception {
        ResponseEntity<String> response = post("/clientes", "{\"nombre\":");

        assertEquals(400, response.getStatusCode().value());
        JsonNode error = json(response.getBody());
        assertEquals("JSON malformado", error.path("mensaje").asText());
        assertTrue(error.path("timestamp").isTextual());
    }

    @Test
    @Order(6)
    void validPostReturnsCreatedClient() throws Exception {
        ResponseEntity<String> response = post("/clientes", VALID_CLIENTE);

        assertEquals(201, response.getStatusCode().value());
        JsonNode cliente = json(response.getBody());
        assertTrue(cliente.path("id").asLong() > 0);
        assertFalse(cliente.path("createAt").isMissingNode());
        assertFalse(cliente.path("createAt").isNull());
    }

    @Test
    @Order(7)
    void duplicatePostReturnsConflict() throws Exception {
        ResponseEntity<String> response = post("/clientes",
                """
                {"nombre":"Duplicado","apellido":"Email","email":"linus@kernel.org",
                 "region":{"id":1}}
                """);

        assertEquals(409, response.getStatusCode().value());
        assertEquals(409, json(response.getBody()).path("status").asInt());
    }

    @Test
    @Order(8)
    void putMissingClientReturns404BeforeValidatingBody() throws Exception {
        ResponseEntity<String> validBody = put("/clientes/999", VALID_CLIENTE);
        ResponseEntity<String> invalidBody = put("/clientes/999",
                """
                {"nombre":"","apellido":"","email":"bad","region":null}
                """);

        assertEquals(404, validBody.getStatusCode().value());
        assertEquals(404, invalidBody.getStatusCode().value());
        assertTrue(json(invalidBody.getBody()).path("mensaje").asText().contains("999"));
    }

    @Test
    @Order(9)
    void updatesExistingClientAndPreservesConflictHandling() throws Exception {
        ResponseEntity<String> update = put("/clientes/2",
                """
                {"nombre":"Ada","apellido":"Lovelace","email":"ada@analytical.example",
                 "foto":"ada.jpg","region":{"id":3}}
                """);
        assertEquals(200, update.getStatusCode().value());

        ResponseEntity<String> duplicateEmail = put("/clientes/1",
                """
                {"nombre":"Linus","apellido":"Torvalds","email":"ada@analytical.example",
                 "foto":"linus.jpg","region":{"id":1}}
                """);
        assertEquals(409, duplicateEmail.getStatusCode().value());
        assertEquals(409, json(duplicateEmail.getBody()).path("status").asInt());
    }

    @Test
    @Order(10)
    void missingRouteHasStructured404() throws Exception {
        ResponseEntity<String> response = get("/ruta-inexistente");

        assertEquals(404, response.getStatusCode().value());
        assertEquals(404, json(response.getBody()).path("status").asInt());
    }

    @Test
    @Order(11)
    void deletingMissingClientReturnsStructured404() throws Exception {
        ResponseEntity<String> response = delete("/clientes/999");

        assertEquals(404, response.getStatusCode().value());
        assertEquals(404, json(response.getBody()).path("status").asInt());
    }

    @Test
    @Order(12)
    void deletingExistingClientReturns204Then404() throws Exception {
        ResponseEntity<String> deleted = delete("/clientes/1");
        assertEquals(204, deleted.getStatusCode().value());
        assertTrue(deleted.getBody() == null || deleted.getBody().isEmpty());

        ResponseEntity<String> missing = get("/clientes/1");
        assertEquals(404, missing.getStatusCode().value());
        assertEquals(404, json(missing.getBody()).path("status").asInt());
    }

    private ResponseEntity<String> get(String path) {
        return restTemplate.getForEntity(BASE_URL + path, String.class);
    }

    private ResponseEntity<String> post(String path, String body) {
        return exchange(path, HttpMethod.POST, body);
    }

    private ResponseEntity<String> put(String path, String body) {
        return exchange(path, HttpMethod.PUT, body);
    }

    private ResponseEntity<String> delete(String path) {
        return exchange(path, HttpMethod.DELETE, null);
    }

    private ResponseEntity<String> exchange(String path, HttpMethod method, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(BASE_URL + path, method,
                new HttpEntity<>(body, headers), String.class);
    }

    private JsonNode json(String body) throws Exception {
        return objectMapper.readTree(body);
    }
}
