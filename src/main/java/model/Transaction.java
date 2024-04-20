package model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Transaction {
    Long id;
    Long senderUser;
    Long receiverUser;
    Long amount;
}
