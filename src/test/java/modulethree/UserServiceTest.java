package modulethree;

import jakarta.validation.ConstraintViolationException;
import modulethree.dao.UserDao;
import modulethree.model.User;
import modulethree.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    private User createUserWithId(Long id) {
        User user = new User();
        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID via reflection", e);
        }
        return user;
    }

    @Test
    void createUser_Success() {
        User newUser = new User();
        newUser.setEmail("test@example.com");
        newUser.setName("John Doe");

        userService.createUser(newUser);

        verify(userDao).create(newUser);
        assertNull(newUser.getId(), "ID should be generated during persistence");
    }

    @Test
    void createUser_ThrowsWhenEmailExists() {
        User existingUser = new User();
        existingUser.setEmail("exists@example.com");
        existingUser.setName("John");
        existingUser.setAge(30);

        doThrow(new IllegalArgumentException("Email already exists"))
                .when(userDao).create(existingUser);

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(existingUser));
    }

    @Test
    void getUserById_Found() {
        User expected = createUserWithId(1L);
        when(userDao.read(1L)).thenReturn(Optional.of(expected));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void getUserById_NotFound() {
        when(userDao.read(999L)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllUsers_Success() {
        User user1 = createUserWithId(1L);
        User user2 = createUserWithId(2L);
        when(userDao.readAll()).thenReturn(List.of(user1, user2));

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
    }

    @Test
    void deleteUser_Success() {
        when(userDao.delete(1L)).thenReturn(true);

        boolean result = userService.deleteUser(1L);

        assertTrue(result);
        verify(userDao).delete(1L);
    }

    @Test
    void deleteUser_NotFound() {
        when(userDao.delete(999L)).thenReturn(false);

        boolean result = userService.deleteUser(999L);

        assertFalse(result);
        verify(userDao).delete(999L);
    }

    @Test
    void deleteUser_WhenIdIsNull() {
        doThrow(new IllegalArgumentException("Invalid ID"))
                .when(userDao).delete(null);
        assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser(null));
    }

    @Test
    void deleteUser_WhenIdIsNegative() {
        doThrow(new IllegalArgumentException("Invalid ID"))
                .when(userDao).delete(-1L);
        assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser(-1L));
    }

    @Test
    void createUser_InvalidAge() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setAge(151);

        assertThrows(ConstraintViolationException.class,
                () -> userService.createUser(user));
    }

    @Test
    void updateUser_UpdateName() {
        User existing = createUserWithId(1L);
        existing.setName("Old Name");
        existing.setEmail("same@email.com");

        User updateData = createUserWithId(1L);
        updateData.setName("New Name");
        updateData.setEmail("same@email.com");

        when(userDao.read(1L)).thenReturn(Optional.of(existing));

        userService.updateUser(updateData);

        verify(userDao).update(argThat(u ->
                u.getName().equals("New Name")
        ));
    }

    @Test
    void updateUser_UserNotFound() {
        User updateData = createUserWithId(999L);
        updateData.setName("New Name");
        updateData.setEmail("new@example.com");

        when(userDao.read(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(updateData));
    }

    @Test
    void updateUser_EmailConflict() {
        User existing = createUserWithId(1L);
        existing.setEmail("old@example.com");
        existing.setName("Existing");
        existing.setAge(25);

        User updateData = createUserWithId(1L);
        updateData.setEmail("conflict@example.com"); // другой email
        updateData.setName("Updated");
        updateData.setAge(30);

        when(userDao.read(1L)).thenReturn(Optional.of(existing));
        when(userDao.existsByEmail("conflict@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(updateData));
    }

    @Test
    void updateUser_InvalidUser() {
        User user = createUserWithId(1L);
        user.setEmail("invalid-email");
        user.setAge(200);

        assertThrows(ConstraintViolationException.class,
                () -> userService.updateUser(user));
    }

}