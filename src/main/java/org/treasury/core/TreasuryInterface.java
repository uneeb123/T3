package org.treasury.core;

import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.Wallet;
import org.treasury.core.exceptions.AmountExceedsLimitException;
import org.treasury.core.exceptions.ClientError;
import org.treasury.core.exceptions.InitiationSequenceException;
import org.treasury.core.model.TransactionHistory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TreasuryInterface {
    private final String walletName = "walletappkit-example";

    public static final long MINUTE = 60000;
    public static final long HOUR = MINUTE * 60;
    public static final long DAY = HOUR * 24;
    public static final long WEEK = DAY * 7;

    private File directory = new File(".");

    private WalletAppKit kit;
    private Controller controller;

    public void initiateKit() {
        NetworkParameters params = TestNet3Params.get();
        kit = new WalletAppKit(params, directory, walletName);
        kit.startAsync();
        kit.awaitRunning();
    }

    public void initiateTreasury(String treasuryId, boolean testMode, long cadence) {
        if (kit == null) {
            throw new InitiationSequenceException();
        }
        controller = new Controller(treasuryId, kit.wallet(), testMode, cadence);
    }

    public void syncTreasury() throws IOException, ClientError {
        if (controller == null) {
            throw new InitiationSequenceException();
        }
        controller.syncTreasury();
    }


    public Transaction createTransaction(Coin value, String to)throws
            IOException, ClientError, InsufficientMoneyException, AmountExceedsLimitException {
        try {
            if (!controller.complyWithAccessControls(value)) {
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
            controller.postTransaction(newItem);
            return result.tx;
        } catch (AmountExceedsLimitException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    public void getFreshAddress() throws IOException, ClientError {
        Address newAddress = kit.wallet().freshReceiveAddress();
        controller.postAddress(newAddress.toString());
        kit.wallet().addWatchedAddress(newAddress);
    }

    public void getBalance() throws IOException, ClientError {
        Coin balance = kit.wallet().getBalance();
        controller.postBalance(balance.value);
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    /**
     * Not implemented at the moment, but high priority
     */
    public void getMnemonicCode() {
        DeterministicSeed seed = kit.wallet().getKeyChainSeed();
        seed.getCreationTimeSeconds();
        seed.getMnemonicCode();
    }

    public void restoreWallet(List<String> mnemonicCode, long creationTime) {
        String passphrase = "";
        DeterministicSeed seed = new DeterministicSeed(mnemonicCode, null, passphrase, creationTime);
        kit.restoreWalletFromSeed(seed);
    }
}
