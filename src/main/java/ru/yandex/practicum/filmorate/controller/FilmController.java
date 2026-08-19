package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        validate(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм id = {}, name = {} успешно добавлен", film.getId(), film.getName());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Ошибка валидации - id обновляемого фильма пуст");
            throw new ValidationException("Id фильма должен быть указан");
        }
        if (!films.containsKey(newFilm.getId())) {
            log.warn("Ошибка валидации - фильм с id {} не найден", newFilm.getId());
            throw new ValidationException("Фильм с id " + newFilm.getId() + " не найден");
        }
        validate(newFilm);
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм id {}, name {} - был обновлен", newFilm.getId(), newFilm.getName());
        return newFilm;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void validate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Ошибка валидации - пустое название фильма");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.warn("Ошибка валидации - длина описания больше 200 символов");
            throw new ValidationException("Длина описания не может превышать 200 символов");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("Ошибка валидации - фильм не мог быть выпущен раньше 28 декабря 1895 года");
            throw new ValidationException("Фильм не мог выйти раньше 28.12.1895");
        }
        if (film.getDuration() <= 0) {
            log.warn("Ошибка валидации - фильм не может идти меньше минуты");
            throw new ValidationException("Фильм не может идти меньше одной минуты");
        }
    }
}
