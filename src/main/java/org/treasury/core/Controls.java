package org.treasury.core;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.wallet.Wallet;
import org.joda.time.DateTime;
import org.treasury.core.pojo.TransactionHistory;
import org.treasury.core.pojo.TransactionHistoryByDate;
import org.treasury.core.pojo.Treasury;

import java.util.*;

public class Controls {

    private TreasuryClient client;
    private Wallet wallet;
    private final long allowedDifference = 86400; // one day

    Controls(String treasuryId, Wallet wallet, boolean testMode) throws Exception {
        this.wallet = wallet;
        this.client = new TreasuryClient(testMode, treasuryId);
    }

    Controls(String treasuryId, Wallet wallet) throws Exception {
        this(treasuryId, wallet, true);
    }

    public void postTransaction(TransactionHistory history) throws Exception {
        int responseCode = client.postTransaction(history);
        if (responseCode != 200) {
            throw new Exception("Posting transaction failed");
        }
    }

    public void postAddress(String address) throws Exception {
        int responseCode = client.postAddress(address);
        if (responseCode != 200) {
            throw new Exception("Posting address failed");
        }
    }

    public boolean complyWithAccessControls(Coin amount) throws Exception {
        Treasury treasury = client.getTreasury();
        long limit = treasury.spending_limit;
        Date s = startingTime(outgoingTransactions(Arrays.asList(treasury.history)));
        if (amount.value > limit) {
            throw new AmountExceedsLimitException(limit);
        }
        TransactionHistory last = lastOutgoingTransaction(Arrays.asList(treasury.history));
        Date previous = last.created_on;
        Date today = new Date();
        long difference =  (today.getTime()/1000) - (previous.getTime()/1000);
        if (difference < allowedDifference) {
            return false;
        }
        return true;
    }

    public void syncTreasury() throws Exception {
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

    private TransactionHistory lastOutgoingTransaction(List<TransactionHistory> all) {
        List<TransactionHistory> onlyOutgoing = outgoingTransactions(all);
        return onlyOutgoing.get(onlyOutgoing.size() - 1);
    }

    private Date startingTime(List<TransactionHistory> outgoing) {
        TransactionHistory first = outgoing.get(0);

        DateTime dt = new DateTime(first.created_on.getTime());
        int firstHour = dt.hourOfDay().get();
        int firstMin = dt.minuteOfHour().get();
        int firstSec = dt.secondOfMinute().get();
        int firstTime = firstSec + (firstMin*60) + (firstHour*60*60);

        DateTime now = new DateTime();
        int nowYear = now.year().get();
        int nowMonth = now.monthOfYear().get();
        int nowDay = now.dayOfMonth().get();
        int nowHour = now.hourOfDay().get();
        int nowMin = now.minuteOfHour().get();
        int nowSec = now.secondOfMinute().get();
        int nowTime = nowSec + (nowMin*60) + (nowHour*60*60);

        // current time for the day exceeds first timestamp time of the day
        // calendar indexes months from 0
        if (firstTime < nowTime) {
            Calendar c = new GregorianCalendar(
                    nowYear, nowMonth-1, nowDay, firstHour, firstMin, firstSec);
            return c.getTime();
        } else {
            Calendar c = new GregorianCalendar(
                    nowYear, nowMonth-1, nowDay-1, firstHour, firstMin, firstSec);
            return c.getTime();
        }
    }

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
                            "0", amount.value, updateTime, tx_hash);
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
