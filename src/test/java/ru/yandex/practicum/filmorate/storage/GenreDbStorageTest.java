package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({GenreDbStorage.class, GenreRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreDbStorageTest {

    private final GenreDbStorage genreStorage;

    @Test
    @DisplayName("Должно находить все жанры")
    void shouldFindAll() {
        Collection<Genre> genres = genreStorage.findAll();

        assertThat(genres)
                .hasSize(6)
                .extracting(Genre::getId)
                .containsExactly(1, 2, 3, 4, 5, 6);
    }

    @Test
    @DisplayName("Должно находить жанр по идентификатору")
    void shouldFindById() {
        Optional<Genre> genreOptional = genreStorage.findById(1);

        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre -> {
                    assertThat(genre.getId()).isEqualTo(1);
                    assertThat(genre.getName()).isEqualTo("Комедия");
                });
    }

    @Test
    @DisplayName("Не должно находить несуществующий класс")
    void shouldNotFindById_whenGenreNotExist() {
        Optional<Genre> genreOptional = genreStorage.findById(999);

        assertThat(genreOptional).isEmpty();
    }
}
