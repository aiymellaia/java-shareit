package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingServiceImpl;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User booker;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingInputDto inputDto;

    @BeforeEach
    void setUp() {
        booker = User.builder().id(1L).name("Аня").email("anya@mail.ru").build();
        owner = User.builder().id(2L).name("Борис").email("boris@mail.ru").build();

        item = Item.builder()
                .id(1L)
                .name("Дрель")
                .description("Ударная")
                .available(true)
                .owner(owner)
                .build();

        booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        inputDto = new BookingInputDto();
        inputDto.setItemId(1L);
        inputDto.setStart(LocalDateTime.now().plusDays(1));
        inputDto.setEnd(LocalDateTime.now().plusDays(2));
    }

    // --- ТЕСТЫ МЕТОДА CREATE ---

    @Test
    void create_whenValid_thenSavesBooking() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.create(1L, inputDto);

        assertNotNull(result);
        assertEquals(BookingStatus.WAITING, result.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void create_whenItemNotAvailable_thenThrowsValidationException() {
        item.setAvailable(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.create(1L, inputDto));
    }

    @Test
    void create_whenBookerIsOwner_thenThrowsNotFoundException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> bookingService.create(2L, inputDto));
    }

    @Test
    void create_whenEndBeforeStart_thenThrowsValidationException() {
        inputDto.setEnd(inputDto.getStart().minusHours(1));
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.create(1L, inputDto));
    }

    // --- ТЕСТЫ МЕТОДА APPROVE ---

    @Test
    void approve_whenApprovedTrue_thenStatusChangesToApproved() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.approve(2L, 1L, true);

        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    void approve_whenNotOwner_thenThrowsValidationException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () -> bookingService.approve(1L, 1L, true));
    }

    @Test
    void approve_whenStatusNotWaiting_thenThrowsValidationException() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () -> bookingService.approve(2L, 1L, true));
    }

    // --- ТЕСТЫ МЕТОДА GETBOOKINGBYID ---

    @Test
    void getBookingById_whenBookerOrOwner_thenReturnsBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.getBookingById(1L, 1L); // Запрашивает букер
        assertNotNull(result);

        result = bookingService.getBookingById(2L, 1L); // Запрашивает овнер
        assertNotNull(result);
    }

    @Test
    void getBookingById_whenStranger_thenThrowsNotFoundException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(99L, 1L));
    }

    // --- ТЕСТЫ МЕТОДОВ СЕМЕЙСТВА GETBOOKINGS (SWITCH-CASE) ---

    @Test
    void getBookingsByBooker_withDifferentStates() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(anyLong())).thenReturn(List.of(booking));
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking));

        // Проверим дефолтный кейс (ALL)
        List<BookingDto> resultAll = bookingService.getBookingsByBooker(1L, "ALL");
        assertEquals(1, resultAll.size());

        // Проверим кейс WAITING
        List<BookingDto> resultWaiting = bookingService.getBookingsByBooker(1L, "WAITING");
        assertEquals(1, resultWaiting.size());

        // Проверим, что вызываются нужные методы репозитория для CURRENT/PAST/FUTURE
        bookingService.getBookingsByBooker(1L, "CURRENT");
        verify(bookingRepository).findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any());

        bookingService.getBookingsByBooker(1L, "PAST");
        verify(bookingRepository).findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any());

        bookingService.getBookingsByBooker(1L, "FUTURE");
        verify(bookingRepository).findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any());
    }

    @Test
    void getBookingsByOwner_withDifferentStates() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(anyLong())).thenReturn(List.of(booking));

        List<BookingDto> resultAll = bookingService.getBookingsByOwner(2L, "ALL");
        assertEquals(1, resultAll.size());

        bookingService.getBookingsByOwner(2L, "CURRENT");
        verify(bookingRepository).findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any());

        bookingService.getBookingsByOwner(2L, "PAST");
        verify(bookingRepository).findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any());

        bookingService.getBookingsByOwner(2L, "FUTURE");
        verify(bookingRepository).findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any());
    }
}