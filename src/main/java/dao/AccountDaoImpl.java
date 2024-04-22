package dao;

import data.ConnectionPool;
import exception.RuntimeSqlException;
import lombok.extern.slf4j.Slf4j;
import model.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Slf4j
public class AccountDaoImpl implements AccountDao {
    private final ConnectionPool connectionPool;

    public AccountDaoImpl(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public Optional<Account> getByUsername(String username) {
        try (Connection connection = connectionPool.getConnection()) {
            String query = "SELECT a.id, a.user_id, a.balance FROM accounts a INNER JOIN users u on a.user_id = u.id " +
                    "WHERE u.username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.executeQuery();
            ResultSet resultSet = statement.getResultSet();
            if (resultSet != null && resultSet.next()) {
                return Optional.of(new Account(
                        resultSet.getLong("id"),
                        resultSet.getLong("user_id"),
                        resultSet.getLong("balance")
                ));
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            log.error("Error occurred while retrieving user from database: {}", e.getMessage());
            throw new RuntimeSqlException("Error occurred while retrieving account from database: " + e.getMessage());
        }
    }

    @Override
    public void transferByAccountId(Long senderAccountId, Long receiverAccountId, Long amount) {
        try (Connection connection = connectionPool.getConnection()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            String senderQuery = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
            PreparedStatement senderStatement = connection.prepareStatement(senderQuery);
            senderStatement.setLong(1, amount);
            senderStatement.setLong(2, senderAccountId);
            int rowsUpdated = senderStatement.executeUpdate();
            if (rowsUpdated == 0) {
                throw new RuntimeSqlException("Error occurred while updating account id " + senderAccountId + " balance");
            }

            String receiverQuery = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
            PreparedStatement receiverStatement = connection.prepareStatement(receiverQuery);
            receiverStatement.setLong(1, amount);
            receiverStatement.setLong(2, receiverAccountId);
            rowsUpdated = receiverStatement.executeUpdate();
            if (rowsUpdated == 0) {
                throw new RuntimeSqlException("Error occurred while updating account id " + receiverAccountId + " balance");
            }

            String transactionQuery = "INSERT INTO transactions (sender_account_id, receiver_account_id, amount) VALUES (?, ?, ?)";
            PreparedStatement transactionStatement = connection.prepareStatement(transactionQuery);
            transactionStatement.setLong(1, senderAccountId);
            transactionStatement.setLong(2, receiverAccountId);
            transactionStatement.setLong(3, amount);
            rowsUpdated = transactionStatement.executeUpdate();
            if (rowsUpdated == 0) {
                throw new RuntimeSqlException("Error occurred while logging transaction in the database");
            }

            connection.commit();
        } catch (SQLException e) {
            log.error("Error occurred while transferring money: {}", e.getMessage());
            throw new RuntimeSqlException("Error occurred while transferring money: {}" + e.getMessage());
        }
    }
}
