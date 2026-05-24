package ru.practicum.shareit;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplIntegrationTest {

    private final ItemService itemService;
    private final EntityManager em;

    @Test
    void getItemsByOwner_shouldReturnItemsWithCorrectData() {
        User owner = User.builder().name("Владелец").email("owner@mail.ru").build();
        em.persist(owner);

        Item item1 = Item.builder().name("Дрель").description("Ударная").available(true).owner(owner).build();
        Item item2 = Item.builder().name("Отвертка").description("Магнитная").available(true).owner(owner).build();
        em.persist(item1);
        em.persist(item2);

        em.flush();

        List<ItemDto> result = itemService.getItemsByOwner(owner.getId());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Дрель", result.get(0).getName());
        assertEquals("Отвертка", result.get(1).getName());
    }
}