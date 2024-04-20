package dao;

import data.ConnectionPool;
import lombok.extern.slf4j.Slf4j;
import model.User;

import java.sql.*;
import java.util.Optional;

@Slf4j
public class UserDaoImpl implements UserDao {
    private final ConnectionPool connectionPool;

    public UserDaoImpl(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public User create(User user) {
        try (Connection connection = connectionPool.getConnection()) {
            connection.setAutoCommit(false);

            String createUser = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement createUserStatement = connection.prepareStatement(createUser, Statement.RETURN_GENERATED_KEYS);
            createUserStatement.setString(1, user.getUsername());
            createUserStatement.setString(2, user.getPassword());
            createUserStatement.executeUpdate();
            ResultSet userResult = createUserStatement.getGeneratedKeys();
            if (userResult != null && userResult.next()) {
                user.setId(userResult.getLong(1));
                log.debug("User id " + user.getId() + " created");
            } else {
                log.error("Error occurred while retrieving saved user id");
            }
            if (userResult != null) userResult.close();
            createUserStatement.close();

            //DB is designed for functionality extension, such as user having multiple or no accounts
            //Leading to separation of creating new users and accounts
            //Since such functionality is not required for test assignment, account creation is tied to user creation
            String createAccount = "INSERT INTO accounts (user_id) VALUES (?)";
            PreparedStatement createAccountStatement = connection.prepareStatement(createAccount, Statement.RETURN_GENERATED_KEYS);
            createAccountStatement.setLong(1, user.getId());
            createAccountStatement.executeUpdate();
            ResultSet accountResult = createAccountStatement.getGeneratedKeys();
            if (accountResult != null && accountResult.next()) {
                log.debug("Account id " + (accountResult.getLong(1)) + " created");
            } else {
                log.error("Error occurred while retrieving saved account id");
            }
            if (accountResult != null) accountResult.close();
            createAccountStatement.close();

            connection.commit();

            return user;
        } catch (SQLException e) {
            log.error("Error occurred while creating new user: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> getByUsername(String username) {
        try (Connection connection = connectionPool.getConnection()) {
            String query = "SELECT id, username, password FROM users WHERE username like ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username);
                statement.execute();
                ResultSet resultSet = statement.getResultSet();
                if (resultSet != null && resultSet.next()) {
                    return Optional.of(new User(
                            resultSet.getLong("id"),
                            resultSet.getString("username"),
                            resultSet.getString("password")
                    ));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            log.error("Error occurred while retrieving user from database: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
