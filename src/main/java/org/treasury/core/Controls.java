package org.treasury.core;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.wallet.Wallet;
import org.treasury.core.pojo.TransactionHistory;
import org.treasury.core.pojo.TransactionHistoryByDate;
import org.treasury.core.pojo.Treasury;

import java.io.IOException;
import java.util.*;

public class Controls {

    private TreasuryClient client;
    private Wallet wallet;
    private long allowedDifference; // in milliseconds

    Controls(String treasuryId, Wallet wallet, boolean testMode, long allowedDifference) {
        this.wallet = wallet;
        this.client = new TreasuryClient(testMode, treasuryId);
        this.allowedDifference = allowedDifference;
    }

    Controls(String treasuryId, Wallet wallet, boolean testMode) {
        this(treasuryId, wallet, testMode, 86400000); // one day
    }

    Controls(String treasuryId, Wallet wallet) {
        this(treasuryId, wallet, true);
    }

    public void postTransaction(TransactionHistory history) throws IOException, ClientError {
        client.postTransaction(history);
    }

    public void postAddress(String address) throws IOException, ClientError {
        client.postAddress(address);
    }

    public void postBalance(long balance) throws IOException, ClientError {
        client.postBalance(balance);
    }

    public boolean complyWithAccessControls(Coin amount)
            throws AmountExceedsLimitException, IOException, ClientError {
        Treasury treasury = client.getTreasury();
        long limit = treasury.spending_limit;
        List<TransactionHistory> sortedOutgoingTransaction =
                outgoingTransactions(Arrays.asList(treasury.history));
        if (sortedOutgoingTransaction.isEmpty()) {
            // first transaction
            if (amount.value > limit) {
                throw new AmountExceedsLimitException(limit, null);
            }
            else {
                return true;
            }
        } else {
            Date threshold = startingTime(sortedOutgoingTransaction);
            long nextTimeWindow = threshold.getTime() + allowedDifference;
            Date nextWindow = new Date(nextTimeWindow);
            long amountAccrued = amountAccrued(sortedOutgoingTransaction, threshold);
            if ((amount.value + amountAccrued) > limit) {
                throw new AmountExceedsLimitException(limit, nextWindow);
            }
            else {
                return true;
            }
        }
    }

    public void syncTreasury() throws IOException, ClientError {
        // NOTE this will contain all transactions that are directed towards this wallet
        List<Transaction> allIncoming = incomingTransactions();
        List<TransactionHistory> blockchainHistory = convertToTreasuryTransaction(allIncoming);
        List<TransactionHistory> treasuryHistory = convertToHistoryList(client.getTreasury().history);
        Iterator<TransactionHistory> it = blockchainHistory.iterator();
        while (it.hasNext()) {
            TransactionHistory item = it.next();
            // find this item in treasury history
            Iterator<TransactionHistory> tIt = treasuryHistory.iterator();
            boolean found = false;
            while (tIt.hasNext()) {
                TransactionHistory tItem = tIt.next();
                if (tItem.equals(item)) {
                    found = true;
                    break;
                }
            }
            // If not found then insert
            if (!found) {
                client.postTransaction(item);
            }
        }
    }

    private Date startingTime(List<TransactionHistory> outgoing) {
        TransactionHistory first = outgoing.get(0);

        long initialTime = first.created_on.getTime();
        long currentTime = new Date().getTime();

        long startOfWindow = initialTime;
        long endOfWindow = initialTime + allowedDifference;
        while(endOfWindow <= currentTime) {
            startOfWindow += allowedDifference;
            endOfWindow += allowedDifference;
        }

        return new Date(startOfWindow);
    }

    private long amountAccrued(List<TransactionHistory> sortedOutgoing, Date threshold) {
        long accrued = 0;
        Iterator<TransactionHistory> it = sortedOutgoing.iterator();
        while (it.hasNext()) {
            TransactionHistory tx = it.next();
            Date dateOfTx = tx.created_on;
            if (dateOfTx.before(threshold)) {
                break;
            } else {
                // outgoing are negative are negative
                accrued += (-tx.amount);
            }
        }
        return accrued;
    }

    // filters and sorts the transaction list
    private List<TransactionHistory> outgoingTransactions(List<TransactionHistory> all) {
        List<TransactionHistory> onlyOutgoing = new ArrayList<TransactionHistory>();
        Iterator<TransactionHistory> it = all.iterator();
        while (it.hasNext()) {
            TransactionHistory t = it.next();
            if (!t.incoming()) {
                onlyOutgoing.add(t);
            }
        }
        Collections.sort(onlyOutgoing, new TransactionHistoryByDate());
        return onlyOutgoing;
    }

    private List<TransactionHistory> convertToTreasuryTransaction(List<Transaction> txs) {
        List<TransactionHistory> tTxs = new ArrayList<TransactionHistory>();
        Iterator<Transaction> it = txs.iterator();
        while (it.hasNext()) {
            Transaction tx = it.next();
            Coin fee = tx.getFee();
            long feeValue = 0;
            if (fee != null) {
                feeValue = fee.value;
            }
            List<TransactionOutput> txOuts = tx.getOutputs();
            String tx_hash = tx.getHash().toString();
            Date updateTime = tx.getUpdateTime();
            Iterator<TransactionOutput> itOut = txOuts.iterator();
            while (itOut.hasNext()) {
                TransactionOutput out = itOut.next();
                if (out.isMine(wallet)) {
                    Coin amount = out.getValue();
                    // to address is 0 if destination address is mine
                    TransactionHistory tTx = new TransactionHistory(
                                "0", amount.value, updateTime, tx_hash, feeValue);
                    tTxs.add(tTx);
                }
            }
        }
        return tTxs;
    }

    private List<TransactionHistory> convertToHistoryList(TransactionHistory[] h) {
        List<TransactionHistory> list = new ArrayList<TransactionHistory>();
        for (int i = 0; i < h.length; i++) {
            TransactionHistory t = h[i];
            list.add(t);
        }
        return list;
    }

    private List<Transaction> incomingTransactions() {
        List<Transaction> all = wallet.getTransactionsByTime();
        List<Transaction> incomingTransactions = new ArrayList<Transaction>();
        Iterator<Transaction> itTx = all.iterator();
        while (itTx.hasNext()) {
            Transaction current = itTx.next();
            List<TransactionOutput> outputs = current.getOutputs();
            Iterator<TransactionOutput> itTxOut = outputs.iterator();
            boolean forMe = false;
            while (itTxOut.hasNext()) {
                TransactionOutput output = itTxOut.next();
                if (output.isMine(wallet)) {
                    forMe = true;
                }
            }
            if (forMe) {
                incomingTransactions.add(current);
            }
        }
        return incomingTransactions;
    }
}
