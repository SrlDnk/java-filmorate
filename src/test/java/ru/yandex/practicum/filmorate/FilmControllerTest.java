package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {
    private FilmController controller;

    @BeforeEach
    void setUp() {
        controller = new FilmController();
    }
    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фильм про космос");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);
        return film;
    }

    @DisplayName("Валидный фильм успешно создаётся")
    @Test
    void createFilm_whenValid_returnsFilmWithId() {
        Film film = createValidFilm();

        Film created = controller.create(film);

        assertNotNull(created.getId());
        assertEquals(1, controller.findAll().size());
    }

    @DisplayName("Фильм с пустым названием не должен создаваться")
    @Test
    void createFilm_whenNameIsBlank_throwsValidationException() {
        Film film = createValidFilm();
        film.setName("");

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @DisplayName("Фильм с описанием 200 должен создаётся")
    @Test
    void createFilm_whenDescriptionIs200Chars_createsFilm() {
        Film film = createValidFilm();
        String longDescription = "a".repeat(200);
        film.setDescription(longDescription);

        Film created = controller.create(film);

        assertNotNull(created.getId());
        assertEquals(1, controller.findAll().size());
    }

    @DisplayName("Фильм с описанием больше 200 не должен создаваться")
    @Test
    void createFilm_whenDescriptionIsMoreThan200Chars_throwsValidationException() {
        Film film = createValidFilm();
        String longDescription = "a".repeat(201);
        film.setDescription(longDescription);

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @DisplayName("Фильм с датой релиза 28.12.1895 создаётся")
    @Test
    void createFilm_whenReleaseDateIsMinimal_createsFilm() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));

        Film created = controller.create(film);

        assertNotNull(created.getId());
        assertEquals(1, controller.findAll().size());
    }

    @DisplayName("Фильм с датой релиза 27.12.1895 не должен создаваться")
    @Test
    void createFilm_whenReleaseDateIsBeforeMinimal_throwsValidationException() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @DisplayName("Фильм с продолжительностью 0 не должен создваться")
    @Test
    void createFilm_whenDurationIsZero_throwsValidationException() {
        Film film = createValidFilm();
        film.setDuration(0);

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @DisplayName("Фильм с отрицательной продолжительностью не должен создаваться")
    @Test
    void createFilm_whenDurationIsNegative_throwsValidationException() {
        Film film = createValidFilm();
        film.setDuration(-10);

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @DisplayName("Фильм с несуществующим id не должен обновляться")
    @Test
    void updateFilm_whenIdNotFound_throwsValidationException() {
        Film film = createValidFilm();
        controller.create(film);
        Film newFilm = createValidFilm();
        newFilm.setId(999L);

        assertThrows(ValidationException.class, () -> controller.update(newFilm));
    }
}
