package com.dizzia.wordquizzle;

import com.dizzia.wordquizzle.Exceptions.UserAlreadyTakenException;
import com.dizzia.wordquizzle.commons.ByteBufferIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class CLIent {
    static Registry registry;
    static RegisterInterface stub;

    public static void registra_utente(String nickUtente, String password) throws RemoteException, NotBoundException, UserAlreadyTakenException {
         registry = LocateRegistry.getRegistry(RegisterInterface.REG_PORT);
        stub = (RegisterInterface) registry.lookup("WordQuizzle_" + RegisterInterface.MATRICOLA);
        stub.registerUser(nickUtente, password);
    }


    public static void main(String[] args){
        int port = 1919;

        try {
            SocketAddress address = new InetSocketAddress("localhost", port);
            SocketChannel server = SocketChannel.open(address);

            Scanner s = new Scanner(System.in);
            int i = 0;
            while(true) {
                String string = s.nextLine();
                i++;

                if(string.equals("quit"))
                    break;

                String[] a = string.split(" ");
                if(a[0].equalsIgnoreCase("register")) {
                    registra_utente(a[1], a[2]);
                }
                ByteBufferIO.writeString(server, string);

                int result_code = ByteBufferIO.readInt(server);
                System.out.println(result_code);
            }

        } catch (IOException | NotBoundException | UserAlreadyTakenException ex) {
            ex.printStackTrace();
        }
    }

}
