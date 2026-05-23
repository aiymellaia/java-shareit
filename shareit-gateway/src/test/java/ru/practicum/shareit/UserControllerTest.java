package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.client.UserClient;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @Test
    void findAll_thenReturns200() throws Exception {
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(userClient.getAllUsers()).thenReturn(mockResponse);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    void findById_thenReturns200() throws Exception {
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(userClient.getUserById(anyLong())).thenReturn(mockResponse);

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void create_whenValidUser_thenReturns200() throws Exception {
        UserDto validUser = UserDto.builder()
                .name("Котофей")
                .email("cat@mail.ru")
                .build();

        ResponseEntity<Object> mockResponse = new ResponseEntity<>("Success User JSON", HttpStatus.OK);
        when(userClient.createUser(any(UserDto.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk());
    }

    @Test
    void create_whenInvalidEmail_thenReturns400() throws Exception {
        UserDto invalidUser = UserDto.builder()
                .name("Котофей")
                .email("not-an-email") // Нарушаем @Email в DTO шлюза
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_whenBlankName_thenReturns400() throws Exception {
        UserDto invalidUser = UserDto.builder()
                .name("") // Нарушаем @NotBlank в DTO шлюза
                .email("cat@mail.ru")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_thenReturns200() throws Exception {
        UserDto updateDto = UserDto.builder()
                .name("Новое Имя")
                .build();

        ResponseEntity<Object> mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(userClient.updateUser(anyLong(), any(UserDto.class))).thenReturn(mockResponse);

        mockMvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_thenReturns200() throws Exception {
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(userClient.deleteUser(anyLong())).thenReturn(mockResponse);

        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isOk());
    }
}