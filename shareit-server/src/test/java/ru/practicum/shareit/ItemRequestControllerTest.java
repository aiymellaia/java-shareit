package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService requestService;

    private final String userIdHeader = "X-Sharer-User-Id";
    private ItemRequestDto responseDto;
    private ItemRequestInputDto inputDto;

    @BeforeEach
    void setUp() {
        inputDto = new ItemRequestInputDto();
        inputDto.setDescription("Нужна стремянка");

        responseDto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужна стремянка")
                .created(LocalDateTime.now())
                .items(Collections.emptyList())
                .build();
    }

    @Test
    void create_whenValid_thenReturns200AndJson() throws Exception {
        when(requestService.create(eq(1L), any(ItemRequestInputDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Нужна стремянка"))
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.items").isArray());

        verify(requestService, times(1)).create(eq(1L), any(ItemRequestInputDto.class));
    }

    @Test
    void getOwnRequests_thenReturns200AndList() throws Exception {
        when(requestService.getOwnRequests(1L)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(requestService, times(1)).getOwnRequests(1L);
    }

    @Test
    void getAllRequests_withPaginationParams_thenReturns200AndList() throws Exception {
        when(requestService.getAllRequests(anyLong(), anyInt(), anyInt())).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests/all")
                        .header(userIdHeader, 1L)
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));

        verify(requestService, times(1)).getAllRequests(1L, 0, 5);
    }

    @Test
    void getAllRequests_withoutParams_thenUsesDefaultValues() throws Exception {
        when(requestService.getAllRequests(anyLong(), anyInt(), anyInt())).thenReturn(List.of(responseDto));

        // Проверяем, что дефолтные значения defaultValue = "0" и "10" подставляются контроллером
        mockMvc.perform(get("/requests/all")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk());

        verify(requestService, times(1)).getAllRequests(1L, 0, 10);
    }

    @Test
    void getRequestById_whenRequestExists_thenReturns200() throws Exception {
        when(requestService.getRequestById(1L, 1L)).thenReturn(responseDto);

        mockMvc.perform(get("/requests/{requestId}", 1L)
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Нужна стремянка"));
    }

    @Test
    void getRequestById_whenNotFound_thenReturns404() throws Exception {
        when(requestService.getRequestById(1L, 99L))
                .thenThrow(new NotFoundException("Запрос не найден"));

        mockMvc.perform(get("/requests/{requestId}", 99L)
                        .header(userIdHeader, 1L))
                .andExpect(status().isNotFound());
    }
}