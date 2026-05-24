package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import static ru.practicum.shareit.common.ProjectConstants.USER_ID_HEADER;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private ItemDto itemDto;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        itemDto = ItemDto.builder()
                .id(1L)
                .name("Отвертка")
                .description("Крестовая, магнитная")
                .available(true)
                .comments(Collections.emptyList())
                .build();

        commentDto = CommentDto.builder()
                .id(1L)
                .text("Отличный инструмент, выручил!")
                .authorName("Иван")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void create_whenValid_thenReturns200AndJson() throws Exception {
        when(itemService.create(eq(1L), any(ItemDto.class))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Отвертка"))
                .andExpect(jsonPath("$.available").value(true));

        verify(itemService, times(1)).create(eq(1L), any(ItemDto.class));
    }

    @Test
    void update_whenValid_thenReturns200AndUpdatedJson() throws Exception {
        ItemDto updateDto = ItemDto.builder().name("Супер Отвертка").build();
        ItemDto resultDto = ItemDto.builder().id(1L).name("Супер Отвертка").available(true).build();

        when(itemService.update(eq(1L), eq(1L), any(ItemDto.class))).thenReturn(resultDto);

        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Супер Отвертка"));
    }

    @Test
    void getItemById_whenExists_thenReturns200() throws Exception {
        when(itemService.getItemById(1L, 1L)).thenReturn(itemDto);

        mockMvc.perform(get("/items/{itemId}", 1L)
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Крестовая, магнитная"));
    }

    @Test
    void getItemsByOwner_thenReturns200AndList() throws Exception {
        when(itemService.getItemsByOwner(1L)).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Отвертка"));
    }

    @Test
    void search_withTextParam_thenReturns200AndList() throws Exception {
        when(itemService.search("Отвертка")).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "Отвертка"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(itemService, times(1)).search("Отвертка");
    }

    @Test
    void addComment_whenValid_thenReturns200AndCommentJson() throws Exception {
        when(itemService.addComment(eq(1L), eq(1L), any(CommentDto.class))).thenReturn(commentDto);

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Отличный инструмент, выручил!"))
                .andExpect(jsonPath("$.authorName").value("Иван"))
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    void addComment_whenUserCannotComment_thenReturns400() throws Exception {
        when(itemService.addComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenThrow(new ValidationException("Вы не арендатор вещи"));

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }
}