package Service;

import dao.AccountDao;
import dto.BalanceResponse;
import dto.TransferRequest;
import dto.TransferResponse;
import exception.InsufficientBalanceException;
import exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import model.Account;

import java.math.BigDecimal;

@Slf4j
public class AccountServiceImpl implements AccountService {
    private final AccountDao accountDao;

    public AccountServiceImpl(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public BalanceResponse checkBalance(String username) {
        Account account = accountDao.getByUsername(username).orElseThrow(
                () -> new UserNotFoundException("User " + username + " not found or does not have an account")
        );
        log.debug("User {} has requested balance: {}", username, account.getBalance());
        return new BalanceResponse(BigDecimal.valueOf(account.getBalance() / 100.0));
    }

    @Override
    public TransferResponse transferToUsername(String senderUsername, TransferRequest transferRequest) {
        //Checking if account exists
        Account senderAccount = accountDao.getByUsername(senderUsername).orElseThrow(
                () ->  new UserNotFoundException("User " + senderUsername + " not found or does not have an account")
        );

        //Checking if balance is enough for transaction
        Long amountLong = transferRequest.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
        if (senderAccount.getBalance() < amountLong) {
            throw new InsufficientBalanceException("User " + senderUsername + " has insufficient balance");
        }

        //Checking if account exists
        Account receiverAccount = accountDao.getByUsername(transferRequest.getReceiverUsername()).orElseThrow(
                () -> new UserNotFoundException("User " + transferRequest.getReceiverUsername() +
                            " not found or does not have an account")
        );

        accountDao.transferByAccountId(senderAccount.getId(), receiverAccount.getId(), amountLong);
        log.debug("Transaction of {} sent from user {} to user {}",
                amountLong, senderUsername, transferRequest.getReceiverUsername());

        return new TransferResponse(BigDecimal.valueOf(senderAccount.getBalance() / 100.0));
    }
}
