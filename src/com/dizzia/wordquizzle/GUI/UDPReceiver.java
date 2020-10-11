package com.dizzia.wordquizzle.gui;

import com.dizzia.wordquizzle.commons.ByteBufferIO;

import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

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
                System.out.println("Ho ricevuto: " + string);
                JOptionPane.showMessageDialog(frame, string, "Ping" , JOptionPane.INFORMATION_MESSAGE);
                Arrays.fill(buf,(byte) 0);

                try {
                    ByteBufferIO.writeString(server, "GREEEVE ZIOOO");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
