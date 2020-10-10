package com.dizzia.wordquizzle;

import com.dizzia.wordquizzle.commons.ByteBufferIO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServerHandler implements Runnable {
    private UserTable table;
    private ConcurrentHashMap<String, Boolean> loggedUsers;

    public ServerHandler(UserTable table) {
        this.table = table;
        loggedUsers = new ConcurrentHashMap<>();

        try {
            deserialize();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void serialize() throws IOException {
        Gson gson = new Gson();
        String jsonString = gson.toJson(loggedUsers);
        System.out.println("\nStringa json: " + jsonString + "\n");
        FileWriter file = new FileWriter("./backup.json");
        file.write(jsonString);
        file.close();
    }

    private void deserialize() throws FileNotFoundException {
        FileReader file = new FileReader("./backup.json");
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(file);
        loggedUsers = gson.fromJson(reader, new TypeToken<ConcurrentHashMap<String, Boolean>>(){}.getType());
    }



    private void commandParser(String command) {
        String[] args = command.split(" ");
        String COMMAND_NAME = args[0];

        switch (COMMAND_NAME) {
            case "LOGIN":
                System.out.println("login preso");
                loggedUsers.put(args[1], true);
                try {
                    serialize();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "ADD_FRIEND":
                System.out.println("add_friend TODO");
                break;
            case "FRIENDLIST":
                System.out.println("friendlist TODO");
                break;
            case "CHALLENGE":
                System.out.println("challenge TODO");
                break;
            case "PRINT":
                System.out.println("Online users: \t" + loggedUsers.keySet());
                break;
        }
    }

    public void run() {
        int port = 1919;
        System.out.println("Listening for connections on port " + port);

        ServerSocketChannel serverChannel;
        Selector selector;
        try {
            serverChannel = ServerSocketChannel.open();
            ServerSocket ss = serverChannel.socket();
            InetSocketAddress address = new InetSocketAddress(port);
            ss.bind(address);
            serverChannel.configureBlocking(false);
            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        while (true) {
            try {
                selector.select();
            } catch (IOException ex) {
                ex.printStackTrace();
                break;
            }
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                try {
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        System.out.println("Accepted connection from " + client);
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                    }
                    else if(key.isReadable()){
                        SocketChannel client = (SocketChannel) key.channel();
                        /*ByteBuffer input = ByteBuffer.allocate(8);
                        input.clear();
                        client.read(input);
                        input.flip();
                        System.out.println("Ricevuto: " + input.getDouble());
                        */

                        /*
                        ByteBuffer input = ByteBuffer.allocate(16);
                        input.clear();
                        client.read(input);
                        input.flip();
                        String result = StandardCharsets.UTF_8.decode(input).toString();
                        */
                        String result = ByteBufferIO.readString(client);
                        this.commandParser(result);
                        System.out.println("Ricevuto: " + result);


                        key.interestOps(SelectionKey.OP_WRITE);
                    }
                    else if(key.isWritable()){
                        SocketChannel client = (SocketChannel) key.channel();
//                        ByteBuffer output = ByteBuffer.allocate(4);
//                        output.clear();
//                        output.putInt(200);
//                        output.flip();

//                        if (!output.hasRemaining()) {
//                            output.rewind();
//                            int value = output.getInt();
//                            output.clear();
//                            output.putInt(value + 1);
//                            output.flip();
//                        }

//                        client.write(output);
//                        output.rewind();

                        ByteBufferIO.writeInt(client, 200);
                        System.out.println("Mando come risposta: " + 200);
                        key.interestOps(SelectionKey.OP_READ);
                    }
                } catch (IOException ex) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException cex) {
                        cex.printStackTrace();
                    }
                }
            }


        }
    }
}

