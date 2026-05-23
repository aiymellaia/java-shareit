package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class BaseClientTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    private TestClient testClient;
    private ObjectMapper objectMapper;

    private static class TestClient extends BaseClient {
        public TestClient(RestTemplate rest) {
            super(rest);
        }

        public ResponseEntity<Object> publicGet(String path) {
            return super.get(path);
        }

        public ResponseEntity<Object> publicGet(String path, long userId) {
            return super.get(path, userId);
        }

        public <T> ResponseEntity<Object> publicPost(String path, T body) {
            return super.post(path, body);
        }

        public <T> ResponseEntity<Object> publicPatch(String path, Long userId, Map<String, Object> parameters, T body) {
            return super.patch(path, userId, parameters, body);
        }

        public ResponseEntity<Object> publicDelete(String path) {
            return super.delete(path);
        }
    }

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        testClient = new TestClient(restTemplate);
        objectMapper = new ObjectMapper();
    }

    @Test
    void get_withUserId_shouldSendCorrectHeadersAndReturn200() {
        mockServer.expect(requestTo("/items/1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "42"))
                .andExpect(header("Accept", MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = testClient.publicGet("/items/1", 42L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    void post_withBody_shouldSendJsonAndReturn201() throws Exception {
        Map<String, String> body = Map.of("name", "Дрель");
        String jsonBody = objectMapper.writeValueAsString(body);

        mockServer.expect(requestTo("/items"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(jsonBody))
                .andRespond(withStatus(HttpStatus.CREATED).body("{\"id\":1}").contentType(MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = testClient.publicPost("/items", body);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    void patch_withParameters_shouldReplacePlaceholdersAndReturn200() {
        mockServer.expect(requestTo("/items/1?state=ALL"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = testClient.publicPatch("/items/1?state={state}", null, Map.of("state", "ALL"), null);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    void delete_shouldSendRequestWithoutBodyAndReturn204() {
        mockServer.expect(requestTo("/items/1"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withNoContent());

        ResponseEntity<Object> response = testClient.publicDelete("/items/1");

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    void makeAndSendRequest_whenServerThrowsException_shouldReturnServerStatusAndBytes() {
        mockServer.expect(requestTo("/items/99"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withResourceNotFound().body("Вещь не найдена"));

        ResponseEntity<Object> response = testClient.publicGet("/items/99");

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        mockServer.verify();
    }
}