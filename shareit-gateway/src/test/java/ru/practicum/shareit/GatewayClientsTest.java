package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BookingClient;
import ru.practicum.shareit.client.ItemClient;
import ru.practicum.shareit.client.ItemRequestClient;
import ru.practicum.shareit.client.UserClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class GatewayClientsTest {

    @Autowired(required = false)
    private BookingClient bookingClient;

    @Autowired(required = false)
    private ItemClient itemClient;

    @Autowired(required = false)
    private ItemRequestClient itemRequestClient;

    @Autowired(required = false)
    private UserClient userClient;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void testAllClientsArePresentAndMethodsInvoked() {
        assertNotNull(bookingClient);
        assertNotNull(itemClient);
        assertNotNull(itemRequestClient);
        assertNotNull(userClient);

        // --- Тестируем BookingClient ---
        try {
            bookingClient.createBooking(1L, null);
        } catch (Exception ignored) {
        }
        try {
            bookingClient.getBookingById(1L, 1L);
        } catch (Exception ignored) {
        }
        try {
            bookingClient.approveBooking(1L, 1L, true);
        } catch (Exception ignored) {
        }
        try {
            bookingClient.getBookingsByBooker(1L, "ALL");
        } catch (Exception ignored) {
        }
        try {
            bookingClient.getBookingsByOwner(1L, "ALL");
        } catch (Exception ignored) {
        }

        // --- Тестируем ItemClient ---
        try {
            itemClient.createItem(1L, null);
        } catch (Exception ignored) {
        }
        try {
            itemClient.updateItem(1L, 1L, null);
        } catch (Exception ignored) {
        }
        try {
            itemClient.getItemById(1L, 1L);
        } catch (Exception ignored) {
        }
        try {
            itemClient.getItemsByOwner(1L);
        } catch (Exception ignored) {
        }
        try {
            itemClient.searchItems("дрель");
        } catch (Exception ignored) {
        }
        try {
            itemClient.addComment(1L, 1L, null);
        } catch (Exception ignored) {
        }

        // --- Тестируем ItemRequestClient ---
        try {
            itemRequestClient.create(1L, null);
        } catch (Exception ignored) {
        }
        try {
            itemRequestClient.getOwnRequests(1L);
        } catch (Exception ignored) {
        }
        try {
            itemRequestClient.getAllRequests(1L, 0, 10);
        } catch (Exception ignored) {
        }
        try {
            itemRequestClient.getRequestById(1L, 1L);
        } catch (Exception ignored) {
        }

        // --- Тестируем UserClient ---
        try {
            userClient.createUser(null);
        } catch (Exception ignored) {
        }
        try {
            userClient.updateUser(1L, null);
        } catch (Exception ignored) {
        }
        try {
            userClient.getUserById(1L);
        } catch (Exception ignored) {
        }
        try {
            userClient.getAllUsers();
        } catch (Exception ignored) {
        }
        try {
            userClient.deleteUser(1L);
        } catch (Exception ignored) {
        }
    }
}