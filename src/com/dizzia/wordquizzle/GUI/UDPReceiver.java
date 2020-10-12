package com.dizzia.wordquizzle.gui;

import com.dizzia.wordquizzle.commons.ByteBufferIO;

import javax.swing.*;
import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

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

                //String string = new String(buf,0, buf.length, StandardCharsets.UTF_8);
                String string = new String(buf, 0, packet.getLength());
                //String string = "sfida crash";
                if(string.compareToIgnoreCase("sfida crash") == 0){
                    System.out.println("MATCHMATCH");
                }

                packet.setLength(buf.length);
                System.out.println(string);
                int choice = JOptionPane.showConfirmDialog(frame, string, "Ping",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if(choice == JOptionPane.YES_OPTION){
                    //System.out.println(string.split(" ")[1].length());
                    String challenger = string.split(" ")[1];
                    ByteBufferIO.writeString(server, "ZIZIZI " + challenger);
                    String result_code = ByteBufferIO.readString(server);
                    System.out.println(result_code);
                }else{
                    ByteBufferIO.writeString(server, "NONONO");
                    int result = ByteBufferIO.readInt(server);
                    System.out.println(result);
                }




            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
