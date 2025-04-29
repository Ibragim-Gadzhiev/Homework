package modulethree.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import modulethree.dao.UserDao;
import modulethree.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Сервис для управления бизнес-логикой, связанной с пользователями.
 * Обеспечивает CRUD операции над пользователями,
 * а также взаимодействует с DAO слоем для доступа к данным.
 */
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDao userDao;
    private final Validator validator;

    /**
     * Создаёт экземпляр сервиса с указанным DAO пользователя.
     *
     * @param userDao DAO для работы с данными пользователей
     */
    public UserService(UserDao userDao) {
        this.userDao = userDao;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }
    /**
     * Создаёт нового пользователя после проверки валидности данных.
     *
     * @param user пользователь для создания
     * @throws IllegalArgumentException если данные пользователя некорректны
     * @throws IllegalStateException    если email уже существует
     */
    public void createUser(User user) {
        validateUser(user);

        logger.debug("Attempting to create user: {}", user.getEmail());
        if (userDao.existsByEmail(user.getEmail())) {
            throw new IllegalStateException("Email already exists: " + user.getEmail());
        }

        userDao.create(user);
        logger.info("User created successfully. ID: {}", user.getId());
    }

    private void validateUser(User user) {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            String errorMsg = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            logger.error("Validation failed: {}", errorMsg);
            throw new ConstraintViolationException(errorMsg, violations);
        }
    }

    /**
     * Получает пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @return {@link Optional} с пользователем, если найден
     */
    public Optional<User> getUserById(Long id) {
        logger.debug("Fetching user by ID: {}", id);
        Optional<User> user = userDao.read(id);
        if (user.isEmpty()) {
            logger.warn("User not found for ID: {}", id);
        }
        return user;
    }

    /**
     * Возвращает список всех пользователей.
     *
     * @return список пользователей
     */
    public List<User> getAllUsers() {
        logger.debug("Fetching all users");
        List<User> users = userDao.readAll();
        logger.info("Retrieved {} user(s)", users.size());
        return users;
    }

    /**
     * Обновляет данные пользователя.
     *
     * <p>Выполняет валидацию данных пользователя и обновляет его в хранилище.</p>
     *
     * @param user пользователь с обновлёнными данными
     * @return true, если обновление выполнено успешно
     * @throws ConstraintViolationException если данные пользователя не проходят валидацию
     * @throws IllegalArgumentException     если {@code user.getId() == null}, пользователь не найден
     *                                      или email уже используется другим пользователем
     */
    public boolean updateUser(User user) {
        validateUser(user);

        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        Optional<User> existingOpt = userDao.read(user.getId());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User existing = existingOpt.get();

        if (!existing.getEmail().equals(user.getEmail()) &&
                userDao.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        existing.setEmail(user.getEmail());
        existing.setName(user.getName());

        return userDao.update(existing);
    }


    /**
     * Удаляет пользователя по ID.
     *
     * @param id идентификатор пользователя
     * @return true, если удаление успешно
     */
    public boolean deleteUser(Long id) {
        logger.debug("Deleting user ID: {}", id);
        boolean deleted = userDao.delete(id);
        if (deleted) {
            logger.info("User deleted successfully. ID: {}", id);
        } else {
            logger.warn("Delete failed - user not found. ID: {}", id);
        }
        return deleted;
    }
}