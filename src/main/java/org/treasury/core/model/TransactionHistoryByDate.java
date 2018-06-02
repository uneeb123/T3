package org.treasury.core.model;

import java.util.Comparator;

public class TransactionHistoryByDate implements Comparator<TransactionHistory> {
    @Override
    public int compare(TransactionHistory t1, TransactionHistory t2) {
        return t1.created_on.compareTo(t2.created_on);
    }
}
