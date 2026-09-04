package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, FilmRowMapper.class, UserDbStorage.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Test
    @DisplayName("Должно находить все фильмы")
    void shouldFindAll() {
        Film film = createFilm();

        Collection<Film> films = filmStorage.findAll();

        assertThat(films)
                .isNotEmpty()
                .anySatisfy(foundFilm -> assertThat(foundFilm.getId()).isEqualTo(film.getId()));
    }

    @Test
    @DisplayName("Должно создавать фильм")
    void shouldCreateFilm() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фильм про космос");
        film.setReleaseDate(LocalDate.of(2014, 10, 26));
        film.setDuration(169);
        film.setMpa(new Mpa(3, "PG-13"));

        LinkedHashSet<Genre> genres = new LinkedHashSet<>();
        genres.add(new Genre(2, "Драма"));
        film.setGenres(genres);

        Film createdFilm = filmStorage.create(film);

        assertThat(createdFilm.getId()).isNotNull();
        assertThat(createdFilm.getName()).isEqualTo("Интерстеллар");
        assertThat(createdFilm.getDescription()).isEqualTo("Фильм про космос");
        assertThat(createdFilm.getReleaseDate()).isEqualTo(LocalDate.of(2014, 10, 26));
        assertThat(createdFilm.getDuration()).isEqualTo(169);
        assertThat(createFilm().getMpa())
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 3)
                .hasFieldOrPropertyWithValue("name", "PG-13");
        assertThat(createdFilm.getGenres())
                .extracting(Genre::getId)
                .containsExactly(2);
    }

    @Test
    @DisplayName("Должно обновлять фильм")
    void shouldUpdateFilm() {
        Film film = createFilm();

        film.setName("Одиссея");
        film.setDescription("Фильм основанный на древнегреческой поэме");
        film.setReleaseDate(LocalDate.of(2026, 7, 17));
        film.setDuration(172);
        film.setMpa(new Mpa(4, "R"));

        LinkedHashSet<Genre> genres = new LinkedHashSet<>();
        genres.add(new Genre(2, "Драма"));
        genres.add(new Genre(6, "Боевик"));
        film.setGenres(genres);

        Film updatedFilm = filmStorage.update(film);

        assertThat(updatedFilm.getId()).isEqualTo(film.getId());
        assertThat(updatedFilm.getName()).isEqualTo("Одиссея");
        assertThat(updatedFilm.getDescription()).isEqualTo("Фильм основанный на древнегреческой поэме");
        assertThat(updatedFilm.getDuration()).isEqualTo(172);
        assertThat(updatedFilm.getMpa())
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 4);
        assertThat(updatedFilm.getGenres())
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(2, 6);
    }

    @Test
    @DisplayName("Должно находить фильм по идентификатору")
    void shouldFindById() {
        Film film = createFilm();

        Optional<Film> filmOptional = filmStorage.findById(film.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(foundFilm -> {
                    assertThat(foundFilm.getId()).isEqualTo(film.getId());
                    assertThat(foundFilm.getName()).isEqualTo("Интерстеллар");
                    assertThat(foundFilm.getMpa())
                            .isNotNull()
                            .hasFieldOrPropertyWithValue("id", 3);
                    assertThat(foundFilm.getGenres())
                            .extracting(Genre::getId)
                            .containsExactly(2);
                });
    }

    @Test
    @DisplayName("Не должно находить несуществующий фильм")
    void shouldNotFindById_whenFilmNotExist() {
        Optional<Film> filmOptional = filmStorage.findById(99999L);

        assertThat(filmOptional).isEmpty();
    }

    @Test
    @DisplayName("Должно добавлять лайк фильму")
    void shouldAddLikeToFilm() {
        Film film = createFilm();
        User user = createUser();

        filmStorage.addLike(film.getId(), user.getId());

        Collection<Film> popularFilms = filmStorage.getPopular(1);

        assertThat(popularFilms)
                .hasSize(1)
                .first()
                .satisfies(foundFilm -> assertThat(foundFilm.getId()).isEqualTo(film.getId()));
    }

    @Test
    @DisplayName("Должно удалять лайк у фильма")
    void shouldRemoveLikeFromFilm() {
        Film film = createFilm();

        User user = createUser();

        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.removeLike(film.getId(), user.getId());

        Collection<Film> popularFilms = filmStorage.getPopular(1);

        assertThat(popularFilms).hasSize(1);
    }

    @Test
    @DisplayName("Должно возвращать популярные фильмы")
    void shouldGetPopular() {
        Film film1 = createFilm();
        Film film2 = createFilm();

        User user1 = createUser();
        User user2 = createUser();

        filmStorage.addLike(film1.getId(), user1.getId());
        filmStorage.addLike(film1.getId(), user2.getId());
        filmStorage.addLike(film2.getId(), user1.getId());

        Collection<Film> popularFilms = filmStorage.getPopular(2);

        assertThat(popularFilms)
                .hasSize(2)
                .first()
                .satisfies(foundFilm -> assertThat(foundFilm.getId()).isEqualTo(film1.getId()));
    }

    private Film createFilm() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фильм про космос");
        film.setReleaseDate(LocalDate.of(2014, 10, 26));
        film.setDuration(169);
        film.setMpa(new Mpa(3, "PG-13"));

        LinkedHashSet<Genre> genres = new LinkedHashSet<>();
        genres.add(new Genre(2, "Драма"));
        film.setGenres(genres);

        return filmStorage.create(film);
    }

    private User createUser() {
        User user = new User();
        user.setEmail("rldk@yandex.ru");
        user.setLogin("rldk");
        user.setName("Danil");
        user.setBirthday(LocalDate.of(2002, 6, 5));

        return userStorage.create(user);
    }
}
