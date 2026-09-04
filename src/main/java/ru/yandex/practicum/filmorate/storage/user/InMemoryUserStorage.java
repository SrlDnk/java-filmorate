package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);
        if (friend.getFriends().containsKey(userId)) {
            user.getFriends().put(friendId, FriendshipStatus.CONFIRMED);
            friend.getFriends().put(userId, FriendshipStatus.CONFIRMED);
        } else {
            user.getFriends().put(friendId, FriendshipStatus.UNCONFIRMED);
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        users.get(userId).getFriends().remove(friendId);
        if (users.get(friendId).getFriends().containsKey(userId)) {
            users.get(friendId).getFriends().put(userId, FriendshipStatus.UNCONFIRMED);
        }
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        return users.get(userId).getFriends().keySet().stream()
                .map(users::get)
                .toList();
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        Set<Long> otherFriends = users.get(otherId).getFriends().keySet();
        return users.get(userId).getFriends().keySet().stream()
                .filter(otherFriends::contains)
                .map(users::get)
                .toList();
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
