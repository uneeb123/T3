package org.treasury.core;

import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.Wallet;

import java.io.File;
import java.util.Date;
import java.util.List;

public class Main {

    private String walletName = "walletappkit-example";
    private NetworkParameters params = TestNet3Params.get();
    private File directory = new File(".");
    private WalletAppKit kit;
    private String treasuryId;
    private String memberId;

    public void initiate() {
        kit = new WalletAppKit(params, directory, walletName);
        kit.startAsync();
        kit.awaitRunning();
        // load treasury
    }

    public void createTransaction(String amount, String to) throws Exception {
        String faucetAddr = "2N8hwP1WmJrFF5QWABn38y63uYLhnJYJYTF";
        try {
            Coin value = Coin.parseCoin(amount);
            Wallet wallet = kit.wallet();
            Address toAddr = Address.fromBase58(wallet.getParams(), to);
            Wallet.SendResult result = wallet.sendCoins(kit.peerGroup(), toAddr, value);
            String tx_id = result.tx.getHashAsString();
        } catch (Exception e) {
            throw e;
        }
        // comply with treasury rules
        // post to the treasury
        // actually create transaction
    }

    public Address getFreshAddress() {
        return kit.wallet().freshReceiveAddress();
    }

    public Coin getBalance() {
        return kit.wallet().getBalance();
    }

    public void syncTreasury() {
        Wallet wallet = kit.wallet();
        List<Transaction> transactions = wallet.getTransactionsByTime();
        NetworkParameters params = wallet.getNetworkParameters();
        // check transactions
        // compare with the server tx_ids
        // add new
    }

    public static void main(String[] args) throws Exception {
        String treasuryId = "15f5cf3b-bdef-4708-9634-1c79bf17c0c8";
        long allowedDifference = 86400; // one day
        TreasuryClient c = new TreasuryClient(true, treasuryId);
        Treasury treasury = c.getTreasury();
        Integer limit  = treasury.spending_limit;
        TransactionHistory last = treasury.history[treasury.history.length-1];
        Date lastTransactionSeen = last.created_on;
        Date today = new Date();
        long difference =  (today.getTime()/1000) - (lastTransactionSeen.getTime()/1000);
        TransactionHistory newItem = new TransactionHistory("x105",
                10,
                today,
                "1234");
        int responseCode = c.postTransaction(newItem);
        c.postAddress("CDE");
        System.out.println(responseCode);
        /*
        Main t = new Main();
        t.initiate();
        String address = t.getFreshAddress().toString();
        System.out.println("address " + address);
        String balance = t.getBalance().toFriendlyString();
        System.out.println("balance " + balance);
        */
    }
}