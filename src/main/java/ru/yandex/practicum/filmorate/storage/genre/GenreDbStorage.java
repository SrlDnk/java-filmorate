package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class GenreDbStorage implements GenreStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM genre ORDER BY id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genre WHERE id = ?";
    private static final String FIND_ALL_BY_IDS_QUERY = "SELECT * FROM genre WHERE id IN (%s)";

    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper;

    @Autowired
    public GenreDbStorage(JdbcTemplate jdbc, GenreRowMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public Collection<Genre> findAll() {
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }

    @Override
    public Optional<Genre> findById(int id) {
        try {
            Genre genre = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
            return Optional.ofNullable(genre);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Genre> findAllByIds(Collection<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        String joinedIds = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return jdbc.query(String.format(FIND_ALL_BY_IDS_QUERY, joinedIds), mapper);
    }
}
