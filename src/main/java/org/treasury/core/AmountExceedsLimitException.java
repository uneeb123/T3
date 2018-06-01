package org.treasury.core;

import java.util.Date;

public class AmountExceedsLimitException extends Exception {

    public long limit;
    public Date nextWindow;

    public AmountExceedsLimitException(long satoshis, Date nextWindow) {
        super("Amount exceeds the limit (" + satoshis + " satoshis) set by treasury. Try again at " +
                nextWindow.toString());
        this.limit = satoshis;
        this.nextWindow = nextWindow;
    }
}
