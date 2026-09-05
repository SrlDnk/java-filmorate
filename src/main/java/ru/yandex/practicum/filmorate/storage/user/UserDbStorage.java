package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Optional;

@Repository
public class UserDbStorage implements UserStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String CREATE_QUERY = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String UPDATE_QUERY =
            "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    private static final String CHECK_FRIENDSHIP_QUERY =
            "SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?";
    private static final String ADD_FRIEND_QUERY =
            "MERGE INTO friendship (user_id, friend_id, status) VALUES (?, ?, ?)";
    private static final String SET_STATUS_QUERY =
            "UPDATE friendship SET status = ? WHERE user_id = ? AND friend_id = ?";
    private static final String REMOVE_FRIEND_QUERY =
            "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_FRIENDS_QUERY =
            "SELECT u.* FROM users AS u " +
                    "JOIN friendship AS f ON u.id = f.friend_id " +
                    "WHERE f.user_id = ?";
    private static final String FIND_COMMON_FRIENDS_QUERY =
            "SELECT u.* FROM users AS u " +
                    "JOIN friendship AS f1 ON u.id = f1.friend_id " +
                    "JOIN friendship AS f2 ON u.id = f2.friend_id " +
                    "WHERE f1.user_id = ? AND f2.user_id = ?";

    private final JdbcTemplate jdbc;
    private final UserRowMapper mapper;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public Collection<User> findAll() {
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }

    @Override
    public User create(User user) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(CREATE_QUERY, new String[]{"id"});
            preparedStatement.setString(1, user.getEmail());
            preparedStatement.setString(2, user.getLogin());
            preparedStatement.setString(3, user.getName());
            preparedStatement.setDate(4, Date.valueOf(user.getBirthday()));
            return preparedStatement;
        }, keyHolder);
        user.setId(keyHolder.getKeyAs(Long.class));
        return user;
    }

    @Override
    public User update(User user) {
        jdbc.update(UPDATE_QUERY, user.getEmail(), user.getLogin(), user.getName(),
                Date.valueOf(user.getBirthday()), user.getId());
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        try {
            User user = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        Integer count = jdbc.queryForObject(CHECK_FRIENDSHIP_QUERY, Integer.class, friendId, userId);
        if (count != null && count == 1) {
            jdbc.update(ADD_FRIEND_QUERY, userId, friendId, FriendshipStatus.CONFIRMED.name());
            jdbc.update(SET_STATUS_QUERY, FriendshipStatus.CONFIRMED.name(), friendId, userId);
        } else {
            jdbc.update(ADD_FRIEND_QUERY, userId, friendId, FriendshipStatus.UNCONFIRMED.name());
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        jdbc.update(REMOVE_FRIEND_QUERY, userId, friendId);
        jdbc.update(SET_STATUS_QUERY, FriendshipStatus.UNCONFIRMED.name(), friendId, userId);
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        return jdbc.query(FIND_FRIENDS_QUERY, mapper, userId);
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        return jdbc.query(FIND_COMMON_FRIENDS_QUERY, mapper, userId, otherId);
    }
}
