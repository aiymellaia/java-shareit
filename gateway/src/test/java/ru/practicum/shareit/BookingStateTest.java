package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.BookingState;

import static org.junit.jupiter.api.Assertions.*;

class BookingStateTest {

    @Test
    void testBookingStateValues() {
        assertEquals(BookingState.valueOf("ALL"), BookingState.ALL);
        assertEquals(BookingState.valueOf("CURRENT"), BookingState.CURRENT);
        assertEquals(BookingState.valueOf("PAST"), BookingState.PAST);
        assertEquals(BookingState.valueOf("FUTURE"), BookingState.FUTURE);
        assertEquals(BookingState.valueOf("WAITING"), BookingState.WAITING);
        assertEquals(BookingState.valueOf("REJECTED"), BookingState.REJECTED);
    }
}