package modulethree.dao;

import java.util.List;
import java.util.Optional;
import modulethree.model.User;

/**
 * Интерфейс для доступа к данным пользователей.
 * Определяет стандартные CRUD-операции и дополнительные методы поиска.
 */
public interface UserDao {
    /**
     * Создаёт нового пользователя в БД.
     *
     * @param user объект пользователя для сохранения
     */
    void create(User user);

    /**
     * Ищет пользователя по ID.
     *
     * @param id уникальный идентификатор пользователя
     * @return {@link Optional} с найденным пользователем,
     *     либо пустой {@link Optional}, если пользователь не найден
     */
    Optional<User> read(Long id);

    /**
     * Возвращает список всех пользователей.
     *
     * @return список всех пользователей
     */
    List<User> readAll();

    /**
     * Обновляет информацию о существующем пользователе.
     *
     * @param user объект пользователя с обновлёнными данными
     * @return {@code true}, если обновление прошло успешно;
     *         {@code false}, если пользователь не найден
     */
    boolean update(User user);

    /**
     * Удаляет пользователя по ID.
     *
     * @param id уникальный идентификатор пользователя
     * @return {@code true}, если удаление прошло успешно;
     *         {@code false}, если пользователь не найден
     */
    boolean delete(Long id);

    /**
     * Проверяет существование пользователя по его адресу электронной почты.
     *
     * @param email адрес электронной почты пользователя
     * @return {@code true}, если пользователь с таким email найден;
     *         {@code false} если пользователь не найден
     */
    boolean existsByEmail(String email);
}
