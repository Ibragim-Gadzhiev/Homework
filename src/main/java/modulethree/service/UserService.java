package modulethree.service;

import java.util.List;
import java.util.Optional;
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

    /**
     * Создаёт экземпляр сервиса с указанным DAO пользователя.
     *
     * @param userDao DAO для работы с данными пользователей
     */
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Создаёт нового пользователя после проверки валидности данных.
     *
     * @param user пользователь для создания
     * @throws IllegalArgumentException если данные пользователя некорректны
     * @throws IllegalStateException    если email уже существует
     */
    public void createUser(User user) {
        logger.debug("Attempting to create user: {}", user.getEmail());
        try {
            userDao.create(user);
            logger.info("User created successfully. ID: {}", user.getId());
        } catch (IllegalArgumentException e) {
            logger.error("User creation failed: {}", e.getMessage());
            throw e;
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
     * @param user пользователь с обновлёнными данными
     * @return true, если обновление успешно
     * @throws IllegalArgumentException если данные пользователя некорректны
     * @throws IllegalStateException    если email уже используется другим пользователем
     */
    public boolean updateUser(User user) {
        logger.debug("Updating user ID: {}", user.getId());
        try {
            boolean updated = userDao.update(user);
            if (updated) {
                logger.info("User updated successfully. ID: {}", user.getId());
            } else {
                logger.warn("Update failed - user not found. ID: {}", user.getId());
            }
            return updated;
        } catch (IllegalArgumentException e) {
            logger.error("User update failed: {}", e.getMessage());
            throw e;
        }
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