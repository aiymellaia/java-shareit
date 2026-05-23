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
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.client.BookingClient;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    private final String userIdHeader = "X-Sharer-User-Id";

    @Test
    void createBooking_whenValid_thenReturns200() throws Exception {
        BookingInputDto inputDto = new BookingInputDto();
        inputDto.setItemId(1L);
        inputDto.setStart(LocalDateTime.now().plusDays(1));
        inputDto.setEnd(LocalDateTime.now().plusDays(2));

        ResponseEntity<Object> mockResponse = new ResponseEntity<>("Success Booking JSON", HttpStatus.OK);
        when(bookingClient.createBooking(anyLong(), any(BookingInputDto.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/bookings")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk());
    }

    @Test
    void createBooking_whenInvalidDto_thenReturns400() throws Exception {
        // Оставляем поля null, чтобы сработал @NotNull в DTO шлюза
        BookingInputDto invalidDto = new BookingInputDto();

        mockMvc.perform(post("/bookings")
                        .header(userIdHeader, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking_thenReturns200() throws Exception {
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(bookingClient.approveBooking(anyLong(), anyLong(), any(Boolean.class))).thenReturn(mockResponse);

        mockMvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header(userIdHeader, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingById_thenReturns200() throws Exception {
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(bookingClient.getBookingById(anyLong(), anyLong())).thenReturn(mockResponse);

        mockMvc.perform(get("/bookings/{bookingId}", 1L)
                        .header(userIdHeader, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getBookings_whenValidState_thenReturns200() throws Exception {
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(bookingClient.getBookingsByBooker(anyLong(), eq("WAITING"))).thenReturn(mockResponse);

        mockMvc.perform(get("/bookings")
                        .header(userIdHeader, 1L)
                        .param("state", "waiting")) // проверяем, что приводится к upperCase
                .andExpect(status().isOk());
    }

    @Test
    void getBookings_whenUnknownState_thenReturns400() throws Exception {
        // На этот запрос шлюз должен ответить сам (400 Bad Request) с ошибкой в теле
        mockMvc.perform(get("/bookings")
                        .header(userIdHeader, 1L)
                        .param("state", "UNSUPPORTED_STATE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unknown state: UNSUPPORTED_STATE"));
    }

    @Test
    void getBookingsByOwner_whenUnknownState_thenReturns400() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header(userIdHeader, 1L)
                        .param("state", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unknown state: INVALID"));
    }
}