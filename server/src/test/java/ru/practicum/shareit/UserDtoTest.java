package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void testUserDtoSerialization() throws Exception {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("Алексей")
                .email("alex@mail.ru")
                .build();

        JsonContent<UserDto> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id", 1);
        assertThat(result).hasJsonPathStringValue("$.name", "Алексей");
        assertThat(result).hasJsonPathStringValue("$.email", "alex@mail.ru");
    }

    @Test
    void testUserDtoDeserialization() throws Exception {
        String jsonContent = "{\"id\":1,\"name\":\"Алексей\",\"email\":\"alex@mail.ru\"}";

        UserDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Алексей");
        assertThat(result.getEmail()).isEqualTo("alex@mail.ru");
    }
}