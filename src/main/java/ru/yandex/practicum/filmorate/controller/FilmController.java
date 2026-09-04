package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmStorage filmStorage;
    private final FilmService filmService;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmController(@Qualifier("filmDbStorage") FilmStorage filmStorage, FilmService filmService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Long id) {
        Film film = filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
        return film;
    }

    @GetMapping("/popular")
    public Collection<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopular(count);
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        validate(film);
        Film created = filmService.create(film);
        log.info("Фильм id = {}, name = {} успешно добавлен", created.getId(), created.getName());
        return created;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Ошибка валидации - id обновляемого фильма пуст");
            throw new ValidationException("Id фильма должен быть указан");
        }
        validate(newFilm);
        filmStorage.findById(newFilm.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id " + newFilm.getId() + " не найден"));
        log.info("Фильм id {}, name {} - был обновлен", newFilm.getId(), newFilm.getName());
        return filmService.update(newFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
        log.info("Пользователь с id {} поставил лайк фильму с id {}", userId, id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(id, userId);
        log.info("Пользователь с id {} убрал лайк с фильма с id {}", userId, id);
    }

    private void validate(Film film) {
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("Ошибка валидации - фильм не мог быть выпущен раньше 28 декабря 1895 года");
            throw new ValidationException("Фильм не мог выйти раньше 28.12.1895");
        }
    }
}
