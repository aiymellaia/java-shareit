package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void testItemRequestDtoSerialization() throws Exception {
        LocalDateTime created = LocalDateTime.of(2026, 5, 24, 10, 0, 0);
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужен перфоратор")
                .created(created)
                .items(Collections.emptyList())
                .build();

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id", 1);
        assertThat(result).hasJsonPathStringValue("$.description", "Нужен перфоратор");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2026-05-24T10:00:00");
        assertThat(result).hasJsonPathArrayValue("$.items");
    }

    @Test
    void testItemRequestDtoDeserialization() throws Exception {
        String jsonContent = "{\"id\":1,\"description\":\"Нужен перфоратор\",\"created\":\"2026-05-24T10:00:00\",\"items\":[]}";

        ItemRequestDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Нужен перфоратор");
        assertThat(result.getCreated()).isEqualTo(LocalDateTime.of(2026, 5, 24, 10, 0, 0));
        assertThat(result.getItems()).isEmpty();
    }
}