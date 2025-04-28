package modulethree.service;

import java.util.List;
import java.util.Optional;
import modulethree.dao.UserDao;
import modulethree.model.User;

public class UserService {
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public void createUser(User user) {
        userDao.create(user);
    }

    public Optional<User> getUserById(Long id) {
        return userDao.read(id);
    }

    public List<User> getAllUsers() {
        return userDao.readAll();
    }

    public boolean updateUser(User user) {
        return userDao.update(user);
    }

    public boolean deleteUser(Long id) {
        return userDao.delete(id);
    }
}