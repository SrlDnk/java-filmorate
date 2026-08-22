package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        validate(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь id {} email {} создан успешно", user.getId(), user.getEmail());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        if (newUser.getId() == null) {
            log.warn("Ошибка валидации - id обновляемого пользователя пуст");
            throw new ValidationException("Id пользователя должен быть указан");
        }
        if (!users.containsKey(newUser.getId())) {
            log.warn("Ошибка валидации - пользователь с id {} не найден", newUser.getId());
            throw new ValidationException("Пользователь с id " + newUser.getId() + " не найден");
        }
        validate(newUser);
        users.put(newUser.getId(), newUser);
        log.info("Пользователь id {}, name {} - был обновлен", newUser.getId(), newUser.getName());
        return newUser;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void validate(User user) {
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Ошибка валидации - login заполнен некорректно");
            throw new ValidationException("Логин введен некорректно");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
