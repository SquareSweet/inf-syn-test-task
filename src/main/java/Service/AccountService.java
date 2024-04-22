package Service;

import dto.BalanceResponse;
import dto.TransferRequest;
import dto.TransferResponse;

public interface AccountService {
    BalanceResponse checkBalance(String username);
    TransferResponse transferToUsername(String senderUsername, TransferRequest transferRequest);
}
