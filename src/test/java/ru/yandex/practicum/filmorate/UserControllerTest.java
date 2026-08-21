package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {
    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController();
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("egrv28@mail.ru");
        user.setLogin("kkkk28");
        user.setName("Дмитрий");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }

    @DisplayName("Валидный пользователь успешно создаётся")
    @Test
    void createUser_whenValid_returnsUserWithId() {
        User user = createValidUser();

        User created = controller.create(user);

        assertNotNull(created.getId());
        assertEquals(1, controller.findAll().size());
    }

    @DisplayName("Пользователь с пробелом в логине не должен создаваться")
    @Test
    void createUser_whenLoginContainsSpace_throwsValidationException() {
        User user = createValidUser();
        user.setLogin("ego rv");

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @DisplayName("Пользователь с пустым логином не должен создаваться")
    @Test
    void createUser_whenLoginIsBlank_throwsValidationException() {
        User user = createValidUser();
        user.setLogin("");

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @DisplayName("При пустом имени в качестве имени используется логин")
    @Test
    void createUser_whenNameIsBlank_usesLoginAsName() {
        User user = createValidUser();
        user.setName("");

        User created = controller.create(user);

        assertEquals("kkkk28", created.getName());
    }

    @DisplayName("При null имени в качестве имени используется логин")
    @Test
    void createUser_whenNameIsNull_usesLoginAsName() {
        User user = createValidUser();
        user.setName(null);

        User created = controller.create(user);

        assertEquals("kkkk28", created.getName());
    }

    @DisplayName("Пользователь с сегодняшней датой рождения создаётся")
    @Test
    void createUser_whenBirthdayIsToday_createsUser() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now());

        User created = controller.create(user);

        assertNotNull(created.getId());
    }

    @DisplayName("Пользователь с несуществующим id не должен обновляться")
    @Test
    void updateUser_whenIdNotFound_throwsValidationException() {
        User user = createValidUser();
        controller.create(user);
        User newUser = createValidUser();
        newUser.setId(999L);

        assertThrows(ValidationException.class, () -> controller.update(newUser));
    }
}