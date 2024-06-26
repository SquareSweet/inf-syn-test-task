package dao;

import data.ConnectionPool;
import exception.RuntimeSqlException;
import exception.UsernameAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import model.User;
import org.postgresql.util.PSQLException;

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
            try (PreparedStatement createUserStatement = connection.prepareStatement(createUser, Statement.RETURN_GENERATED_KEYS)) {
                createUserStatement.setString(1, user.getUsername());
                createUserStatement.setString(2, user.getPassword());
                createUserStatement.executeUpdate();
                ResultSet userResult = createUserStatement.getGeneratedKeys();
                if (userResult != null && userResult.next()) {
                    user.setId(userResult.getLong(1));
                    log.debug("User id {} created", user.getId());
                } else {
                    log.error("Error occurred while retrieving saved user id");
                }
            } catch (PSQLException e) {
                connection.rollback();
                if ("23505".equals(e.getSQLState())) {
                    log.error("User with username {} already exists", user.getUsername());
                    throw new UsernameAlreadyExistsException("User with username " + user.getUsername() + " already exists");
                } else {
                    throw e;
                }
            }

            //DB is designed for functionality extension, such as user having multiple or no accounts
            //Leading to separation of creating new users and accounts
            //Since such functionality is not required for test assignment, account creation is tied to user creation
            String createAccount = "INSERT INTO accounts (user_id, balance) VALUES (?, ?)";
            try (PreparedStatement createAccountStatement = connection.prepareStatement(createAccount, Statement.RETURN_GENERATED_KEYS)) {
                createAccountStatement.setLong(1, user.getId());
                createAccountStatement.setLong(2, 50000);
                createAccountStatement.executeUpdate();
                ResultSet accountResult = createAccountStatement.getGeneratedKeys();
                if (accountResult != null && accountResult.next()) {
                    log.debug("Account id {} created", (accountResult.getLong(1)));
                } else {
                    log.error("Error occurred while retrieving saved account id");
                }
            } catch (SQLException e) {
                connection.rollback();
                log.error("Error occurred while creating account for new user id {}", user.getId());
                throw e;
            }

            connection.commit();

            return user;
        } catch (SQLException e) {
            log.error("Error occurred while creating new user: {}", e.getMessage());
            throw new RuntimeSqlException("Error occurred while creating new user: " + e.getMessage());
        }
    }

    @Override
    public Optional<User> getByUsername(String username) {
        try (Connection connection = connectionPool.getConnection()) {
            String query = "SELECT id, username, password FROM users WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.executeQuery();
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
        } catch (SQLException e) {
            log.error("Error occurred while retrieving user from database: {}", e.getMessage());
            throw new RuntimeSqlException("Error occurred while retrieving user from database: " + e.getMessage());
        }
    }
}
