package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestInputDtoTest {

    @Autowired
    private JacksonTester<ItemRequestInputDto> json;

    @Test
    void testItemRequestInputDtoSerialization() throws Exception {
        ItemRequestInputDto dto = new ItemRequestInputDto();
        dto.setDescription("Нужен шуруповерт");

        JsonContent<ItemRequestInputDto> result = json.write(dto);

        assertThat(result).hasJsonPathStringValue("$.description", "Нужен шуруповерт");
    }

    @Test
    void testItemRequestInputDtoDeserialization() throws Exception {
        String jsonContent = "{\"description\":\"Нужен шуруповерт\"}";

        ItemRequestInputDto result = json.parse(jsonContent).getObject();

        assertThat(result.getDescription()).isEqualTo("Нужен шуруповерт");
    }
}