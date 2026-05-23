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
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.client.BookingClient;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class BookingClientTest {

    private BookingClient bookingClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);

        // Наш проверенный стаб, отсекающий реальную сеть
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

        bookingClient = new BookingClient(stubBuilder);
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