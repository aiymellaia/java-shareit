package ru.practicum.shareit;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.exception.ErrorHandler;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ErrorHandlerTest {

    private ErrorHandler errorHandler;

    @BeforeEach
    void setUp() {
        errorHandler = new ErrorHandler();
    }

    @Test
    void handleValidation_ShouldReturnBadRequestMap() {
        // Создаем моки для сложного исключения валидации Spring
        MethodArgumentNotValidException exception = Mockito.mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "must not be blank");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldError()).thenReturn(fieldError);

        // Вызываем обработчик
        Map<String, String> response = errorHandler.handleValidation(exception);

        // Проверяем результат
        assertNotNull(response);
        assertTrue(response.containsKey("error"));
        assertEquals("Ошибка валидации данных: must not be blank", response.get("error"));
    }

    @Test
    void handleConstraintViolationException_ShouldReturnBadRequestMap() {
        // Создаем реальное исключение валидации параметров
        ConstraintViolationException exception = new ConstraintViolationException("id: must be greater than 0", Collections.emptySet());

        // Вызываем обработчик
        Map<String, String> response = errorHandler.handleConstraintViolationException(exception);

        // Проверяем результат
        assertNotNull(response);
        assertTrue(response.containsKey("error"));
        assertEquals("id: must be greater than 0", response.get("error"));
    }

    @Test
    void handleThrowable_ShouldReturnInternalServerErrorMap() {
        // Создаем базовое непредвиденное исключение
        Throwable throwable = new RuntimeException("DB connection timeout");

        // Вызываем обработчик
        Map<String, String> response = errorHandler.handleThrowable(throwable);

        // Проверяем результат
        assertNotNull(response);
        assertTrue(response.containsKey("error"));
        assertEquals("Произошла непредвиденная ошибка: DB connection timeout", response.get("error"));
    }
}