package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemDtoTest {

    @Test
    void testItemDto() {
        List<CommentDto> comments = Collections.emptyList();
        BookingShortDto lastBooking = new BookingShortDto(1L, 2L);
        BookingShortDto nextBooking = new BookingShortDto(3L, 4L);

        // Тестируем Builder
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .requestId(5L)
                .comments(comments)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .build();

        // Тестируем геттеры
        assertEquals(1L, dto.getId());
        assertEquals("Дрель", dto.getName());
        assertEquals("Мощная дрель", dto.getDescription());
        assertTrue(dto.getAvailable());
        assertEquals(5L, dto.getRequestId());
        assertEquals(comments, dto.getComments());
        assertEquals(lastBooking, dto.getLastBooking());
        assertEquals(nextBooking, dto.getNextBooking());

        // Тестируем сеттеры (@Data)
        ItemDto secondDto = ItemDto.builder().build();
        secondDto.setId(2L);
        secondDto.setName("Отвертка");
        assertEquals(2L, secondDto.getId());
        assertEquals("Отвертка", secondDto.getName());

        // Тестируем toString, equals и hashCode для Lombok
        assertNotNull(dto.toString());
        assertEquals(dto, dto);
        assertNotEquals(dto, secondDto);
        assertEquals(dto.hashCode(), dto.hashCode());
    }

    @Test
    void testCommentDto() {
        LocalDateTime createdTime = LocalDateTime.now();

        // Тестируем Builder и AllArgsConstructor
        CommentDto dto = CommentDto.builder()
                .id(1L)
                .text("Отличная вещь!")
                .authorName("Алексей")
                .created(createdTime)
                .itemId(10L)
                .build();

        // Тестируем геттеры
        assertEquals(1L, dto.getId());
        assertEquals("Отличная вещь!", dto.getText());
        assertEquals("Алексей", dto.getAuthorName());
        assertEquals(createdTime, dto.getCreated());
        assertEquals(10L, dto.getItemId());

        // Тестируем NoArgsConstructor и сеттеры
        CommentDto emptyDto = new CommentDto();
        emptyDto.setId(2L);
        emptyDto.setText("Норм");

        assertEquals(2L, emptyDto.getId());
        assertEquals("Норм", emptyDto.getText());

        // Тестируем toString, equals и hashCode
        assertNotNull(dto.toString());
        assertEquals(dto, dto);
        assertNotEquals(dto, emptyDto);
        assertEquals(dto.hashCode(), dto.hashCode());
    }
}