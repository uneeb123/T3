package org.treasury.core.model;

public class Treasury {
    public String _id;
    public String[] members;
    public String treasurer;
    public String created_by;
    public long spending_limit;
    public String[] shares;
    public TransactionHistory[] history; // probably wrong
    public long balance;
    public boolean ready;
    public String[] addresses;

    Treasury(String _id,
             String[] members,
             String treasurer,
             String created_by,
             long spending_limit,
             String[] shares,
             TransactionHistory[] history,
             boolean ready,
             String[] addresses) {
        this._id = _id;
        this.members = members;
        this.treasurer = treasurer;
        this.created_by = created_by;
        this.spending_limit = spending_limit;
        this.shares = shares;
        this.history = history;
        this.balance = balance;
        this.ready = ready;
        this.addresses = addresses;
    }
}