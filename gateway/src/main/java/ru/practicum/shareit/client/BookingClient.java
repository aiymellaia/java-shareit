package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookingInputDto;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(RestTemplateBuilder builder,
                         @Value("${shareit.server.url:http://localhost:9090}") String serverUrl) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new org.springframework.http.client.HttpComponentsClientHttpRequestFactory(
                                org.apache.hc.client5.http.impl.classic.HttpClients.createDefault()
                        ))
                        .build()
        );
    }

    public BookingClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<Object> createBooking(Long userId, BookingInputDto bookingInputDto) {
        return post("", userId, bookingInputDto);
    }

    public ResponseEntity<Object> approveBooking(Long userId, Long bookingId, Boolean approved) {
        Map<String, Object> parameters = Map.of("approved", approved);
        return patch("/" + bookingId + "?approved={approved}", userId, parameters, null);
    }

    public ResponseEntity<Object> getBookingById(Long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getBookingsByBooker(Long userId, String state) {
        Map<String, Object> parameters = Map.of("state", state);
        return get("?state={state}", userId, parameters);
    }

    public ResponseEntity<Object> getBookingsByOwner(Long userId, String state) {
        Map<String, Object> parameters = Map.of("state", state);
        return get("/owner?state={state}", userId, parameters);
    }
}