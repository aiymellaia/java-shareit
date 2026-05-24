package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.UserMapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MappersServerTest {

    @Test
    void testCommentMapper() {
        User author = new User();
        author.setId(1L);
        author.setName("Author");
        author.setEmail("author@mail.ru");

        Item item = new Item();
        item.setId(2L);
        item.setName("Item");

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Text");
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        // Вызываем маппер комментариев
        CommentDto dto = CommentMapper.toCommentDto(comment);

        assertNotNull(dto);
        assertEquals("Text", dto.getText());
        assertEquals("Author", dto.getAuthorName());
        assertEquals(2L, dto.getItemId());
    }

    @Test
    void testItemMapper_WithRequest_And_WithoutRequest() {
        // Сценарий 1: У вещи НЕТ запроса (проверяем ветку else в тернарном операторе)
        Item itemWithoutRequest = new Item();
        itemWithoutRequest.setId(1L);
        itemWithoutRequest.setName("Дрель");
        itemWithoutRequest.setDescription("Обычная дрель");
        itemWithoutRequest.setAvailable(true);

        ItemDto dtoNoRequest = ItemMapper.toItemDto(itemWithoutRequest);
        assertNotNull(dtoNoRequest);
        assertNull(dtoNoRequest.getRequestId());

        // Сценарий 2: У вещи ЕСТЬ запрос (проверяем ветку true в тернарнике)
        ItemRequest request = new ItemRequest();
        request.setId(5L);

        Item itemWithRequest = new Item();
        itemWithRequest.setId(2L);
        itemWithRequest.setName("Стремянка");
        itemWithRequest.setDescription("Высокая");
        itemWithRequest.setAvailable(true);
        itemWithRequest.setRequest(request); // подставляем объект запроса

        ItemDto dtoWithRequest = ItemMapper.toItemDto(itemWithRequest);
        assertNotNull(dtoWithRequest);
        assertEquals(5L, dtoWithRequest.getRequestId());

        // Сценарий 3: Маппинг обратно из DTO в Model (с requestId != null)
        Item mappedItem = ItemMapper.toItem(dtoWithRequest);
        assertNotNull(mappedItem);
        assertEquals(2L, mappedItem.getId());
        assertNotNull(mappedItem.getRequest());
        assertEquals(5L, mappedItem.getRequest().getId());
    }

    @Test
    void testUserMapper() {
        User user = new User();
        user.setId(1L);
        user.setName("User");
        user.setEmail("user@mail.ru");

        // Map TO Dto
        UserDto dto = UserMapper.toUserDto(user);
        assertNotNull(dto);
        assertEquals("User", dto.getName());

        // Map FROM Dto
        User model = UserMapper.toUser(dto);
        assertNotNull(model);
        assertEquals("User", model.getName());
    }
}