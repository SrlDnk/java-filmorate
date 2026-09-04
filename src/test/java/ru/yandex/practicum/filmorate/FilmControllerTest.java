package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {
    private FilmController controller;
    private UserStorage userStorage;

    @BeforeEach
    void setUp() {
        FilmStorage filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        FilmService filmService = new FilmService(filmStorage, userStorage, null, null);
        controller = new FilmController(filmStorage, filmService);
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фильм про космос");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);
        return film;
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("egrv28@mail.ru");
        user.setLogin("kkkk28");
        user.setName("Дмитрий");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }

    @DisplayName("Валидный фильм успешно создаётся")
    @Test
    void createFilm_whenValid_returnsFilmWithId() {
        Film film = createValidFilm();

        Film created = controller.create(film);

        assertNotNull(created.getId());
        assertEquals(1, controller.findAll().size());
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

    @DisplayName("Фильм с несуществующим id не должен обновляться")
    @Test
    void updateFilm_whenIdNotFound_throwsNotFoundException() {
        Film film = createValidFilm();
        controller.create(film);
        Film newFilm = createValidFilm();
        newFilm.setId(999L);

        assertThrows(NotFoundException.class, () -> controller.update(newFilm));
    }

    @DisplayName("Лайк должен добавлятся фильму")
    @Test
    void addLike_addsUserIdToLikes() {
        Film film = controller.create(createValidFilm());
        User user = userStorage.create(createValidUser());

        controller.addLike(film.getId(), user.getId());

        assertTrue(film.getLikes().contains(user.getId()));
    }

    @DisplayName("Популярные фильмы должны сортироваться по количеству лайков")
    @Test
    void getPopular_sortsByLikes() {
        controller.create(createValidFilm());
        Film otherFilm = createValidFilm();
        otherFilm.setName("Другой фильм");
        Film createdOtherFilm = controller.create(otherFilm);
        User user = userStorage.create(createValidUser());

        controller.addLike(createdOtherFilm.getId(), user.getId());

        List<Film> popular = new ArrayList<>(controller.getPopular(2));
        assertEquals(createdOtherFilm.getId(), popular.get(0).getId());
    }
}
