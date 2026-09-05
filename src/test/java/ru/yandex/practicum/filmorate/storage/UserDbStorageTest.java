package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    @DisplayName("Должен находить всех пользователей")
    void shouldFindAll() {
        User user = createUser("rldk@yandex.ru", "rldk", "Danil");

        Collection<User> users = userStorage.findAll();

        assertThat(users)
                .isNotEmpty()
                .anySatisfy(foundUser ->
                        assertThat(foundUser.getId()).isEqualTo(user.getId())
                );
    }

    @Test
    @DisplayName("Должно создавать пользователя")
    void shouldCreateUser() {
        User user = new User();
        user.setEmail("rldk@yandex.ru");
        user.setLogin("rldddk");
        user.setName("Rllldddk");
        user.setBirthday(LocalDate.of(2003, 5, 10));

        User createdUser = userStorage.create(user);

        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo("rldk@yandex.ru");
        assertThat(createdUser.getLogin()).isEqualTo("rldddk");
        assertThat(createdUser.getName()).isEqualTo("Rllldddk");
        assertThat(createdUser.getBirthday()).isEqualTo(LocalDate.of(2003, 5, 10));
    }

    @Test
    @DisplayName("Должно обновлять пользователя")
    void shouldUpdateUser() {
        User user = createUser("nerldk@yandex.ru", "nerldk", "ddddk");

        user.setEmail("rldk@yandex.ru");
        user.setLogin("rldddk");
        user.setName("Rllldddk");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User updatedUser = userStorage.update(user);

        assertThat(updatedUser)
                .hasFieldOrPropertyWithValue("id", user.getId())
                .hasFieldOrPropertyWithValue("email", "rldk@yandex.ru")
                .hasFieldOrPropertyWithValue("login", "rldddk")
                .hasFieldOrPropertyWithValue("name", "Rllldddk")
                .hasFieldOrPropertyWithValue(
                        "birthday",
                        LocalDate.of(2000, 1, 1)
                );
    }

    @Test
    @DisplayName("Должно находить пользователя по идентификатору")
    void shouldFindByIdUser() {
        User user = createUser("rldk@yandex.ru", "rldk", "Danil");

        Optional<User> userOptional = userStorage.findById(user.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(foundUser -> {
                    assertThat(foundUser)
                            .hasFieldOrPropertyWithValue(
                                    "id",
                                    user.getId()
                            );
                    assertThat(foundUser.getEmail())
                            .isEqualTo("rldk@yandex.ru");
                    assertThat(foundUser.getLogin())
                            .isEqualTo("rldk");
                });
    }

    @Test
    @DisplayName("Не должно находить несуществующего пользователя")
    void shouldNotFindById_whenUserNotExist() {
        Optional<User> userOptional = userStorage.findById(99999L);

        assertThat(userOptional).isEmpty();
    }

    @Test
    @DisplayName("Должно добавлять пользователя в друзья")
    void shouldAddFriends() {
        User user = createUser("rldk@yandex.ru", "rldk", "Danil");
        User friend = createUser("nerldk@yandex.ru", "nerldk", "ddddk");

        userStorage.addFriend(user.getId(), friend.getId());

        Collection<User> friends = userStorage.getFriends(user.getId());

        assertThat(friends)
                .extracting(User::getId)
                .containsExactly(friend.getId());
    }

    @Test
    @DisplayName("Должно подтверждать дружбу при взаимном добавлении")
    void shouldAddFriendMutual() {
        User user = createUser("rldk@yandex.ru", "rldk", "Danil");
        User friend = createUser("nerldk@yandex.ru", "nerldk", "ddddk");

        userStorage.addFriend(user.getId(), friend.getId());
        userStorage.addFriend(friend.getId(), user.getId());

        Collection<User> userFriends = userStorage.getFriends(user.getId());

        Collection<User> friendFriends = userStorage.getFriends(friend.getId());

        assertThat(userFriends)
                .extracting(User::getId)
                .containsExactly(friend.getId());

        assertThat(friendFriends)
                .extracting(User::getId)
                .containsExactly(user.getId());
    }

    @Test
    @DisplayName("Должно удалять пользователя из друзей")
    void shouldRemoveFriend() {
        User user = createUser("rldk@yandex.ru", "rldk", "Danil");
        User friend = createUser("nerldk@yandex.ru", "nerldk", "ddddk");

        userStorage.addFriend(user.getId(), friend.getId());
        userStorage.removeFriend(user.getId(), friend.getId());


        Collection<User> friends = userStorage.getFriends(user.getId());

        assertThat(friends)
                .extracting(User::getId)
                .doesNotContain(friend.getId());
    }

    @Test
    @DisplayName("Должно возвращать список друзей")
    void shouldGetFriends() {
        User user = createUser("rldk@yandex.ru", "rldk", "Danil");
        User friend = createUser("nerldk@yandex.ru", "nerldk", "ddddk");
        User anotherFriend = createUser("nrldk@yandex.ru", "nldk", "vdk");


        userStorage.addFriend(user.getId(), friend.getId());
        userStorage.addFriend(user.getId(), anotherFriend.getId());

        Collection<User> friends = userStorage.getFriends(user.getId());

        assertThat(friends)
                .extracting(User::getId)
                .containsExactlyInAnyOrder(friend.getId(), anotherFriend.getId());
    }

    @Test
    @DisplayName("Должно находить общих друзей")
    void shouldGetCommonFriends() {
        User user = createUser("rldk@yandex.ru", "rldk", "Danil");
        User friend = createUser("nerldk@yandex.ru", "nerldk", "ddddk");
        User anotherFriend = createUser("nrldk@yandex.ru", "nldk", "vdk");

        userStorage.addFriend(user.getId(), anotherFriend.getId());
        userStorage.addFriend(friend.getId(), anotherFriend.getId());

        Collection<User> commonFriends = userStorage.getCommonFriends(user.getId(), friend.getId());

        assertThat(commonFriends)
                .extracting(User::getId)
                .containsExactly(anotherFriend.getId());
    }


    private User createUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(2002, 6, 5));

        return userStorage.create(user);
    }
}
