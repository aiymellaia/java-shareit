package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingInputDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
class BookingInputDtoTest {

    @Autowired
    private JacksonTester<BookingInputDto> json;

    @Test
    void testBookingInputDtoSerialization() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 6, 1, 12, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 2, 12, 0, 0);

        BookingInputDto dto = new BookingInputDto();
        dto.setItemId(2L);
        dto.setStart(start);
        dto.setEnd(end);

        JsonContent<BookingInputDto> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.itemId", 2);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2026-06-01T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2026-06-02T12:00:00");
    }

    @Test
    void testBookingInputDtoDeserialization() throws Exception {
        String jsonContent = "{\"itemId\":3,\"start\":\"2026-07-10T15:30:00\",\"end\":\"2026-07-11T15:30:00\"}";

        BookingInputDto result = json.parse(jsonContent).getObject();

        assertEquals(3L, result.getItemId());
        assertEquals(LocalDateTime.of(2026, 7, 10, 15, 30, 0), result.getStart());
        assertEquals(LocalDateTime.of(2026, 7, 11, 15, 30, 0), result.getEnd());
    }
}