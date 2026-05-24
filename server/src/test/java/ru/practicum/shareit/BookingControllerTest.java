package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.common.ProjectConstants.USER_ID_HEADER;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingInputDto inputDto;
    private BookingDto responseDto;

    @BeforeEach
    void setUp() {
        inputDto = new BookingInputDto();
        inputDto.setItemId(1L);
        inputDto.setStart(LocalDateTime.now().plusDays(1));
        inputDto.setEnd(LocalDateTime.now().plusDays(2));

        UserDto booker = UserDto.builder().id(1L).name("Аня").build();
        ItemDto item = ItemDto.builder().id(1L).name("Дрель").build();

        responseDto = BookingDto.builder()
                .id(1L)
                .start(inputDto.getStart())
                .end(inputDto.getEnd())
                .status(BookingStatus.WAITING)
                .booker(booker)
                .item(item)
                .build();
    }

    @Test
    void create_whenValid_thenReturns200AndJson() throws Exception {
        when(bookingService.create(eq(1L), any(BookingInputDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.name").value("Аня"))
                .andExpect(jsonPath("$.item.name").value("Дрель"));

        verify(bookingService, times(1)).create(eq(1L), any(BookingInputDto.class));
    }

    @Test
    void approve_whenValid_thenReturns200AndUpdatedStatus() throws Exception {
        responseDto.setStatus(BookingStatus.APPROVED);
        when(bookingService.approve(1L, 1L, true)).thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingService, times(1)).approve(1L, 1L, true);
    }

    @Test
    void getBooking_whenExists_thenReturns200() throws Exception {
        when(bookingService.getBookingById(1L, 1L)).thenReturn(responseDto);

        mockMvc.perform(get("/bookings/{bookingId}", 1L)
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getBooking_whenNotFound_thenReturns404() throws Exception {
        when(bookingService.getBookingById(1L, 99L))
                .thenThrow(new NotFoundException("Бронирование не найдено"));

        mockMvc.perform(get("/bookings/{bookingId}", 99L)
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookings_withStateParam_thenReturns200AndList() throws Exception {
        when(bookingService.getBookingsByBooker(1L, "FUTURE")).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "FUTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));

        verify(bookingService, times(1)).getBookingsByBooker(1L, "FUTURE");
    }

    @Test
    void getBookings_withoutStateParam_thenUsesDefaultAll() throws Exception {
        when(bookingService.getBookingsByBooker(1L, "ALL")).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).getBookingsByBooker(1L, "ALL");
    }

    @Test
    void getBookingsByOwner_withoutStateParam_thenUsesDefaultAll() throws Exception {
        when(bookingService.getBookingsByOwner(1L, "ALL")).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).getBookingsByOwner(1L, "ALL");
    }
}