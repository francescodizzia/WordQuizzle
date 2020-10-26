package com.dizzia.wordquizzle.client;

import com.dizzia.wordquizzle.commons.ByteBufferIO;

import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.channels.SocketChannel;

public class UDPListener implements Runnable {
    JFrame frame;
    SocketChannel server;
    DatagramSocket datagramSocket;

    public UDPListener(JFrame frame, SocketChannel server, DatagramSocket datagramSocket){
        this.frame = frame;
        this.server = server;
        this.datagramSocket = datagramSocket;
    }

    //Il thread si mette in attesa della richiesta UDP e alla ricezione mostra una finestra per
    //che permette di accettare o rifiutare la sfida
    public void run() {
        byte[] buf = new byte[256];

        try {
            System.out.println("Sono nel thread UDPReceiver");

            while(!Thread.interrupted()){
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                datagramSocket.receive(packet);

                String string = new String(buf, 0, packet.getLength());

                packet.setLength(buf.length);
                System.out.println(string);
                String challenger = string.split(" ")[1];


                int choice = JOptionPane.showConfirmDialog(frame, "Hai ricevuto una sfida da " + challenger +
                                "\nAccetti l'invito?", "Notifica di sfida",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if(choice == JOptionPane.YES_OPTION){
                    ByteBufferIO.writeString(server, "ACCEPT " + challenger);
                    String response = ByteBufferIO.readString(server);
                    if (response.compareTo("TIMEOUT") == 0)
                        JOptionPane.showMessageDialog(frame, "Tempo scaduto per l'accettazione della richiesta!",
                                "Errore", JOptionPane.ERROR_MESSAGE);
                    else
                        WQClient.inizio_sfida(response);
                }else{
                    ByteBufferIO.writeString(server, "REFUSE " + challenger);
                }


            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
