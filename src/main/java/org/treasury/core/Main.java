package org.treasury.core;

import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.Wallet;
import org.treasury.core.pojo.TransactionHistory;
import org.treasury.core.pojo.Treasury;

import java.io.File;
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

    public void initiateTreasury(String treasuryId) throws Exception {
        if (kit == null) {
            throw new Exception("Initialize WalletAppKit first");
        }
        controls = new Controls(treasuryId, kit.wallet());
    }

    public void syncTreasury() throws Exception {
        if (controls == null) {
            throw new Exception("Initialize Controls first");
        }
        controls.syncTreasury();
    }


    public void createTransaction(String amount, String to) throws Exception {
        try {
            Coin value = Coin.parseCoin(amount);
            if (!controls.complyWithAccessControls(value)) {
                throw new Exception("Conditions not met");
            }
            Wallet wallet = kit.wallet();
            Address toAddr = Address.fromBase58(wallet.getParams(), to);
            Wallet.SendResult result = wallet.sendCoins(kit.peerGroup(), toAddr, value);
            String tx_id = result.tx.getHashAsString();
            // post it
        } catch (Exception e) {
            throw e;
        }
    }

    public Address getFreshAddress() {
        // post it
        return kit.wallet().freshReceiveAddress();
    }

    public Coin getBalance() {
        return kit.wallet().getBalance();
    }

    public static void main(String[] args) throws Exception {
        Main t = new Main();
        String treasuryId = "21ce720d-cc65-4de0-885b-ba2ad0664900";
        String faucetAddr = "2N8hwP1WmJrFF5QWABn38y63uYLhnJYJYTF";
        t.initiateKit();
    }
}