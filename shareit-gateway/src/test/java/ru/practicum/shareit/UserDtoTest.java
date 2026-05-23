package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.*;

class UserDtoTest {

    @Test
    void testUserDto() {
        // Тестируем Builder
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("Айнур")
                .email("ainur@yandex.kz")
                .build();

        // Тестируем геттеры
        assertEquals(1L, dto.getId());
        assertEquals("Айнур", dto.getName());
        assertEquals("ainur@yandex.kz", dto.getEmail());

        // Тестируем сеттеры (@Data)
        UserDto secondDto = UserDto.builder().build();
        secondDto.setId(2L);
        secondDto.setName("Иван");
        secondDto.setEmail("ivan@yandex.ru");

        assertEquals(2L, secondDto.getId());
        assertEquals("Иван", secondDto.getName());
        assertEquals("ivan@yandex.ru", secondDto.getEmail());

        // Тестируем toString, equals и hashCode для Lombok
        assertNotNull(dto.toString());
        assertEquals(dto, dto);
        assertNotEquals(dto, secondDto);
        assertEquals(dto.hashCode(), dto.hashCode());
    }
}