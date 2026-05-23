package ru.practicum.shareit;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplIntegrationTest {

    private final BookingService bookingService;
    private final EntityManager em;

    @Test
    void create_shouldSaveBookingInDatabaseCorrectly() {
        User owner = User.builder().name("Владелец").email("owner@mail.com").build();
        User booker = User.builder().name("Арендатор").email("booker@mail.com").build();
        em.persist(owner);
        em.persist(booker);

        Item item = Item.builder().name("Дрель").description("Ударная").available(true).owner(owner).build();
        em.persist(item);
        em.flush();

        BookingInputDto inputDto = new BookingInputDto();
        inputDto.setItemId(item.getId());
        inputDto.setStart(LocalDateTime.now().plusDays(1));
        inputDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingDto result = bookingService.create(booker.getId(), inputDto);

        assertNotNull(result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
        assertEquals("Дрель", result.getItem().getName());
        assertEquals("Арендатор", result.getBooker().getName());
    }

    @Test
    void getBookingsByOwner_shouldReturnCorrectBookingsWithStateFuture() {
        User owner = User.builder().name("Хозяин").email("host@mail.com").build();
        User booker = User.builder().name("Клиент").email("client@mail.com").build();
        em.persist(owner);
        em.persist(booker);

        Item item = Item.builder().name("Перфоратор").description("Мощный").available(true).owner(owner).build();
        em.persist(item);

        Booking futureBooking = Booking.builder()
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(4))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();

        Booking pastBooking = Booking.builder()
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(3))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();

        em.persist(futureBooking);
        em.persist(pastBooking);
        em.flush();

        List<BookingDto> result = bookingService.getBookingsByOwner(owner.getId(), "FUTURE");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(futureBooking.getId(), result.get(0).getId());
        assertTrue(result.get(0).getStart().isAfter(LocalDateTime.now()));
    }
}