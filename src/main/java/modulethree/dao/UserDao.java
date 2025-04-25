package modulethree.dao;

import java.util.List;
import java.util.Optional;
import modulethree.model.User;

public interface UserDao {
    void create(User user);

    Optional<User> read(Long id);

    List<User> readAll();

    boolean update(User user);

    boolean delete(Long id);

    boolean existsByEmail(String email);
}
