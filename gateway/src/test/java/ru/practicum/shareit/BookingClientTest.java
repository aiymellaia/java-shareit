package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.client.BookingClient;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class BookingClientTest {

    private BookingClient bookingClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        // 1. Создаем RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // 2. Устанавливаем обработчик URI с учетом API_PREFIX
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:9090/bookings"));

        // 3. Создаем сервер
        mockServer = MockRestServiceServer.createServer(restTemplate);

        // 4. Инициализируем клиент, передавая restTemplate напрямую
        bookingClient = new BookingClient(restTemplate);
    }

    @Test
    void testBookingClientMethods() {
        Long userId = 1L;
        Long bookingId = 2L;

        mockServer.expect(requestTo("http://localhost:9090/bookings"))
                .andRespond(withStatus(HttpStatus.CREATED));
        BookingInputDto bookingInputDto = new BookingInputDto();
        ResponseEntity<Object> responseCreate = bookingClient.createBooking(userId, bookingInputDto);
        assertNotNull(responseCreate);
        assertEquals(HttpStatus.CREATED, responseCreate.getStatusCode());

        mockServer.reset();
        mockServer.expect(requestTo("http://localhost:9090/bookings/2?approved=true"))
                .andRespond(withStatus(HttpStatus.OK));
        ResponseEntity<Object> responseApprove = bookingClient.approveBooking(userId, bookingId, true);
        assertNotNull(responseApprove);
        assertEquals(HttpStatus.OK, responseApprove.getStatusCode());

        mockServer.reset();
        mockServer.expect(requestTo("http://localhost:9090/bookings/2"))
                .andRespond(withStatus(HttpStatus.OK));
        ResponseEntity<Object> responseById = bookingClient.getBookingById(userId, bookingId);
        assertNotNull(responseById);

        mockServer.reset();
        mockServer.expect(requestTo("http://localhost:9090/bookings?state=ALL"))
                .andRespond(withStatus(HttpStatus.OK));
        ResponseEntity<Object> responseByBooker = bookingClient.getBookingsByBooker(userId, "ALL");
        assertNotNull(responseByBooker);

        mockServer.reset();
        mockServer.expect(requestTo("http://localhost:9090/bookings/owner?state=FUTURE"))
                .andRespond(withStatus(HttpStatus.OK));
        ResponseEntity<Object> responseByOwner = bookingClient.getBookingsByOwner(userId, "FUTURE");
        assertNotNull(responseByOwner);

        mockServer.verify();
    }
}