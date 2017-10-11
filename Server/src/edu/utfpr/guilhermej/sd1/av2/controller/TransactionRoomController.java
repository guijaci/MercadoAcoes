package edu.utfpr.guilhermej.sd1.av2.controller;

import edu.utfpr.guilhermej.sd1.av2.model.ITransactionRoom;

public class TransactionRoomController {
    protected ITransactionRoom transactionRoom;

    public ITransactionRoom getTransactionRoom() {
        return transactionRoom;
    }

    public TransactionRoomController setTransactionRoom(ITransactionRoom transactionRoom) {
        this.transactionRoom = transactionRoom;
        return this;
    }
}
