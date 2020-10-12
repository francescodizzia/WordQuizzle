package com.dizzia.wordquizzle.legacy.client;

import com.dizzia.wordquizzle.commons.exceptions.UserAlreadyTakenException;
import com.dizzia.wordquizzle.RegisterInterface;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class WQClient implements Runnable{
    static Registry registry;
    static RegisterInterface stub;

    private static String USERNAME;

    public static void registra_utente(String nickUtente, String password) throws RemoteException, NotBoundException, UserAlreadyTakenException {
        registry = LocateRegistry.getRegistry(RegisterInterface.REG_PORT);
        stub = (RegisterInterface) registry.lookup("WordQuizzle_" + RegisterInterface.MATRICOLA);
        stub.registerUser(nickUtente, password);
    }


    public void run() {
        int port = 1919;
        int m = (int) (Math.random() * 1000);
        try {
            registra_utente(Thread.currentThread().getName(), "pass_" + m);
        } catch (RemoteException | UserAlreadyTakenException | NotBoundException e) {
            e.printStackTrace();
        }

        try {
            SocketAddress address = new InetSocketAddress("localhost", port);
            SocketChannel server = SocketChannel.open(address);

//            Scanner s = new Scanner(System.in);
//            while(true) {
//                String string = s.nextLine();
//
//                ByteBufferIO.writeString(server, string);
//
//                int result_code = ByteBufferIO.readInt(server);
//                System.out.println(result_code);
//            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }




    }
}