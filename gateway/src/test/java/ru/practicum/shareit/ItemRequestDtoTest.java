package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestDtoTest {

    @Test
    void testItemRequestDto() {
        LocalDateTime createdTime = LocalDateTime.now();
        List<ItemDto> items = Collections.emptyList();

        // Тестируем Builder и AllArgsConstructor
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужна стремянка")
                .created(createdTime)
                .items(items)
                .build();

        // Тестируем геттеры
        assertEquals(1L, dto.getId());
        assertEquals("Нужна стремянка", dto.getDescription());
        assertEquals(createdTime, dto.getCreated());
        assertEquals(items, dto.getItems());

        // Тестируем NoArgsConstructor и сеттеры (@Data)
        ItemRequestDto emptyDto = new ItemRequestDto();
        emptyDto.setId(2L);
        emptyDto.setDescription("Нужен перфоратор");

        assertEquals(2L, emptyDto.getId());
        assertEquals("Нужен перфоратор", emptyDto.getDescription());

        // Тестируем toString, equals и hashCode для Lombok
        assertNotNull(dto.toString());
        assertEquals(dto, dto);
        assertNotEquals(dto, emptyDto);
        assertEquals(dto.hashCode(), dto.hashCode());
    }

    @Test
    void testItemRequestInputDto() {
        // Тестируем сеттер и геттер (@Data)
        ItemRequestInputDto inputDto = new ItemRequestInputDto();
        inputDto.setDescription("Ищу велосипед");

        assertEquals("Ищу велосипед", inputDto.getDescription());

        // Тестируем toString, equals и hashCode
        assertNotNull(inputDto.toString());
        assertEquals(inputDto, inputDto);
        assertEquals(inputDto.hashCode(), inputDto.hashCode());

        // Проверяем сравнение с другим объектом
        ItemRequestInputDto secondInputDto = new ItemRequestInputDto();
        secondInputDto.setDescription("Другое описание");
        assertNotEquals(inputDto, secondInputDto);
    }
}