package com.dizzia.wordquizzle;

import com.dizzia.wordquizzle.Exceptions.UserAlreadyTakenException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.util.Scanner;

import static com.dizzia.wordquizzle.CLI.CLIHandler.executeCMD;
import static com.dizzia.wordquizzle.WQInterface.TCP_PORT;

public class WQClient {

    public static void main(String[] args) throws IOException {

        try {
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            executeCMD(input);
        } catch (NotBoundException | UserAlreadyTakenException e) {
            e.printStackTrace();
        }
        System.out.println("Connecting to the server...");
        try (Socket socket = new Socket("localhost", TCP_PORT)) {

            InputStream input = socket.getInputStream();


            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            String line = reader.readLine();
            System.out.println(line);
        }
    }

}