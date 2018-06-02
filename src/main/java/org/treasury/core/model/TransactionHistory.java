package org.treasury.core.model;

import java.util.Date;

public class TransactionHistory {
    public String to_address;
    public long amount;
    public Date created_on; // some ambiguation here, should be update time
    public String tx_id;
    public long fee;

    public TransactionHistory(String to_address,
                              long amount,
                              Date created_on,
                              String tx_id,
                              long fee) {
        this.to_address = to_address;
        this.amount = amount;
        this.created_on = created_on;
        this.tx_id = tx_id;
        this.fee = fee;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TransactionHistory &&
                this.tx_id.equals(((TransactionHistory)obj).tx_id);
    }

    public boolean incoming() {
        if (amount > 0) {
            return true;
        } else {
            return false;
        }
    }
}
