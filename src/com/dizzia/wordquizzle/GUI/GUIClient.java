package com.dizzia.wordquizzle.gui;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import com.dizzia.wordquizzle.commons.ByteBufferIO;
import com.dizzia.wordquizzle.commons.StatusCode;
import sun.plugin2.os.windows.Windows;


public class GUIClient {
    static LoginFrame loginFrame = new LoginFrame();
    static SocketChannel server;

    static int port = 1919;

    public static void hub(){
        UDPReceiver receiver = new UDPReceiver(loginFrame, server);
        Thread t = new Thread(receiver);
        t.start();
    }

    public static void login(String username, String password) {
        System.out.println(password);

        try {
            SocketAddress address = new InetSocketAddress("localhost", port);
            server = SocketChannel.open(address);


            ByteBufferIO.writeString(server, "login " + username + " " + password);
            int login_result = ByteBufferIO.readInt(server);

            switch(login_result){
                case StatusCode.OK:
                    System.out.println("Login effettuato con successo!");
                    hub();
                    break;
                case StatusCode.USER_NOT_FOUND:
                    System.out.println("Utente non trovato");
                    break;
                case StatusCode.WRONG_PASSWORD:
                    System.out.println("Password sbagliata");
                    break;
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        loginFrame.setVisible(false);
        loginFrame.dispose();


    }



    public static void main(String[] a) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        loginFrame.setTitle("WordQuizzle - Login");
        loginFrame.setVisible(true);
        loginFrame.setBounds(10, 10, 480, 360);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setResizable(false);

    }
}