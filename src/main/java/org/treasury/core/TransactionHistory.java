package org.treasury.core;

import java.util.Date;

public class TransactionHistory {
    public String to_address;
    public Integer amount;
    public Date created_on;
    public String tx_id;

    TransactionHistory(String to_address,
                       Integer amount,
                       Date created_on,
                       String tx_id) {
        this.to_address = to_address;
        this.amount = amount;
        this.created_on = created_on;
        this.tx_id = tx_id;
    }
}
