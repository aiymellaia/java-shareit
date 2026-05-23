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
import ru.practicum.shareit.client.ItemRequestClient;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ItemRequestController.class, ErrorHandler.class})
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient requestClient;

    private final String userIdHeader = "X-Sharer-User-Id";

    @Test
    void create_whenValid_thenReturns200() throws Exception {
        ItemRequestInputDto inputDto = new ItemRequestInputDto();
        inputDto.setDescription("Нужна стремянка на вечер");

        ResponseEntity<Object> mockResponse = new ResponseEntity<>("Success Request JSON", HttpStatus.OK);
        when(requestClient.create(anyLong(), any(ItemRequestInputDto.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/requests")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk());
    }

    @Test
    void create_whenDescriptionIsEmpty_thenReturns400() throws Exception {
        ItemRequestInputDto invalidDto = new ItemRequestInputDto();
        invalidDto.setDescription(""); // Нарушаем @NotBlank в DTO

        mockMvc.perform(post("/requests")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOwnRequests_thenReturns200() throws Exception {
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(requestClient.getOwnRequests(anyLong())).thenReturn(mockResponse);

        mockMvc.perform(get("/requests")
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllRequests_whenValidParams_thenReturns200() throws Exception {
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(requestClient.getAllRequests(anyLong(), anyInt(), anyInt())).thenReturn(mockResponse);

        mockMvc.perform(get("/requests/all")
                        .header(userIdHeader, 1L)
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllRequests_whenFromIsNegative_thenReturns400() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header(userIdHeader, 1L)
                        .param("from", "-1") // Нарушаем @PositiveOrZero
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllRequests_whenSizeIsZero_thenReturns400() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header(userIdHeader, 1L)
                        .param("from", "0")
                        .param("size", "0")) // Нарушаем @Positive (size должен быть строго > 0)
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestById_thenReturns200() throws Exception {
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(requestClient.getRequestById(anyLong(), anyLong())).thenReturn(mockResponse);

        mockMvc.perform(get("/requests/{requestId}", 1L)
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk());
    }
}