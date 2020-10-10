package com.dizzia.wordquizzle.server;

import com.dizzia.wordquizzle.commons.ByteBufferIO;
import com.dizzia.wordquizzle.commons.StatusCode;
import com.dizzia.wordquizzle.database.Database;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServerHandler implements Runnable {
    private final Database database;
    private final ConcurrentHashMap<String, Boolean> loggedUsers;


    public ServerHandler(Database database) {
        loggedUsers = new ConcurrentHashMap<>();
        this.database = database;
    }


    public synchronized void serialize() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(database);
        //System.out.println("\nStringa json: " + jsonString + "\n");
        FileWriter file = new FileWriter("./backup.json");
        file.write(jsonString);
        file.close();
    }



    private void commandParser(String command, SocketChannel client, SelectionKey key) {
        String[] args = command.split(" ");
        ClientResources resources = (ClientResources) key.attachment();
        String CURRENT_USER = resources.getUsername();
        String COMMAND_NAME = args[0].toUpperCase();
        ByteBuffer buffer = resources.buffer;


        switch (COMMAND_NAME) {
            case "LOGIN":
                loggedUsers.put(args[1], true);
                System.out.println(loggedUsers.keySet());
                resources.setUsername(args[1]);
//                k_ID = new ClientResources(args[1]);
//                key.attach(k_ID);
                resources.buffer.clear();
                resources.buffer.putInt(StatusCode.OK);
                resources.buffer.flip();
                key.interestOps(SelectionKey.OP_WRITE);
                break;
            case "ADD_FRIEND":
                database.makeFriends(CURRENT_USER, args[1]);
                resources.buffer.clear();
                resources.buffer.putInt(StatusCode.OK);
                resources.buffer.flip();
                key.interestOps(SelectionKey.OP_WRITE);
                try {
                    serialize();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "FRIENDLIST":
                String list = database.getFriendList(CURRENT_USER);
                resources.buffer.clear();
                resources.buffer.put(list.getBytes());
                resources.buffer.flip();
                key.interestOps(SelectionKey.OP_WRITE);
                System.out.println("ciao");
                break;
            case "CHALLENGE":
                System.out.println("challenge TODO");
                break;
            case "PRINT":
                System.out.println("Online users: \t" + loggedUsers.keySet());
                resources.buffer.clear();
                resources.buffer.putInt(StatusCode.OK);
                resources.buffer.flip();
                database.updateScore(CURRENT_USER, 999);
                key.interestOps(SelectionKey.OP_WRITE);
                break;
            case "SCORE":
                resources.buffer.clear();
                resources.buffer.putInt(database.getScore(CURRENT_USER));
                resources.buffer.flip();
                key.interestOps(SelectionKey.OP_WRITE);
                break;
            case "LOGOUT":
                loggedUsers.remove(CURRENT_USER);
                System.out.println(CURRENT_USER+ " logged out");
                System.out.println(loggedUsers.keySet());
                key.interestOps(SelectionKey.OP_READ);
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
                        SelectionKey key2 = client.register(selector, SelectionKey.OP_READ);
                        ClientResources resources = new ClientResources();
                        key2.attach(resources);
                    }
                    else if(key.isReadable()){
                        ClientResources resources = (ClientResources) key.attachment();
                        String username = resources.getUsername();
                        System.out.println("[" + username + "] isReadable");
                        SocketChannel client = (SocketChannel) key.channel();

//                        String line = ByteBufferIO.readString(client);
                        ByteBuffer input = resources.buffer;
                        input.clear();
                        int read_byte = client.read(input);

//                        if(read_byte == -1)
//                            if(!id.equalsIgnoreCase("anonymous")) {
//                                loggedUsers.remove(id);
//                                System.out.println(id + " has logged out");
//                                System.out.println(loggedUsers.keySet());
//                            }

                        input.flip();
                        String line = StandardCharsets.UTF_8.decode(input).toString();

                        this.commandParser(line, client, key);
                        System.out.println("Ricevuto: " + line);
                    }
                    else if(key.isWritable()){
                        ClientResources k = (ClientResources) key.attachment();
                        String username = k.getUsername();
                        System.out.println("[" + username + "] isWriteable");
                        SocketChannel client = (SocketChannel) key.channel();
                        //client.configureBlocking(false);

                        ByteBuffer output = k.buffer;
//                        output.clear();
//                        output.putInt(StatusCode.OK);
//                        output.flip();
                        client.write(output);

                        //ByteBufferIO.writeInt(client, 200);
                        System.out.println("Mando come risposta: " + StatusCode.OK);

                        //if HA FINITO DI LEGGERE QUALCOSA SWITCHA A WRITE PER MANDARE LA RISPOSTA TODO
                        key.interestOps(SelectionKey.OP_READ);
                    }
                } catch (IOException ex) {
                    key.cancel();
                    try {
                        key.channel().close();
                        ClientResources resources = (ClientResources) key.attachment();

                        String username = resources.getUsername();
                        System.out.println("ADDIO " + username);
                        loggedUsers.remove(username);
                        System.out.println(loggedUsers.keySet());

                    } catch (IOException cex) {
                        cex.printStackTrace();
                    }
                }
            }


        }
    }

}

