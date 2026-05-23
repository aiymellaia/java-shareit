package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.client.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    private final String userIdHeader = "X-Sharer-User-Id";

    @Test
    void createItem_whenValid_thenReturns200() throws Exception {
        ItemDto validItem = ItemDto.builder()
                .name("Отвертка")
                .description("Крестовая, удобная")
                .available(true)
                .build();

        ResponseEntity<Object> mockResponse = new ResponseEntity<>("Success Item JSON", HttpStatus.OK);
        when(itemClient.createItem(anyLong(), any(ItemDto.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/items")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItem)))
                .andExpect(status().isOk());
    }

    @Test
    void createItem_whenNameIsBlank_thenReturns400() throws Exception {
        ItemDto invalidItem = ItemDto.builder()
                .name("") // Нарушаем @NotBlank
                .description("Описание")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItem)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItem_whenAvailableIsNull_thenReturns400() throws Exception {
        ItemDto invalidItem = ItemDto.builder()
                .name("Дрель")
                .description("Мощная")
                .available(null) // Нарушаем @NotNull
                .build();

        mockMvc.perform(post("/items")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItem)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_thenReturns200() throws Exception {
        ItemDto updateItem = ItemDto.builder().name("Новое название").build();

        ResponseEntity<Object> mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(itemClient.updateItem(anyLong(), anyLong(), any(ItemDto.class))).thenReturn(mockResponse);

        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateItem)))
                .andExpect(status().isOk());
    }

    @Test
    void getItemById_thenReturns200() throws Exception {
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(itemClient.getItemById(anyLong(), anyLong())).thenReturn(mockResponse);

        mockMvc.perform(get("/items/{itemId}", 1L)
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getItemsByOwner_thenReturns200() throws Exception {
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(itemClient.getItemsByOwner(anyLong())).thenReturn(mockResponse);

        mockMvc.perform(get("/items")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void search_thenReturns200() throws Exception {
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(itemClient.searchItems(anyString())).thenReturn(mockResponse);

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_whenValid_thenReturns200() throws Exception {
        CommentDto validComment = CommentDto.builder()
                .text("Всё супер, дрель отличная!")
                .build();

        ResponseEntity<Object> mockResponse = new ResponseEntity<>("Success Comment JSON", HttpStatus.OK);
        when(itemClient.addComment(anyLong(), anyLong(), any(CommentDto.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validComment)))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_whenTextIsBlank_thenReturns400() throws Exception {
        CommentDto invalidComment = CommentDto.builder()
                .text("") // Нарушаем @NotBlank, который мы добавили шлюзу
                .build();

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidComment)))
                .andExpect(status().isBadRequest());
    }
}