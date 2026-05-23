package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingDtoTest {

    @Test
    void testBookingDto() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        ItemDto item = ItemDto.builder().id(1L).name("Item").build();
        UserDto booker = UserDto.builder().id(2L).name("User").build();

        // Тестируем Builder и AllArgsConstructor
        BookingDto dto = BookingDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        // Тестируем геттеры
        assertEquals(1L, dto.getId());
        assertEquals(start, dto.getStart());
        assertEquals(end, dto.getEnd());
        assertEquals(item, dto.getItem());
        assertEquals(booker, dto.getBooker());
        assertEquals(BookingStatus.WAITING, dto.getStatus());

        // Тестируем NoArgsConstructor и сеттеры
        BookingDto emptyDto = new BookingDto();
        emptyDto.setId(2L);
        assertEquals(2L, emptyDto.getId());

        // Тестируем toString, equals и hashCode для 100% покрытия Lombok @Data
        assertNotNull(dto.toString());
        assertEquals(dto, dto);
        assertNotEquals(dto, emptyDto);
        assertEquals(dto.hashCode(), dto.hashCode());
    }

    @Test
    void testBookingInputDto() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        // Тестируем сеттеры и геттеры (@Data)
        BookingInputDto inputDto = new BookingInputDto();
        inputDto.setItemId(1L);
        inputDto.setStart(start);
        inputDto.setEnd(end);

        assertEquals(1L, inputDto.getItemId());
        assertEquals(start, inputDto.getStart());
        assertEquals(end, inputDto.getEnd());

        // Тестируем toString, equals и hashCode
        assertNotNull(inputDto.toString());
        assertEquals(inputDto, inputDto);
        assertEquals(inputDto.hashCode(), inputDto.hashCode());
    }

    @Test
    void testBookingShortDto() {
        // Тестируем AllArgsConstructor
        BookingShortDto shortDto = new BookingShortDto(1L, 2L);

        assertEquals(1L, shortDto.getId());
        assertEquals(2L, shortDto.getBookerId());

        // Тестируем NoArgsConstructor и сеттеры
        BookingShortDto emptyShortDto = new BookingShortDto();
        emptyShortDto.setId(3L);
        emptyShortDto.setBookerId(4L);

        assertEquals(3L, emptyShortDto.getId());
        assertEquals(4L, emptyShortDto.getBookerId());

        // Тестируем toString, equals и hashCode
        assertNotNull(shortDto.toString());
        assertEquals(shortDto, shortDto);
        assertNotEquals(shortDto, emptyShortDto);
        assertEquals(shortDto.hashCode(), shortDto.hashCode());
    }
}