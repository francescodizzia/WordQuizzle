package com.dizzia.wordquizzle.client;

import com.dizzia.wordquizzle.commons.ByteBufferIO;
import com.dizzia.wordquizzle.commons.StatusCode;
import com.dizzia.wordquizzle.commons.WQSettings;

import javax.swing.*;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WQClient {
    static LoginFrame loginFrame;
    static HubFrame hubFrame;
    static SocketChannel server;
    static DatagramSocket datagramSocket;
    static int udp_port = -1;
    static int port = 1919;

    static boolean endgame = false;


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
            SocketAddress address = new InetSocketAddress(WQSettings.RMI_IP, port);
            server = SocketChannel.open(address);


            ByteBufferIO.writeString(server, "login " + username + " " + password + " " + udp_port);
            int login_result = ByteBufferIO.readInt(server);

            switch(login_result){
                case StatusCode.OK:
                    System.out.println("Login effettuato con successo!");
                    UDPListener receiver = new UDPListener(loginFrame, server, datagramSocket);
                    Thread t = new Thread(receiver);
                    t.start();
                    loginFrame.dispose();
                    hubFrame = new HubFrame(username);
                    break;
                case StatusCode.USER_NOT_FOUND:
                    System.out.println("Utente non trovato");
                    JOptionPane.showMessageDialog(loginFrame, "Utente non trovato!",
                            "Errore di login", JOptionPane.ERROR_MESSAGE);
                    break;
                case StatusCode.WRONG_PASSWORD:
                    System.out.println("Password sbagliata");
                    JOptionPane.showMessageDialog(loginFrame, "Password non corretta!",
                            "Errore di login", JOptionPane.ERROR_MESSAGE);
                    break;
                case StatusCode.USER_ALREADY_LOGGED:
                    System.out.println("L'utente risulta già connesso");
                    JOptionPane.showMessageDialog(loginFrame, "L'utente inserito risulta già connesso!",
                            "Errore di login", JOptionPane.ERROR_MESSAGE);
                    break;
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(loginFrame, "Impossibile connettersi al server!",
                    "Errore di login", JOptionPane.ERROR_MESSAGE);
        }


    }


    public static int aggiungi_amico(String nickAmico){
        writeString("ADD_FRIEND " + nickAmico);
        return readInt();
    }



    public static void main(String[] a) {

        try {
            datagramSocket = new DatagramSocket();
            System.out.println("Ascolto sulla porta " + datagramSocket.getLocalPort());
            udp_port = datagramSocket.getLocalPort();
        } catch (SocketException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        try {
            WQSettings.applyCustomTheme();
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | InstantiationException | IllegalAccessException e) {
            System.out.println(e);
        }


        loginFrame = new LoginFrame();

    }

    public static void inizio_sfida(String firstWord) {
        hubFrame.setVisible(false);
        new ChallengeFrame(firstWord);
    }


    public static void waitEnd(JDialog dialog) {
        endgame = true;

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
                    new ReportDialog(winner, correct_answers, wrong_answers);
                    hubFrame.setVisible(true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        })).start();


    }

    public static String lista_amici() {
        WQClient.writeString("friendlist");
        String jsonFriendlist = WQClient.readString();
        System.out.println(jsonFriendlist);
        return jsonFriendlist;
    }


    public static String classifica() {
        WQClient.writeString("leaderboard");
        String jsonLeaderboard = WQClient.readString();
        System.out.println(jsonLeaderboard);
        return jsonLeaderboard;
    }


}