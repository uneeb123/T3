package org.treasury.core;

import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.Wallet;
import org.treasury.core.pojo.TransactionHistory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main {

    private final String walletName = "walletappkit-example";
    private final File directory = new File(".");

    private WalletAppKit kit;
    private Controls controls;

    public void initiateKit() {
        NetworkParameters params = TestNet3Params.get();
        kit = new WalletAppKit(params, directory, walletName);
        kit.startAsync();
        kit.awaitRunning();
    }

    public void initiateTreasury(String treasuryId) {
        if (kit == null) {
            throw new InitiationSequenceException();
        }
        controls = new Controls(treasuryId, kit.wallet());
    }

    public void syncTreasury() throws IOException, ClientError {
        if (controls == null) {
            throw new InitiationSequenceException();
        }
        controls.syncTreasury();
    }


    public Transaction createTransaction(Coin value, String to)throws
            IOException, ClientError, InsufficientMoneyException, AmountExceedsLimitException {
        try {
            if (!controls.complyWithAccessControls(value)) {
                // should throw an exception
                return null;
            }
            Wallet wallet = kit.wallet();
            Address toAddr = Address.fromBase58(wallet.getParams(), to);
            Wallet.SendResult result = wallet.sendCoins(kit.peerGroup(), toAddr, value);
            String tx_id = result.tx.getHashAsString();
            Date today = new Date();
            TransactionHistory newItem = new TransactionHistory(
                    to,-value.value, today, tx_id, result.tx.getFee().value
            );
            controls.postTransaction(newItem);
            return result.tx;
        } catch (AmountExceedsLimitException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    public void getFreshAddress() throws IOException, ClientError {
        Address newAddress = kit.wallet().freshReceiveAddress();
        controls.postAddress(newAddress.toString());
        kit.wallet().addWatchedAddress(newAddress);
    }

    public void getBalance() throws IOException, ClientError {
        Coin balance = kit.wallet().getBalance();
        controls.postBalance(balance.value);
    }

    public static void main(String[] args) {
        Main t = new Main();
        String treasuryId = "2679ae24-495f-4bab-93db-c8761bbc264a";
        String faucetAddr = "2N8hwP1WmJrFF5QWABn38y63uYLhnJYJYTF";
        t.initiateKit();
        t.initiateTreasury(treasuryId);
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