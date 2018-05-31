package org.treasury.core;

public class AmountExceedsLimitException extends Exception {
    public AmountExceedsLimitException(long satoshis) {
        super("Amount exceeds the limit (" + satoshis + ") set by treasury ");
    }
}
