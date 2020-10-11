package com.dizzia.wordquizzle.legacy;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class UDPSender {

    public static void main(String[] args) throws UnknownHostException, SocketException {
        DatagramSocket datagramSocket = new DatagramSocket();
        InetAddress receiverAddress = InetAddress.getLocalHost();
        byte[] buffer;

        while(true) {
            Scanner scanner = new Scanner(System.in);
            String message = scanner.nextLine();

            buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, receiverAddress, 9999);
            try {
                datagramSocket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}