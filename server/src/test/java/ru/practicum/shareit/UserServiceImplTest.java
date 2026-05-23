package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Дмитрий")
                .email("dima@mail.ru")
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .name("Дмитрий")
                .email("dima@mail.ru")
                .build();
    }

    @Test
    void getAllUsers_whenUsersExist_thenReturnsList() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(user.getName(), result.getFirst().getName());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getAllUsers_whenEmpty_thenReturnsEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void createUser_whenValid_thenSavesUser() {
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto created = userService.createUser(userDto);

        assertNotNull(created);
        assertEquals(user.getEmail(), created.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_whenEmailExists_thenThrowsConflictException() {
        when(userRepository.existsByEmailIgnoreCase(userDto.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.createUser(userDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_whenUserExists_thenReturnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto found = userService.getUserById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
        assertEquals("Дмитрий", found.getName());
    }

    @Test
    void getUserById_whenUserDoesNotExist_thenThrowsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    void updateUser_whenUserExists_thenUpdatesFields() {
        UserDto updateDto = UserDto.builder()
                .name("Дима Новый")
                .email("newdima@mail.ru")
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .name("Дима Новый")
                .email("newdima@mail.ru")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailIgnoreCase(updateDto.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDto result = userService.updateUser(1L, updateDto);

        assertNotNull(result);
        assertEquals("Дима Новый", result.getName());
        assertEquals("newdima@mail.ru", result.getEmail());
    }

    @Test
    void updateUser_whenEmailAlreadyTaken_thenThrowsConflictException() {
        UserDto updateDto = UserDto.builder()
                .email("occupied@mail.ru")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailIgnoreCase(updateDto.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.updateUser(1L, updateDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_whenUserExists_thenDeletesUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_whenUserDoesNotExist_thenThrowsNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.deleteUser(99L));
        verify(userRepository, never()).deleteById(anyLong());
    }
}