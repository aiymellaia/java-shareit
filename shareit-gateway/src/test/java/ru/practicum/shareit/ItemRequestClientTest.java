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
import ru.practicum.shareit.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class ItemRequestClientTest {

    private ItemRequestClient itemRequestClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);

        // Используем наш проверенный ручной стаб
        RestTemplateBuilder stubBuilder = new RestTemplateBuilder() {
            @Override
            public RestTemplateBuilder uriTemplateHandler(UriTemplateHandler handler) {
                restTemplate.setUriTemplateHandler(handler);
                return this;
            }

            @Override
            public RestTemplateBuilder requestFactory(Supplier<ClientHttpRequestFactory> requestFactorySupplier) {
                // Игнорируем сетевой Apache HttpClient
                return this;
            }

            @Override
            public RestTemplate build() {
                return restTemplate;
            }
        };

        itemRequestClient = new ItemRequestClient(stubBuilder);
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