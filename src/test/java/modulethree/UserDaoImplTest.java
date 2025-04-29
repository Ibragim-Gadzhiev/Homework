package modulethree;

import modulethree.util.TransactionUtil;
import jakarta.validation.ConstraintViolationException;
import modulethree.dao.UserDao;
import modulethree.dao.UserDaoImpl;
import modulethree.model.User;
import modulethree.util.HibernateUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class UserDaoImplTest {
    @SuppressWarnings("resource")
    @Container
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:latest")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    private static UserDao userDao;

    @BeforeAll
    static void setup() {
        HibernateUtil.setConfig(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );
        userDao = new UserDaoImpl();
    }

    @AfterAll
    static void cleanup() {
        HibernateUtil.shutdown();
    }

    @BeforeEach
    void clearDatabase() {
        TransactionUtil.doInTransaction(session -> {
            session.createMutationQuery("DELETE FROM User").executeUpdate();
        });
    }

    @Test
    void createUser_WithValidData_SuccessfullyCreatesUser() {
        User user = createTestUser("test@example.com");

        assertNotNull(user.getId());
        Optional<User> retrievedUser = userDao.read(user.getId());
        assertTrue(retrievedUser.isPresent());
        assertEquals("Test User", retrievedUser.get().getName());
    }

    @Test
    void createUser_WithInvalidEmail() {
        User user = new User();
        user.setName("👨🏻‍🦽 Email");
        user.setEmail("👨🏻‍🦽-email");
        user.setAge(-25);

        assertThrows(ConstraintViolationException.class, () -> userDao.create(user));
    }

    @Test
    void createUser_WithInvalidLess() {
        User user = new User();
        user.setName("Ibragim Gadzhiev");
        user.setEmail("unknown.nvme@gmail.com");
        user.setAge(-25);

        assertThrows(ConstraintViolationException.class, () -> userDao.create(user));
    }

    @Test
    void createUser_WithInvalidAgeOver() {
        User user = new User();
        user.setName("Ibragim Gadzhiev");
        user.setEmail("unknown.nvme@gmail.com");
        user.setAge(250);

        assertThrows(ConstraintViolationException.class, () -> userDao.create(user));
    }

    @Test
    void createUser_WithDuplicateEmail_ThrowsException() {
        User user1 = createTestUser("duplicate@example.com");

        User user2 = new User();
        user2.setName("Duplicate User");
        user2.setEmail("duplicate@example.com");
        user2.setAge(30);

        assertThrows(IllegalArgumentException.class, () -> userDao.create(user2));
    }

    @Test
    void readUser_WithExistingId() {
        User user = createTestUser("read@test.com");
        Optional<User> result = userDao.read(user.getId());

        assertTrue(result.isPresent());
        assertEquals(user.getEmail(), result.get().getEmail());
    }

    @Test
    void readUser_WithNonExistingId() {
        Optional<User> result = userDao.read(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void readUser_WithInvalidId() {
        assertThrows(IllegalArgumentException.class, () -> userDao.read(-1L));
    }

    @Test
    void readAll() {
        createTestUser("user1@test.com");
        createTestUser("user2@test.com");

        assertEquals(2, userDao.readAll().size());
    }

    @Test
    void updateUser() {
        User user = createTestUser("update@test.com");
        user.setName("Updated Name");

        assertTrue(userDao.update(user));

        Optional<User> optionalUser = userDao.read(user.getId());
        assertTrue(optionalUser.isPresent());

        assertEquals("Updated Name", optionalUser.get().getName());
    }

    @Test
    void deleteUser() {
        User user = createTestUser("delete@test.com");
        assertTrue(userDao.delete(user.getId()));
        assertFalse(userDao.read(user.getId()).isPresent());
    }

    @Test
    void existsByEmail() {
        createTestUser("update.from.lera@test.com");
        assertTrue(userDao.existsByEmail("update.from.lera@test.com"));
    }

    @Test
    void existsByEmail_WithNewEmail() {
        assertFalse(userDao.existsByEmail("unknown_nvme@gmail.com"));
    }

    private User createTestUser(String email) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        user.setAge(30);
        userDao.create(user);
        return user;
    }
}