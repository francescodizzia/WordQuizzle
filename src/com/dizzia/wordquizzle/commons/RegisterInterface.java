package com.dizzia.wordquizzle.commons;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegisterInterface extends Remote {
    int REG_PORT = 9998;
    int STUB_PORT = 9999;

    int registra_utente(String nickUtente, String password) throws RemoteException;
}
