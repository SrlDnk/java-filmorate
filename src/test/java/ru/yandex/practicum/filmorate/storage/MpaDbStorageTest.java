package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({MpaDbStorage.class, MpaRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaDbStorageTest {

    private final MpaDbStorage mpaStorage;

    @Test
    @DisplayName("Должно находить все рейтинги MPA")
    void shouldFindAll() {
        Collection<Mpa> mpaList = mpaStorage.findAll();

        assertThat(mpaList)
                .hasSize(5)
                .extracting(Mpa::getId)
                .containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    @DisplayName("Должно находить рейтинг MPA по идентификатору")
    void shouldFindById() {
        Optional<Mpa> mpaOptional = mpaStorage.findById(1);

        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(mpa -> {
                    assertThat(mpa.getId()).isEqualTo(1);
                    assertThat(mpa.getName()).isEqualTo("G");
                });
    }

    @Test
    @DisplayName("Не должно находить несуществующих рейтинг MPA")
    void shouldNotFindById_whenMpaNotExist() {
        Optional<Mpa> mpaOptional = mpaStorage.findById(999);

        assertThat(mpaOptional).isEmpty();
    }
}
