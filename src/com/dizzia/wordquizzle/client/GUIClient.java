package com.dizzia.wordquizzle.client;

import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import com.dizzia.wordquizzle.commons.ByteBufferIO;
import com.dizzia.wordquizzle.commons.IO;
import com.dizzia.wordquizzle.commons.StatusCode;
import jdk.nashorn.internal.scripts.JD;

public class GUIClient {
    static LoginFrame loginFrame = new LoginFrame();
    static HubFrame hubFrame = new HubFrame();
    static SocketChannel server;
    static DatagramSocket datagramSocket;
    static int udp_port = -1;

    static int port = 1919;

    public static void hub(){
        UDPReceiver receiver = new UDPReceiver(loginFrame, server, datagramSocket);
        Thread t = new Thread(receiver);
        t.start();
    }


    public static void writeString(String message){
        try {
            ByteBufferIO.writeString(server, message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String readString(){
        String r = null;
        try {
           r = ByteBufferIO.readString(server);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return r;
    }


    public static int readInt(){
        int r=0;
        try {
            r = ByteBufferIO.readInt(server);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return r;
    }


    public static void login(String username, String password) {
        System.out.println(password);

        try {
            SocketAddress address = new InetSocketAddress("localhost", port);
            server = SocketChannel.open(address);


            ByteBufferIO.writeString(server, "login " + username + " " + password + " " + udp_port);
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

        hubFrame.setTitle("[" + username + "] WordQuizzle - Invita");
        hubFrame.setVisible(true);
        hubFrame.setBounds(10, 10, 450, 200);
        hubFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        hubFrame.setLocationRelativeTo(null);
        hubFrame.setResizable(false);

    }



    public static void main(String[] a) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {

        try {
            datagramSocket = new DatagramSocket();
            System.out.println("Ascolto sulla porta " + datagramSocket.getLocalPort());
            udp_port = datagramSocket.getLocalPort();
        } catch (SocketException e) {
            e.printStackTrace();
            System.exit(-1);
        }


        loginFrame.setTitle("WordQuizzle - Login");
        loginFrame.setVisible(true);
        loginFrame.setBounds(10, 10, 480, 360);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setResizable(false);

    }

    public static void inizio_sfida(String firstWord) {
        hubFrame.setVisible(false);
        ChallengeFrame f = new ChallengeFrame(firstWord);

    }

    public static void waitEnd(JDialog dialog) {
        (new Thread(() -> {

            try {
                String result = ByteBufferIO.readString(server);
                String[] results = result.split(" ");

                if(results[0].compareTo("FIN") == 0) {
                    int winner = Integer.parseInt(results[1]);
                    int correct_answers = Integer.parseInt(results[2]);
                    int wrong_answers = Integer.parseInt(results[3]);

                    dialog.dispose();

                    System.out.println(result);
                    ReportDialog reportDialog = new ReportDialog(winner, correct_answers, wrong_answers);
                    HubFrame hubFrame = new HubFrame();
                    hubFrame.setVisible(true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        })).start();


    }
}