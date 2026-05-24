package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestServiceImpl;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    private User user;
    private ItemRequest itemRequest;
    private Item item;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Алексей")
                .email("alex@mail.ru")
                .build();

        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("Нужен перфоратор")
                .requestor(user)
                .created(LocalDateTime.now())
                .build();

        item = Item.builder()
                .id(1L)
                .name("Перфоратор Bosch")
                .description("Мощный, с бурами")
                .available(true)
                .owner(user)
                .request(itemRequest) // <-- ИСПРАВЛЕНО: передаем объект вместо Long
                .build();
    }

    @Test
    void create_whenUserExists_thenSavesRequest() {
        ItemRequestInputDto inputDto = new ItemRequestInputDto();
        inputDto.setDescription("Нужен перфоратор");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestDto result = requestService.create(1L, inputDto);

        assertNotNull(result);
        assertEquals("Нужен перфоратор", result.getDescription());
        assertTrue(result.getItems().isEmpty());
        verify(requestRepository, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void create_whenUserDoesNotExist_thenThrowsNotFoundException() {
        ItemRequestInputDto inputDto = new ItemRequestInputDto();
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.create(99L, inputDto));
        verify(requestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    void getOwnRequests_whenUserExists_thenReturnsRequestsWithItems() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findAllByRequestorIdOrderByCreatedDesc(1L)).thenReturn(List.of(itemRequest));
        when(itemRepository.findAllByRequestIdIn(List.of(1L))).thenReturn(List.of(item));

        List<ItemRequestDto> result = requestService.getOwnRequests(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Нужен перфоратор", result.get(0).getDescription());
        assertEquals(1, result.get(0).getItems().size());
        assertEquals("Перфоратор Bosch", result.get(0).getItems().get(0).getName());
    }

    @Test
    void getOwnRequests_whenUserDoesNotExist_thenThrowsNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> requestService.getOwnRequests(99L));
    }

    @Test
    void getAllRequests_whenValidPagination_thenReturnsPagedRequests() {
        ItemRequest secondRequest = ItemRequest.builder()
                .id(2L)
                .description("Нужна стремянка")
                .requestor(user)
                .created(LocalDateTime.now().minusDays(1))
                .build();

        when(userRepository.existsById(1L)).thenReturn(true);

        when(requestRepository.findAllByRequestorIdNot(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(itemRequest));

        when(itemRepository.findAllByRequestIdIn(any())).thenReturn(Collections.emptyList());

        List<ItemRequestDto> result = requestService.getAllRequests(1L, 0, 1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Нужен перфоратор", result.get(0).getDescription());
    }

    @Test
    void getAllRequests_whenFromExceedsSize_thenReturnsEmptyList() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findAllByRequestorIdNot(eq(1L), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        List<ItemRequestDto> result = requestService.getAllRequests(1L, 5, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getRequestById_whenRequestAndUserExist_thenReturnsRequest() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findAllByRequestId(1L)).thenReturn(List.of(item));

        ItemRequestDto result = requestService.getRequestById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Нужен перфоратор", result.getDescription());
        assertEquals(1, result.getItems().size());
    }

    @Test
    void getRequestById_whenRequestDoesNotExist_thenThrowsNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.getRequestById(1L, 99L));
    }
}