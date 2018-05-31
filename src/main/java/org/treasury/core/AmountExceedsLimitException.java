package org.treasury.core;

import java.util.Date;

public class AmountExceedsLimitException extends Exception {

    public long exceeds;
    public Date nextWindow;

    public AmountExceedsLimitException(long satoshis, Date nextWindow) {
        super("Amount exceeds the limit (" + satoshis + ") set by treasury ");
        this.exceeds = satoshis;
        this.nextWindow = nextWindow;
    }
}
