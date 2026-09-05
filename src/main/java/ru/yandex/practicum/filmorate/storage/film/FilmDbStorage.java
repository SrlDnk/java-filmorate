package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.*;

@Repository
public class FilmDbStorage implements FilmStorage {
    private static final String FIND_ALL_QUERY =
            "SELECT f.*, m.name AS mpa_name FROM film AS f LEFT JOIN mpa AS m ON m.id = f.mpa_id";
    private static final String CREATE_QUERY =
            "INSERT INTO film(name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY =
            "UPDATE film SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
    private static final String ADD_GENRE_QUERY =
            "MERGE INTO film_genre (film_id, genre_id) VALUES (?, ?)";
    private static final String FIND_ALL_GENRES_QUERY =
            "SELECT fg.film_id, g.id, g.name FROM film_genre AS fg " +
                    "JOIN genre AS g ON g.id = fg.genre_id ORDER BY g.id";
    private static final String FIND_BY_ID_QUERY = FIND_ALL_QUERY + " WHERE f.id = ?";
    private static final String DELETE_GENRES_QUERY =
            "DELETE FROM film_genre WHERE film_id = ?";
    private static final String ADD_LIKE_QUERY =
            "MERGE INTO film_like (film_id, user_id) VALUES (?, ?)";
    private static final String REMOVE_LIKE_QUERY =
            "DELETE FROM film_like WHERE film_id = ? AND user_id = ?";
    private static final String POPULAR_QUERY =
            "SELECT f.*, m.name AS mpa_name, COUNT(fl.user_id) AS likes_count " +
                    "FROM film AS f " +
                    "LEFT JOIN mpa AS m ON m.id = f.mpa_id " +
                    "LEFT JOIN film_like AS fl ON fl.film_id = f.id " +
                    "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name " +
                    "ORDER BY likes_count DESC LIMIT ?";

    private final JdbcTemplate jdbc;
    private final FilmRowMapper mapper;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbc.query(FIND_ALL_QUERY, mapper);
        loadGenres(films);
        return films;
    }

    @Override
    public Film create(Film film) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_QUERY, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            if (film.getMpa() != null) {
                ps.setInt(5, film.getMpa().getId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            return ps;
        }, keyHolder);
        film.setId(keyHolder.getKeyAs(Long.class));
        saveGenres(film);
        return findById(film.getId()).orElseThrow();
    }

    @Override
    public Film update(Film film) {
        jdbc.update(UPDATE_QUERY, film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()),
                film.getDuration(), getMpaId(film), film.getId());
        jdbc.update(DELETE_GENRES_QUERY, film.getId());
        saveGenres(film);
        return findById(film.getId()).orElseThrow();
    }

    @Override
    public Optional<Film> findById(Long id) {
        try {
            Film film = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
            loadGenres(List.of(film));
            return Optional.of(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbc.update(ADD_LIKE_QUERY, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbc.update(REMOVE_LIKE_QUERY, filmId, userId);
    }

    @Override
    public Collection<Film> getPopular(int count) {
        List<Film> films = jdbc.query(POPULAR_QUERY, mapper, count);
        loadGenres(films);
        return films;
    }

    private void loadGenres(Collection<Film> films) {
        Map<Long, Film> filmsById = new HashMap<>();
        for (Film film : films) {
            filmsById.put(film.getId(), film);
        }
        RowCallbackHandler handler = rs -> {
            Film film = filmsById.get(rs.getLong("film_id"));
            if (film != null) {
                film.getGenres().add(new Genre(rs.getInt("id"), rs.getString("name")));
            }
        };
        jdbc.query(FIND_ALL_GENRES_QUERY, handler);
    }

    private Integer getMpaId(Film film) {
        if (film.getMpa() == null) {
            return null;
        }
        return film.getMpa().getId();
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        List<Genre> genres = new ArrayList<>(film.getGenres());
        jdbc.batchUpdate(ADD_GENRE_QUERY, genres, genres.size(),
                (ps, genre) -> {
                    ps.setLong(1, film.getId());
                    ps.setInt(2, genre.getId());
                });
    }
}
