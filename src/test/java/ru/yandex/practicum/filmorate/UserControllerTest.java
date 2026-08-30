package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {
    private UserController controller;

    @BeforeEach
    void setUp() {
        UserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        controller = new UserController(userStorage, userService);
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("egrv28@mail.ru");
        user.setLogin("kkkk28");
        user.setName("Дмитрий");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }

    @DisplayName("Валидный пользователь успешно создаётся")
    @Test
    void createUser_whenValid_returnsUserWithId() {
        User user = createValidUser();

        User created = controller.create(user);

        assertNotNull(created.getId());
        assertEquals(1, controller.findAll().size());
    }

    @DisplayName("Пользователь с пробелом в логине не должен создаваться")
    @Test
    void createUser_whenLoginContainsSpace_throwsValidationException() {
        User user = createValidUser();
        user.setLogin("ego rv");

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @DisplayName("Пользователь с пустым логином не должен создаваться")
    @Test
    void createUser_whenLoginIsBlank_throwsValidationException() {
        User user = createValidUser();
        user.setLogin("");

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @DisplayName("При пустом имени в качестве имени используется логин")
    @Test
    void createUser_whenNameIsBlank_usesLoginAsName() {
        User user = createValidUser();
        user.setName("");

        User created = controller.create(user);

        assertEquals("kkkk28", created.getName());
    }

    @DisplayName("При null имени в качестве имени используется логин")
    @Test
    void createUser_whenNameIsNull_usesLoginAsName() {
        User user = createValidUser();
        user.setName(null);

        User created = controller.create(user);

        assertEquals("kkkk28", created.getName());
    }

    @DisplayName("Пользователь с сегодняшней датой рождения создаётся")
    @Test
    void createUser_whenBirthdayIsToday_createsUser() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now());

        User created = controller.create(user);

        assertNotNull(created.getId());
    }

    @DisplayName("Пользователь с несуществующим id не должен обновляться")
    @Test
    void updateUser_whenIdNotFound_throwsNotFoundException() {
        User user = createValidUser();
        controller.create(user);
        User newUser = createValidUser();
        newUser.setId(999L);

        assertThrows(NotFoundException.class, () -> controller.update(newUser));
    }

    @DisplayName("Заявка в друзья должна быть неподтвержденной у отправителя")
    @Test
    void addFriend_whenNoRequestBack_createsUnconfirmed() {
        User user = controller.create(createValidUser());
        User friend = createValidUser();
        friend.setEmail("srl@mail.ru");
        friend.setLogin("srl");
        User createdFriend = controller.create(friend);

        controller.addFriend(user.getId(), createdFriend.getId());

        assertEquals(FriendshipStatus.UNCONFIRMED, user.getFriends().get(createdFriend.getId()));
        assertFalse(createdFriend.getFriends().containsKey(user.getId()));
    }

    @DisplayName("Заявка в друзья должна быть подтвержденной у обоих")
    @Test
    void addFriend_whenRequestBack_createsConfirmed() {
        User user = controller.create(createValidUser());
        User friend = createValidUser();
        friend.setEmail("srl@mail.ru");
        friend.setLogin("srl");
        User createdFriend = controller.create(friend);

        controller.addFriend(user.getId(), createdFriend.getId());
        controller.addFriend(createdFriend.getId(), user.getId());

        assertEquals(FriendshipStatus.CONFIRMED, user.getFriends().get(createdFriend.getId()));
        assertEquals(FriendshipStatus.CONFIRMED, createdFriend.getFriends().get(user.getId()));
    }

    @DisplayName("Удаление из друзей должно работать взаимно")
    @Test
    void removeFriend_removesBothWays() {
        User user = controller.create(createValidUser());
        User friend = createValidUser();
        friend.setEmail("22eg@mail.ru");
        friend.setLogin("222222");
        User createdFriend = controller.create(friend);

        controller.addFriend(user.getId(), createdFriend.getId());
        controller.removeFriend(user.getId(), createdFriend.getId());

        assertFalse(user.getFriends().containsKey(createdFriend.getId()));
        assertFalse(createdFriend.getFriends().containsKey(user.getId()));
    }

    @DisplayName("Общие друзья должны определятся корректно")
    @Test
    void getCommonFriends_returnsCommon() {
        User user = controller.create(createValidUser());

        User otherUser = createValidUser();
        otherUser.setEmail("other@other.com");
        otherUser.setLogin("other");
        User createdOtherUser = controller.create(otherUser);

        User commonUser = createValidUser();
        commonUser.setEmail("common@common.com");
        commonUser.setLogin("common");
        User createdCommonUser = controller.create(commonUser);

        controller.addFriend(user.getId(), createdCommonUser.getId());
        controller.addFriend(createdOtherUser.getId(), createdCommonUser.getId());

        Collection<User> common = controller.getCommonFriends(user.getId(), createdOtherUser.getId());

        assertEquals(1, common.size());
        assertTrue(common.contains(createdCommonUser));
    }
}