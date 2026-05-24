package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingShortDtoTest {

    @Autowired
    private JacksonTester<BookingShortDto> json;

    @Test
    void testBookingShortDtoSerialization() throws Exception {
        BookingShortDto dto = new BookingShortDto(1L, 2L);

        JsonContent<BookingShortDto> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id", 1);
        assertThat(result).hasJsonPathNumberValue("$.bookerId", 2);
    }

    @Test
    void testBookingShortDtoDeserialization() throws Exception {
        String jsonContent = "{\"id\":1,\"bookerId\":2}";
        BookingShortDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBookerId()).isEqualTo(2L);
    }
}