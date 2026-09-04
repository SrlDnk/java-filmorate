package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Long userId, Long friendId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + friendId + " не найден"));
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + friendId + " не найден"));
        userStorage.removeFriend(userId, friendId);
    }

    public Collection<User> getFriends(Long userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        return userStorage.getFriends(userId);
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        userStorage.findById(otherId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + otherId + " не найден"));
        return userStorage.getCommonFriends(userId, otherId);
    }
}
