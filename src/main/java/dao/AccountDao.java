package dao;

import model.Account;

import java.util.Optional;

public interface AccountDao {
    Optional<Account> getByUsername(String username);
    void transferByAccountId(Long senderAccountId, Long receiverAccountId, Long amount);
}
