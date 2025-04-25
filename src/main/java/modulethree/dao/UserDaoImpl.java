package modulethree.dao;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import modulethree.model.User;
import modulethree.util.TransactionUtil;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Реализация интерфейса {@link modulethree.dao.UserDao}, предоставляющая методы для работы
 * с данными пользователей в базе данных.
 *
 * <p>Класс реализует CRUD-операции (Create, Read, Update, Delete)
 * для сущности {@code User} с использованием Hibernate.</p>
 *
 * <p>Пример использования:
 * <pre>{@code
 * UserDao userDao = new UserDaoImpl(dataSource);
 * User user = userDao.findById(1L);
 * }</pre>
 *
 * @author Ibragim Gadzhiev
 * @version 1.0
 * @see modulethree.dao.UserDao
 * @see modulethree.model.User
 */

public class UserDaoImpl implements UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public void create(User user) {
        validateUser(user);

        if (existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }

        TransactionUtil.doInTransaction(session -> {
            session.persist(user);
            logger.info("User created. ID: {}", user.getId());
        });
    }

    @Override
    public Optional<User> read(Long id) {
        validateId(id);
        return TransactionUtil.doInTransaction(session -> {
            User user = session.get(User.class, id);
            if (user != null) {
                logger.debug("Retrieved user with ID: {}", id);
            } else {
                logger.warn("User with ID {} not found", id);
            }
            return Optional.ofNullable(user);
        });
    }

    @Override
    public List<User> readAll() {
        return TransactionUtil.doInTransaction(session -> {
            Query<User> query = session.createQuery("FROM User", User.class);
            List<User> users = query.list();
            logger.info("Retrieved {} users", users.size());
            return users;
        });
    }

    @Override
    public boolean update(User user) {
        validateUser(user);
        return TransactionUtil.doInTransaction(session -> {
            User existing = session.get(User.class, user.getId());
            if (existing == null) {
                logger.warn("Update failed: User with ID {} not found", user.getId());
                return false;
            }

            if (!existing.getEmail().equals(user.getEmail())
                    && existsByEmail(user.getEmail())) {
                logger.error("Email {} already exists", user.getEmail());
                throw new IllegalArgumentException("Email already exists");
            }

            session.merge(user);
            logger.info("User updated. ID: {}", user.getId());
            return true;
        });
    }

    private void validateUser(User user) {
        if (user == null) {
            logger.error("User cannot be null");
            throw new IllegalArgumentException("User cannot be null");
        }

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        if (!violations.isEmpty()) {
            String errorMsg = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            logger.error("Validation failed: {}", errorMsg);
            throw new ConstraintViolationException(errorMsg, violations);
        }
    }

    @Override
    public boolean delete(Long id) {
        validateId(id);
        return TransactionUtil.doInTransaction(session -> {
            User user = session.get(User.class, id);
            if (user == null) {
                logger.warn("Delete failed: User with ID {} not found", id);
                return false;
            }
            session.remove(user);
            logger.info("User deleted. ID: {}", id);
            return true;
        });
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            logger.error("Invalid ID: {}", id);
            throw new IllegalArgumentException("Invalid ID: " + id);
        }
    }

    /**
     * Проверяет, существует ли пользователь с указанным email в базе данных.
     *
     *<p>Метод выполняет запрос в базу данных, чтобы узнать,
     * есть ли хотя бы один пользователь с указанным email.
     * Запрос выполняется в рамках транзакции.</p>
     *
     * @param email адрес электронной почты пользователя для проверки.
     * @return {@code true}, если пользователь с таким email существует в базе данных,
     *         иначе {@code false}.
     */
    public boolean existsByEmail(String email) {
        return TransactionUtil.doInTransaction(session ->
                session.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                        .setParameter("email", email)
                        .uniqueResult() > 0
        );
    }
}