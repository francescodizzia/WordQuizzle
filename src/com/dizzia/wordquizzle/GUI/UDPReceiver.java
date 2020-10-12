package com.dizzia.wordquizzle.gui;

import com.dizzia.wordquizzle.commons.ByteBufferIO;

import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Scanner;

public class UDPReceiver implements Runnable {
    //private final static int PORT = 9999;
    JFrame frame;
    SocketChannel server;

    public UDPReceiver(JFrame frame, SocketChannel server){
        this.frame = frame;
        this.server = server;
    }

    public void run() {
        byte[] buf = new byte[256];

        try {
            System.out.println("Ascolto sulla porta " + server.socket().getPort());
            DatagramSocket socket = new DatagramSocket(server.socket().getRemoteSocketAddress());

            while(true){
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String string = new String(buf,0, buf.length);
                Arrays.fill(buf,(byte) 0);
                System.out.println(string);
                int choice = JOptionPane.showConfirmDialog(frame, string, "Ping",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if(choice == JOptionPane.YES_OPTION){
                    ByteBufferIO.writeString(server, "ZIZIZI");
                }else{
                    ByteBufferIO.writeString(server, "NONONO");
                }

                Scanner s = new Scanner(System.in);
                while(true) {
                String string2 = s.nextLine();

                ByteBufferIO.writeString(server, string2);

                int result_code = ByteBufferIO.readInt(server);
                System.out.println(result_code);
                }


            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
