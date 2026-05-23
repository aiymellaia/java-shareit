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
import ru.practicum.shareit.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class ItemClientTest {

    private ItemClient itemClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);

        // Используем проверенный ручной стаб, изолирующий Apache HttpClient от сети
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

        itemClient = new ItemClient(stubBuilder);
    }

    @Test
    void testItemClientMethods() {
        Long userId = 1L;
        Long itemId = 2L;

        mockServer.expect(requestTo("http://localhost:9090/items"))
                .andRespond(withStatus(HttpStatus.CREATED));
        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .build();
        ResponseEntity<Object> responseCreate = itemClient.createItem(userId, itemDto);
        assertNotNull(responseCreate);
        assertEquals(HttpStatus.CREATED, responseCreate.getStatusCode());

        mockServer.reset();
        mockServer.expect(requestTo("http://localhost:9090/items/2"))
                .andRespond(withStatus(HttpStatus.OK));
        ResponseEntity<Object> responseUpdate = itemClient.updateItem(userId, itemId, itemDto);
        assertNotNull(responseUpdate);
        assertEquals(HttpStatus.OK, responseUpdate.getStatusCode());

        mockServer.reset();
        mockServer.expect(requestTo("http://localhost:9090/items/2"))
                .andRespond(withStatus(HttpStatus.OK));
        ResponseEntity<Object> responseById = itemClient.getItemById(userId, itemId);
        assertNotNull(responseById);

        mockServer.reset();
        mockServer.expect(requestTo("http://localhost:9090/items"))
                .andRespond(withStatus(HttpStatus.OK));
        ResponseEntity<Object> responseByOwner = itemClient.getItemsByOwner(userId);
        assertNotNull(responseByOwner);

        mockServer.reset();
        mockServer.expect(requestTo("http://localhost:9090/items/search?text=drill"))
                .andRespond(withStatus(HttpStatus.OK));
        ResponseEntity<Object> responseSearch = itemClient.searchItems("drill");
        assertNotNull(responseSearch);

        mockServer.reset();
        mockServer.expect(requestTo("http://localhost:9090/items/2/comment"))
                .andRespond(withStatus(HttpStatus.OK));
        CommentDto commentDto = new CommentDto();
        ResponseEntity<Object> responseComment = itemClient.addComment(userId, itemId, commentDto);
        assertNotNull(responseComment);

        mockServer.verify();
    }
}