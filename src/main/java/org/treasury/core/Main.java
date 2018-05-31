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


    public void createTransaction(Coin value, String to) throws Exception {
        try {
            if (!controls.complyWithAccessControls(value)) {
                throw new Exception("Conditions not met");
            }
            Wallet wallet = kit.wallet();
            Address toAddr = Address.fromBase58(wallet.getParams(), to);
            Wallet.SendResult result = wallet.sendCoins(kit.peerGroup(), toAddr, value);
            String tx_id = result.tx.getHashAsString();
            Date today = new Date();
            TransactionHistory newItem = new TransactionHistory(to, value.value, today, tx_id);
            controls.postTransaction(newItem);
        } catch (Exception e) {
            throw e;
        }
    }

    public void getFreshAddress() throws IOException, ClientError {
        Address newAddress = kit.wallet().freshReceiveAddress();
        controls.postAddress(newAddress.toString());
        kit.wallet().addWatchedAddress(newAddress);
    }

    public Coin getBalance() {
        return kit.wallet().getBalance();
    }

    public static void main(String[] args) throws IOException, ClientError {
        Main t = new Main();
        String treasuryId = "21ce720d-cc65-4de0-885b-ba2ad0664900";
        String faucetAddr = "2N8hwP1WmJrFF5QWABn38y63uYLhnJYJYTF";
        t.initiateKit();
        t.initiateTreasury(treasuryId);
        t.syncTreasury();
        t.getFreshAddress();
        System.out.println(t.getBalance().toFriendlyString());
        long amount = 4000;
        Coin value = Coin.valueOf(amount);
        try {
            t.createTransaction(value, faucetAddr);
        } catch (Exception e) {
            e.getCause();
            System.out.println("Conditions not met");
        }
    }
}