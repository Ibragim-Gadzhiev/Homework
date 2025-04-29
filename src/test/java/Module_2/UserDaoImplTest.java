package Module_2;

import Module_2.dao.UserDao;
import Module_2.dao.UserDaoImpl;
import Module_2.model.User;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class UserDaoImplTest {

    // Создаем контейнер PostgreSQL
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    private Connection connection;
    private UserDao userDao;

    @AfterEach
    void clear() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM users");
            System.out.println("Table 'users' cleared.");
        } catch (SQLException e) {
            System.out.println("Error while clearing table: " + e.getMessage());
        }

    }
    // Настройка подключения к базе данных перед каждым тестом
    @BeforeEach
    void setUp() throws SQLException {
        String jdbcUrl = postgres.getJdbcUrl();
        connection = DriverManager.getConnection(jdbcUrl, "testuser", "testpass");
        connection.setAutoCommit(false);

        // Создаем таблицу users
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id SERIAL PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(255) NOT NULL UNIQUE, " +
                    "age INTEGER NOT NULL, " +
                    "created_at TIMESTAMP) ");
        }
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM users");
            System.out.println("Table 'users' cleared before test.");
        }



        userDao = new UserDaoImpl();
    }
    @Test
    void testCreateUser_Success() throws SQLException {
        User user = new User();
        user.setName("Asker");
        user.setEmail("asker@mail.com");
        user.setAge(30);

        userDao.create(user);

        assertTrue(userDao.existsByEmail("asker@mail.com"));
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