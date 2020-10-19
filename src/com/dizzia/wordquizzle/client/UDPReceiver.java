package com.dizzia.wordquizzle.client;

import com.dizzia.wordquizzle.commons.ByteBufferIO;

import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.channels.SocketChannel;

public class UDPReceiver implements Runnable {
    JFrame frame;
    SocketChannel server;
    DatagramSocket datagramSocket;

    public UDPReceiver(JFrame frame, SocketChannel server, DatagramSocket datagramSocket){
        this.frame = frame;
        this.server = server;
        this.datagramSocket = datagramSocket;
    }

    public void run() {
        byte[] buf = new byte[256];

        try {
            System.out.println("Sono nel thread UDPReceiver");

            while(true){
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                datagramSocket.receive(packet);

                String string = new String(buf, 0, packet.getLength());
                if(string.compareToIgnoreCase("sfida crash") == 0)
                    System.out.println("MATCHMATCH");


                packet.setLength(buf.length);
                System.out.println(string);
                String challenger = string.split(" ")[1];


                int choice = JOptionPane.showConfirmDialog(frame, "Hai ricevuto una sfida da " + challenger +
                                "\nAccetti l'invito?", "Notifica di sfida",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if(choice == JOptionPane.YES_OPTION){
                    ByteBufferIO.writeString(server, "ZIZIZI " + challenger);
                    String firstWord = ByteBufferIO.readString(server);
                    WQClient.inizio_sfida(firstWord);
                }else{
                    ByteBufferIO.writeString(server, "NONONO " + challenger);
                }


            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
