package com.dizzia.wordquizzle.server;

import com.dizzia.wordquizzle.commons.StatusCode;
import com.dizzia.wordquizzle.commons.WQSettings;
import com.dizzia.wordquizzle.database.Database;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class ServerHandler implements Runnable {
    public static Database database;
    public static ConcurrentHashMap<String, InetSocketAddress> loggedUsers;
    private final ConcurrentHashMap<String, SelectionKey> keyMap;
    public static WQDictionary wqDictionary;
    public static Selector selector;


    public ServerHandler(Database database) {
        loggedUsers = new ConcurrentHashMap<>();
        ServerHandler.database = database;
        keyMap = new ConcurrentHashMap<>();
        wqDictionary = new WQDictionary();
    }


    public static synchronized void serialize() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(database);
        FileWriter file = new FileWriter("./backup.json");
        file.write(jsonString);
        file.close();
    }




    private void sendUDP(InetSocketAddress address, int port, String message) throws SocketException {
        DatagramSocket datagramSocket = new DatagramSocket();
        byte[] buffer = message.getBytes(StandardCharsets.UTF_8);

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address.getAddress(), port);
        System.out.println(port);
        try {
            datagramSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void commandParser(String command, SocketChannel client, SelectionKey key) {
        String[] args = command.split(" ");
        ClientResources resources = (ClientResources) key.attachment();
        String CURRENT_USER = resources.getUsername();
        String COMMAND_NAME = args[0].toUpperCase();

        System.out.println(Arrays.toString(args));

        switch (COMMAND_NAME) {
            case "LOGIN":
                int login_result = database.checkCredentials(args[1], args[2]);

                if (login_result == StatusCode.OK) {
                    if(!loggedUsers.containsKey(args[1])) {
                        //TODO
                        loggedUsers.put(args[1], (InetSocketAddress) client.socket().getLocalSocketAddress());
                        System.out.println(loggedUsers.keySet());
                        resources.setUsername(args[1]);
                        keyMap.put(args[1], key);
                        resources.port = Integer.parseInt(args[3]);
                    }else{
                        login_result = StatusCode.USER_ALREADY_LOGGED;
                    }
                }
                resources.buffer.clear();
                resources.buffer.putInt(login_result);
                resources.buffer.flip();

                key.interestOps(SelectionKey.OP_WRITE);
                break;
            case "ADD_FRIEND":
                int result = database.makeFriends(CURRENT_USER, args[1]);
                resources.buffer.clear();
                resources.buffer.putInt(result);
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
                break;
            case "LEADERBOARD":
                String leaderboard = database.getLeaderboard(CURRENT_USER);
                resources.buffer.clear();
                resources.buffer.put(leaderboard.getBytes());
                resources.buffer.flip();
                key.interestOps(SelectionKey.OP_WRITE);
                break;
            case "CHALLENGE":
                if (loggedUsers.containsKey(args[1])){
                    System.out.println("richiesta di sfida da " + CURRENT_USER + " [" + loggedUsers.get(CURRENT_USER) + "] a "
                                + args[1] + " [" + loggedUsers.get(args[1]) + "]");
                    try {
                        ClientResources friend = (ClientResources) keyMap.get(args[1]).attachment();
                        friend.challengeTime = System.currentTimeMillis();
                        sendUDP(loggedUsers.get(args[1]), friend.getUDP_port(), "challenge " + CURRENT_USER);
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                }

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
                System.out.println(CURRENT_USER + " logged out");
                System.out.println(loggedUsers.keySet());
                key.interestOps(SelectionKey.OP_READ);
                break;
            case "ACCEPT":
                System.out.println(args[1]);
                SelectionKey challengerKey = keyMap.get(args[1]);

                long elapsedTime = System.currentTimeMillis() - resources.challengeTime;

                if(elapsedTime < WQSettings.CHALLENGE_REQUEST_TIMEOUT) {
                    System.out.println("IN TEMPO");
                    key.interestOps(0);
                    challengerKey.interestOps(0);

                    ChallengeHandler h = new ChallengeHandler(key, challengerKey);
                    Thread t = new Thread(h);
                    t.start();
                }
                else {
                    System.out.println("TIMEOUT ");
//                    challengerResources.buffer.clear();
//                    challengerResources.buffer.put("TIMEOUT".getBytes());
//                    challengerResources.buffer.flip();
                    resources.buffer.clear();
                    resources.buffer.put("TIMEOUT".getBytes());
                    resources.challengeTime = 0;
                    resources.buffer.flip();
                    key.interestOps(SelectionKey.OP_WRITE);
//                    challengerKey.interestOps(SelectionKey.OP_WRITE);
                }
                break;
            case "REFUSE":
                SelectionKey challengerKey2 = keyMap.get(args[1]);
                ClientResources challengerResource2 =  (ClientResources) challengerKey2.attachment();
                challengerResource2.buffer.clear();
                challengerResource2.buffer.put("REFUSED".getBytes());
                challengerResource2.buffer.flip();
                challengerKey2.interestOps(SelectionKey.OP_WRITE);
                break;
        }
    }



    public void run() {
        //TODO
        int port = 1919;
        System.out.println("In ascolto sulla porta " + port);
        ServerSocketChannel serverChannel;

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
//            return;
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
                        System.out.println(client.getLocalAddress().toString());
                        key2.attach(resources);
                    }
                    else if(key.isReadable()){
                        ClientResources resources = (ClientResources) key.attachment();
                        SocketChannel client = (SocketChannel) key.channel();

                        ByteBuffer input = resources.buffer;
                        input.clear();
                        int read_byte = client.read(input);
                        input.flip();

                        if(read_byte == -1) {
                            handleDisconnect(key);
                        }else {
                            String line = StandardCharsets.UTF_8.decode(input).toString();
                            commandParser(line, client, key);
                        }
                    }
                    else if(key.isWritable()){
                        ClientResources k = (ClientResources) key.attachment();
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer output = k.buffer;
                        client.write(output);

                        key.interestOps(SelectionKey.OP_READ);
                    }
                } catch (IOException ex) {
                    handleDisconnect(key);
                }
            }


        }
    }


    private void handleDisconnect(SelectionKey key){
        key.cancel();
        try {
            key.channel().close();
            ClientResources resources = (ClientResources) key.attachment();

            String username = resources.getUsername();
            if(username != null) {
                System.out.println("ADDIO " + username);
                loggedUsers.remove(username);
                System.out.println(loggedUsers.keySet());
            }

        } catch (IOException cex) {
            cex.printStackTrace();
        }
    }



}

