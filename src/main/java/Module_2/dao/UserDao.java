package Module_2.dao;

import Module_2.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    void create(User user);

    Optional<User> read(Long id);

    List<User> readAll();

    boolean update(User user);

    boolean delete(Long id);

    boolean existsByEmail(String email);
}
