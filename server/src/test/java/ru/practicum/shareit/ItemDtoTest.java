package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    void testItemDtoSerialization() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Молоток")
                .description("Стальной молоток")
                .available(true)
                .requestId(10L)
                .comments(Collections.emptyList())
                .build();

        JsonContent<ItemDto> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id", 1);
        assertThat(result).hasJsonPathStringValue("$.name", "Молоток");
        assertThat(result).hasJsonPathStringValue("$.description", "Стальной молоток");
        assertThat(result).hasJsonPathBooleanValue("$.available", true);
        assertThat(result).hasJsonPathNumberValue("$.requestId", 10);
        assertThat(result).hasJsonPathArrayValue("$.comments");
    }

    @Test
    void testItemDtoDeserialization() throws Exception {
        String jsonContent = "{\"id\":1,\"name\":\"Молоток\",\"description\":\"Стальной молоток\",\"available\":true,\"requestId\":10}";

        ItemDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Молоток");
        assertThat(result.getDescription()).isEqualTo("Стальной молоток");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getRequestId()).isEqualTo(10L);
    }
}