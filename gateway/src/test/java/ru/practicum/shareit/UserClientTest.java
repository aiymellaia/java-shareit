package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplateHandler;
import ru.practicum.shareit.client.UserClient;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class UserClientTest {

    private UserClient userClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);

        RestTemplateBuilder stubBuilder = new RestTemplateBuilder() {
            @Override
            public RestTemplateBuilder uriTemplateHandler(UriTemplateHandler handler) {
                restTemplate.setUriTemplateHandler(handler);
                return this;
            }

            @Override
            public RestTemplateBuilder requestFactory(Supplier<ClientHttpRequestFactory> requestFactorySupplier) {
                return this;
            }

            @Override
            public RestTemplate build() {
                return restTemplate;
            }
        };

        userClient = new UserClient(stubBuilder);
    }

    @Test
    void testUserClientMethods() {
        mockServer.expect(requestTo("http://localhost:9090/users"))
                .andRespond(withStatus(HttpStatus.OK));
        ResponseEntity<Object> responseGetAll = userClient.getAllUsers();
        assertNotNull(responseGetAll);
        assertEquals(HttpStatus.OK, responseGetAll.getStatusCode());

        mockServer.reset();
        mockServer.expect(requestTo("http://localhost:9090/users/1"))
                .andRespond(withStatus(HttpStatus.OK));
        ResponseEntity<Object> responseGetById = userClient.getUserById(1L);
        assertNotNull(responseGetById);

        mockServer.reset();
        mockServer.expect(requestTo("http://localhost:9090/users"))
                .andRespond(withStatus(HttpStatus.CREATED));
        ResponseEntity<Object> responseCreate = userClient.createUser(null);
        assertNotNull(responseCreate);
        assertEquals(HttpStatus.CREATED, responseCreate.getStatusCode());

        mockServer.reset();
        mockServer.expect(requestTo("http://localhost:9090/users/1"))
                .andRespond(withStatus(HttpStatus.OK));
        ResponseEntity<Object> responseUpdate = userClient.updateUser(1L, null);
        assertNotNull(responseUpdate);

        mockServer.reset();
        mockServer.expect(requestTo("http://localhost:9090/users/1"))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));
        ResponseEntity<Object> responseDelete = userClient.deleteUser(1L);
        assertNotNull(responseDelete);
        assertEquals(HttpStatus.NO_CONTENT, responseDelete.getStatusCode());

        mockServer.verify();
    }
}