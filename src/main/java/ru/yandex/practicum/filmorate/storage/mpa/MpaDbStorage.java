package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;

import java.util.Collection;
import java.util.Optional;

@Repository
public class MpaDbStorage implements MpaStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM mpa ORDER BY id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM mpa WHERE id = ?";

    private final JdbcTemplate jdbc;
    private final MpaRowMapper mapper;

    @Autowired
    public MpaDbStorage(JdbcTemplate jdbc, MpaRowMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public Collection<Mpa> findAll() {
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }

    @Override
    public Optional<Mpa> findById(int id) {
        try {
            Mpa mpa = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
            return Optional.ofNullable(mpa);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
