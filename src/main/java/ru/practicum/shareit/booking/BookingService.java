package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;

import java.util.List;

public interface BookingService {

    BookingDto create(Long userId, BookingInputDto bookingInputDto);

    BookingDto approve(Long userId, Long bookingId, Boolean approved);

    BookingDto getBookingById(Long userId, Long bookingId);

    List<BookingDto> getBookingsByBooker(Long userId, String state);

    List<BookingDto> getBookingsByOwner(Long userId, String state);
}