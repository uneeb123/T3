package org.treasury.core;

import org.bitcoinj.core.*;
import org.treasury.core.exceptions.AmountExceedsLimitException;
import org.treasury.core.exceptions.ClientError;

import java.io.IOException;

public class Example {

    public static void main(String[] args) {
        TreasuryInterface t = new TreasuryInterface();
        String treasuryId = "2679ae24-495f-4bab-93db-c8761bbc264a";
        String faucetAddr = "2N8hwP1WmJrFF5QWABn38y63uYLhnJYJYTF";
        t.initiateKit();
        t.initiateTreasury(treasuryId, true, TreasuryInterface.HOUR*10);
        try {
            t.syncTreasury();
            t.getFreshAddress();
            t.getBalance();
            long amount = 9000;
            Coin value = Coin.valueOf(amount);
            try {
                t.createTransaction(value, faucetAddr);
            } catch (InsufficientMoneyException e) {
                e.printStackTrace();
            } catch (AmountExceedsLimitException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClientError clientError) {
            clientError.printStackTrace();
        }
    }
}