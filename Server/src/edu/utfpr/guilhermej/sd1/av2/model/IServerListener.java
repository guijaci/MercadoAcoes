package edu.utfpr.guilhermej.sd1.av2.model;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServerListener<T> extends Remote{
    void accept(T event) throws RemoteException;
    boolean test(T event) throws RemoteException;
}
