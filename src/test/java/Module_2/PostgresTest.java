package Module_2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class PostgresTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test")
            .withUsername("testuser")
            .withPassword("123");

    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        String jdbcUrl = postgres.getJdbcUrl();
        connection = DriverManager.getConnection(jdbcUrl, "testuser", "123");
    }

    @Test
    void testDatabaseConnection() throws Exception {
        // Создаем таблицу и вставляем данные
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, name VARCHAR(255))");
            stmt.execute("INSERT INTO users (name) VALUES ('John Doe')");
        }

        // Проверяем, что данные были добавлены
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            int count = rs.getInt(1);
            assertEquals(1, count);
        }
    }
}