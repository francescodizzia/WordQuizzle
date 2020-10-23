package com.dizzia.wordquizzle.commons;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegisterInterface extends Remote {
    public static final String MATRICOLA = "544107";
    public static final int REG_PORT = 9998;
    public static final int STUB_PORT = 9999;

    public int registerUser(String nickUtente, String password) throws RemoteException;
}
