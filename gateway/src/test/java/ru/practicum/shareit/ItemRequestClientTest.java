package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class ItemRequestClientTest {

    private ItemRequestClient itemRequestClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        // 1. Создаем RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // 2. Настраиваем URI хэндлер с полным путем (с учетом API_PREFIX)
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:9090/requests"));

        // 3. Создаем MockServer
        mockServer = MockRestServiceServer.createServer(restTemplate);

        // 4. Передаем тот же самый объект напрямую
        itemRequestClient = new ItemRequestClient(restTemplate);
    }

    @Test
    void testItemRequestClientMethods() {
        Long userId = 1L;
        Long requestId = 2L;

        // 1. Проверяем create (POST на базовый префикс)
        mockServer.expect(requestTo("http://localhost:9090/requests"))
                .andRespond(withStatus(HttpStatus.CREATED));
        ItemRequestInputDto inputDto = new ItemRequestInputDto();
        ResponseEntity<Object> responseCreate = itemRequestClient.create(userId, inputDto);
        assertNotNull(responseCreate);
        assertEquals(HttpStatus.CREATED, responseCreate.getStatusCode());

        // 2. Проверяем getOwnRequests (GET на базовый префикс)
        mockServer.reset();
        mockServer.expect(requestTo("http://localhost:9090/requests"))
                .andRespond(withStatus(HttpStatus.OK));
        ResponseEntity<Object> responseOwn = itemRequestClient.getOwnRequests(userId);
        assertNotNull(responseOwn);
        assertEquals(HttpStatus.OK, responseOwn.getStatusCode());

        mockServer.reset();
        mockServer.expect(requestTo("http://localhost:9090/requests/2"))
                .andRespond(withStatus(HttpStatus.OK));
        ResponseEntity<Object> responseById = itemRequestClient.getRequestById(userId, requestId);
        assertNotNull(responseById);
        assertEquals(HttpStatus.OK, responseById.getStatusCode());

        mockServer.reset();
        mockServer.expect(requestTo("http://localhost:9090/requests/all?from=0&size=10"))
                .andRespond(withStatus(HttpStatus.OK));
        ResponseEntity<Object> responseAll = itemRequestClient.getAllRequests(userId, 0, 10);
        assertNotNull(responseAll);
        assertEquals(HttpStatus.OK, responseAll.getStatusCode());

        mockServer.verify();
    }
}