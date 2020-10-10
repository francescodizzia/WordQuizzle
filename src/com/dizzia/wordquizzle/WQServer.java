package com.dizzia.wordquizzle;

import com.dizzia.wordquizzle.Exceptions.UserAlreadyTakenException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class WQServer implements RegisterInterface{
    static UserTable table;
    static UsersGraph graph;

    public void registra_utente_helper(String nickUtente, String password) throws RemoteException, UserAlreadyTakenException {
        graph.newUser(nickUtente, password);
    }

    public static void main(String[] args) throws IOException {
        table = new UserTable();
        graph = new UsersGraph(table);

        try {
            WQServer obj = new WQServer();
            RegisterInterface stub = (RegisterInterface) UnicastRemoteObject.exportObject(obj, STUB_PORT);

            Registry registry = LocateRegistry.createRegistry(REG_PORT);
            registry.rebind("WordQuizzle_" + MATRICOLA, stub);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        ServerHandler server = new ServerHandler(table);
        Thread thread = new Thread(server);
        thread.start();

        //System.setProperty("java.rmi.server.hostname","95.248.187.159");



    }

}
