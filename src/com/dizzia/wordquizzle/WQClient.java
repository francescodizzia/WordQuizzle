package com.dizzia.wordquizzle;

import com.dizzia.wordquizzle.Exceptions.UserAlreadyTakenException;
import com.dizzia.wordquizzle.commons.ByteBufferIO;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static com.dizzia.wordquizzle.WQInterface.TCP_PORT;

public class WQClient implements Runnable{
    static Registry registry;
    static RegisterInterface stub;

    private static String USERNAME;

    public static void registra_utente(String nickUtente, String password) throws RemoteException, NotBoundException, UserAlreadyTakenException {
        registry = LocateRegistry.getRegistry(RegisterInterface.REG_PORT);
        stub = (RegisterInterface) registry.lookup("WordQuizzle_" + RegisterInterface.MATRICOLA);
        stub.registra_utente_helper(nickUtente, password);
    }

    public void login(String username, String password){

    }



    public void run() {
        int port = 1919;
        double random = Math.random();

        try {
            SocketAddress address = new InetSocketAddress("localhost", port);
            SocketChannel server = SocketChannel.open(address);

            Scanner s = new Scanner(System.in);
       //     while(true) {
       //         String string = s.nextLine();
                //INVIO
//            ByteBuffer output = ByteBuffer.allocateDirect(64);
//            output.clear();
//            byte[] bytes = string.getBytes();
//            output.put(bytes);
//            output.flip();
//            server.write(output);
                ByteBufferIO.writeString(server, "LOGIN " + Thread.currentThread().getName() + " a");

                //RICEVO
//            ByteBuffer input = ByteBuffer.allocateDirect(4);
//            input.clear();
//            server.read(input);
//            input.flip();
//            System.out.println(input.getInt());
                int result_code = ByteBufferIO.readInt(server);
                System.out.println(result_code);
      //      }

        } catch (IOException ex) {
            ex.printStackTrace();
        }




    }
}