package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User user;
    private User stranger;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Влад").email("vlad@mail.ru").build();
        stranger = User.builder().id(2L).name("Иван").email("ivan@mail.ru").build();

        item = Item.builder()
                .id(1L)
                .name("Молоток")
                .description("Стальной молоток")
                .available(true)
                .owner(user)
                .build();

        itemDto = ItemDto.builder()
                .name("Молоток")
                .description("Стальной молоток")
                .available(true)
                .build();
    }

    @Test
    void create_whenUserExists_thenSavesItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.create(1L, itemDto);

        assertNotNull(result);
        assertEquals("Молоток", result.getName());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void update_whenValidOwner_thenUpdatesFields() {
        ItemDto updateDto = ItemDto.builder().name("Супер Молоток").build();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.update(1L, 1L, updateDto);

        assertNotNull(result);
        assertEquals("Супер Молоток", item.getName());
    }

    @Test
    void update_whenNotOwner_thenThrowsNotFoundException() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> itemService.update(2L, 1L, itemDto));
    }

    @Test
    void getItemById_whenUserIsOwner_thenReturnsItemWithBookings() {
        Booking lastBooking = Booking.builder()
                .id(1L).start(LocalDateTime.now().minusDays(2)).end(LocalDateTime.now().minusDays(1))
                .status(BookingStatus.APPROVED).item(item).booker(stranger).build();

        Booking nextBooking = Booking.builder()
                .id(2L).start(LocalDateTime.now().plusDays(1)).end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.APPROVED).item(item).booker(stranger).build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(1L)).thenReturn(Collections.emptyList());
        when(bookingRepository.findAllByItemIdOrderByStartDesc(1L)).thenReturn(List.of(nextBooking, lastBooking));

        ItemDto result = itemService.getItemById(1L, 1L); // 1L — владелец

        assertNotNull(result);
        assertNotNull(result.getLastBooking());
        assertNotNull(result.getNextBooking());
        assertEquals(1L, result.getLastBooking().getId());
        assertEquals(2L, result.getNextBooking().getId());
    }

    @Test
    void getItemsByOwner_thenReturnsItemsList() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findAllByOwnerId(1L)).thenReturn(List.of(item));
        when(commentRepository.findAllByItemIdIn(any())).thenReturn(Collections.emptyList());
        when(bookingRepository.findAllByItemIdInOrderByStartDesc(any())).thenReturn(Collections.emptyList());

        List<ItemDto> result = itemService.getItemsByOwner(1L);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void search_whenTextIsEmpty_thenReturnsEmptyList() {
        List<ItemDto> result = itemService.search("");
        assertTrue(result.isEmpty());
        verify(itemRepository, never()).search(anyString());
    }

    @Test
    void addComment_whenUserHasNoApprovedBookings_thenThrowsValidationException() {
        CommentDto commentDto = CommentDto.builder().text("Норм").build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(stranger));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any()))
                .thenReturn(Collections.emptyList());

        assertThrows(ValidationException.class, () -> itemService.addComment(2L, 1L, commentDto));
    }

    @Test
    void addComment_whenValid_thenReturnsCommentDto() {
        CommentDto commentDto = CommentDto.builder().text("Отличная вещь").build();
        Booking booking = Booking.builder()
                .id(1L).status(BookingStatus.APPROVED).item(item).booker(stranger).build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(stranger));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of(booking));

        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(1L);
            c.setCreated(LocalDateTime.now());
            return c;
        });

        CommentDto result = itemService.addComment(2L, 1L, commentDto);

        assertNotNull(result);
        assertEquals("Отличная вещь", result.getText());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void addComment_whenTextIsBlank_thenThrowsValidationException() {
        CommentDto emptyComment = CommentDto.builder().text("").build();
        assertThrows(ValidationException.class, () -> itemService.addComment(1L, 1L, emptyComment));
    }

    @Test
    void search_whenTextIsValid_thenReturnsList() {
        when(itemRepository.search("молоток")).thenReturn(List.of(item));

        List<ItemDto> result = itemService.search("молоток");

        assertEquals(1, result.size());
        assertEquals("Молоток", result.get(0).getName());
    }

    @Test
    void getItemById_whenUserIsNotOwner_thenReturnsItemWithoutBookings() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(1L)).thenReturn(Collections.emptyList());

        ItemDto result = itemService.getItemById(1L, 99L);

        assertNotNull(result);
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }
}