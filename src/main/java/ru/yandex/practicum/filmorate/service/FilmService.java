package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       MpaStorage mpaStorage, GenreStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public Film create(Film film) {
        validateMpaAndGenres(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        validateMpaAndGenres(film);
        return filmStorage.update(film);
    }

    public void addLike(Long filmId, Long userId) {
        filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден"));
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден"));
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        filmStorage.removeLike(filmId, userId);
    }

    public Collection<Film> getPopular(int count) {
        return filmStorage.getPopular(count);
    }

    private void validateMpaAndGenres(Film film) {
        if (film.getMpa() != null) {
            mpaStorage.findById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("Рейтинг с id " + film.getMpa().getId() + " не найден"));
        }
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> requestedIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            Set<Integer> foundIds = genreStorage.findAllByIds(requestedIds).stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            requestedIds.removeAll(foundIds);
            if (!requestedIds.isEmpty()) {
                throw new NotFoundException("Жанр с id " + requestedIds.iterator().next() + " не найден");
            }
        }
    }
}
