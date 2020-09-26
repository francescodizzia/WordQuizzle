package com.dizzia.wordquizzle;

import com.dizzia.wordquizzle.CLI.CLIServer;
import com.dizzia.wordquizzle.Exceptions.UserAlreadyTakenException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import static com.dizzia.wordquizzle.WQInterface.TCP_PORT;

public class WQServer implements RegisterInterface{
    public static UserPool database;

    public String registra_utente(String nickUtente, String password) throws RemoteException, UserAlreadyTakenException {
        return database.addUser(nickUtente, password);
    }

    public static void main(String[] args) throws IOException {
        database = new UserPool();
        CLIServer cmd = new CLIServer(database);
        Thread t = new Thread(cmd);
        t.setDaemon(false);
        t.start();

        try {
            WQServer obj = new WQServer();
            RegisterInterface stub = (RegisterInterface) UnicastRemoteObject.exportObject(obj, STUB_PORT);

            Registry registry = LocateRegistry.createRegistry(REG_PORT);
            registry.rebind("WQ" + MATRICOLA, stub);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        ServerSocket serverSocket = new ServerSocket(TCP_PORT);
        System.out.println("Listening for clients...");

        while(true) {
            Socket socket = serverSocket.accept();

            // read data from the client
            // send data to the client
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            writer.println("Hello world");
        }


    }
}
