package edu.utfpr.guilhermej.sd1.av2.model;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface para assinante remoto. Utilizado pelo cliente para receber eventos diversos
 * @param <T> tipo de evento retornado
 */
public interface ITransactionRoomListener<T> extends Remote{
    /**
     * Método callback do assinante. Executa caso filtro aceite evento
     * @param event evento recebido por publicador
     * @throws RemoteException em erros de conexão
     */
    void accept(T event) throws RemoteException;

    /**
     * Filtro de eventos. Retorna true caso evento passe pelo filtro, false caso contrário
     * @param event evento recebido por publicador
     * @return true caso evento passa pelo filtro, false caso contrário
     * @throws RemoteException em erros de conexão
     */
    boolean test(T event) throws RemoteException;
}
