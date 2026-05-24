package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.CommentDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoTest {

    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    void testCommentDtoSerialization() throws Exception {
        LocalDateTime created = LocalDateTime.of(2026, 5, 24, 20, 0);
        CommentDto dto = CommentDto.builder()
                .id(1L)
                .text("Отличный товар")
                .authorName("Алексей")
                .created(created)
                .itemId(10L)
                .build();

        JsonContent<CommentDto> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id", 1);
        assertThat(result).hasJsonPathStringValue("$.text", "Отличный товар");
        assertThat(result).hasJsonPathStringValue("$.authorName", "Алексей");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2026-05-24T20:00:00");
        assertThat(result).hasJsonPathNumberValue("$.itemId", 10);
    }
}