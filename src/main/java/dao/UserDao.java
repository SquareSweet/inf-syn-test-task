package dao;

import model.User;

import java.util.Optional;

public interface UserDao {
    User create(User user);
    Optional<User> getByUsername(String username);
}
