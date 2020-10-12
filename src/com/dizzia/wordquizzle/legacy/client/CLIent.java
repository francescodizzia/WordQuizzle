package com.dizzia.wordquizzle.legacy.client;


import com.dizzia.wordquizzle.RegisterInterface;
import com.dizzia.wordquizzle.commons.ByteBufferIO;
import com.dizzia.wordquizzle.commons.StatusCode;
import com.dizzia.wordquizzle.commons.exceptions.UserAlreadyTakenException;

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

    public static int registra_utente(String nickUtente, String password) throws RemoteException {
        registry = LocateRegistry.getRegistry(RegisterInterface.REG_PORT);
        try {
            stub = (RegisterInterface) registry.lookup("WordQuizzle_" + RegisterInterface.MATRICOLA);
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        return stub.registerUser(nickUtente, password);
    }


    public static void main(String[] args) throws RemoteException, NotBoundException, UserAlreadyTakenException {
        int port = 1919;
        String USERNAME = "";
        String PASSWORD = "";
        boolean logged = false;

        print("Benvenuto/a in WordQuizzle!\n");
        Scanner scanner = new Scanner(System.in);


        while (true) {
            String string = scanner.nextLine();

            if (string.equals("quit"))
                break;

            String[] a = string.split(" ");
            if (a[0].equalsIgnoreCase("register")) {
                if (registra_utente(a[1], a[2]) == StatusCode.OK) {
                    print("Registrazione effettuata con successo");
                    USERNAME = a[1];
                    PASSWORD = a[2];
                    break;
                } else {
                    print("RIP ROP"); //TODO
                }
            } else if (a[0].equalsIgnoreCase("login")) {
                USERNAME = a[1];
                PASSWORD = a[2];
                break;
            }

        }


        try {
            SocketAddress address = new InetSocketAddress("localhost", port);
            SocketChannel server = SocketChannel.open(address);

            //Tentativo di login
            ByteBufferIO.writeString(server, "login " + USERNAME + " " + PASSWORD);
            int login_result = ByteBufferIO.readInt(server);

            if (login_result == StatusCode.OK) {
                print("Login effettuato con successo!");
                logged = true;
            } else
                print("Login fallito, riprova!");


            //Command line interface start

            Scanner s = new Scanner(System.in);
            while (true) {
                String string = s.nextLine();
                String op = string.split(" ")[0];

                if (string.equals("quit"))
                    break;

                ByteBufferIO.writeString(server, string);


                if (string.equals("friendlist")) {
                    String a = ByteBufferIO.readString(server);
                    System.out.println(a);
                } else {
                    int result_code = ByteBufferIO.readInt(server);
                    System.out.println(result_code);
                    if(op.equals("sfida")) {
                        String a = ByteBufferIO.readString(server);
                        System.out.println(a);
                    }
                }
            }


            ByteBufferIO.writeString(server, "logout");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    private static void print(String string) {
        System.out.println(string);
    }

}
