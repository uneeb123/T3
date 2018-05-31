package org.treasury.core.pojo;

import org.treasury.core.pojo.TransactionHistory;

import java.util.Comparator;

public class TransactionHistoryByDate implements Comparator<TransactionHistory> {
    @Override
    public int compare(TransactionHistory t1, TransactionHistory t2) {
        long time1 = t1.created_on.getTime();
        long time2 = t2.created_on.getTime();
        if (time1 - time2 > 0) {
            return 1;
        }
        else if (time1 - time2 < 0) {
            return -1;
        }
        return 0;
    }
}
