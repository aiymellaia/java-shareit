package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.UserClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class UserClientTest {

    private UserClient userClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        // 1. Создаем RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // 2. ВАЖНО: Настраиваем базовый URL здесь!
        // BaseClient ожидает, что UriTemplateHandler знает, куда слать запросы
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:9090/users"));

        // 3. Создаем MockServer
        mockServer = MockRestServiceServer.createServer(restTemplate);

        // 4. Инициализируем клиент через тестовый конструктор
        userClient = new UserClient(restTemplate);
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