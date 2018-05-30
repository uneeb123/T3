package org.treasury.core;

public class Treasury {
    public String _id;
    public String[] members;
    public String treasurer;
    public String created_by;
    public Integer spending_limit;
    public String[] shares;
    public TransactionHistory[] history; // probably wrong
    public boolean ready;
    public String[] addresses;

    Treasury(String _id,
             String[] members,
             String treasurer,
             Integer spending_limit,
             String[] shares,
             TransactionHistory[] history,
             boolean ready,
             String[] addresses) {
        this._id = _id;
        this.members = members;
        this.treasurer = treasurer;
        this.spending_limit = spending_limit;
        this.shares = shares;
        this.history = history;
        this.ready = ready;
        this.addresses = addresses;
    }
}