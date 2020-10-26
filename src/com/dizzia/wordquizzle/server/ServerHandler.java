package com.dizzia.wordquizzle.server;

import com.dizzia.wordquizzle.commons.ByteBufferIO;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ServerHandler implements Runnable {
    //Database con le informazioni sugli utenti
    public static Database database;

    //Tabella contenente gli utenti attualmente connessi
    public static ConcurrentHashMap<String, InetSocketAddress> loggedUsers;

    //Tabella che lega gli utenti alle proprie chiavi
    private final HashMap<String, SelectionKey> keyMap;

    //Oggetto che si occupa della scelta delle parole dal dizionario e delle traduzioni
    public static WQDictionary wqDictionary;

    //Selector principale
    public static Selector selector;

    //ThreadPool che verrà usato per le sfide
    private final ExecutorService executor = Executors.newCachedThreadPool();

    //DatagramSocket, per l'invio di messaggi UDP
    private DatagramSocket datagramSocket;


    public ServerHandler(Database database) {
        ServerHandler.database = database;
        loggedUsers = new ConcurrentHashMap<>();
        keyMap = new HashMap<>();
        wqDictionary = new WQDictionary();
    }


    //Serializza i dati degli utenti su un file JSON
    public static synchronized void serialize() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(database);
        FileWriter file = new FileWriter("./backup.json");
        file.write(jsonString);
        file.close();
    }


    //Invia un messaggio UDP con indirizzo e porta specificate
    private void sendUDP(InetSocketAddress address, int port, String message)  {
        byte[] buffer = message.getBytes(StandardCharsets.UTF_8);

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address.getAddress(), port);
        System.out.println(port);
        try {
            datagramSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Esegue il parsing dei comandi ricevuti dal client via TCP, esegue le operazioni richieste
    //e prepara i buffer contenenti le risposte da inviare ai client
    private void commandParser(String command, SocketChannel client, SelectionKey key) {
        String[] args = command.split(" ");
        ClientResources resources = (ClientResources) key.attachment();
        String CURRENT_USER = resources.username;
        String COMMAND_NAME = args[0].toUpperCase();

        System.out.println(command);

        switch (COMMAND_NAME) {
            case "LOGIN":
                int login_result = login(client, resources, key, args[1], args[2], args[3]);
                ByteBufferIO.prepareInt(resources.buffer, login_result);
                key.interestOps(SelectionKey.OP_WRITE);
                break;
            case "ADD_FRIEND":
                int add_friend_result = aggiungi_amico(CURRENT_USER, args[1]);
                ByteBufferIO.prepareInt(resources.buffer, add_friend_result);
                key.interestOps(SelectionKey.OP_WRITE);
                try {
                    serialize();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "FRIENDLIST":
                String friendList = lista_amici(CURRENT_USER);
                ByteBufferIO.prepareString(resources.buffer, friendList);
                key.interestOps(SelectionKey.OP_WRITE);
                break;
            case "LEADERBOARD":
                String leaderboard = mostra_classifica(CURRENT_USER);
                ByteBufferIO.prepareString(resources.buffer, leaderboard);
                key.interestOps(SelectionKey.OP_WRITE);
                break;
            case "CHALLENGE":
                sfida(resources, CURRENT_USER, args[1]);
                key.interestOps(SelectionKey.OP_WRITE);
                break;
            case "SCORE":
                int punteggio = mostra_punteggio(CURRENT_USER);
                ByteBufferIO.prepareInt(resources.buffer, punteggio);
                key.interestOps(SelectionKey.OP_WRITE);
                break;
            case "LOGOUT":
                handleDisconnect(key);
                key.interestOps(SelectionKey.OP_READ);
                break;
            case "ACCEPT":
                System.out.println(args[1]);
                SelectionKey challengerKey = keyMap.get(args[1]);

                //Calcola il tempo trascorso dall'invio della richiesta
                long elapsedTime = System.currentTimeMillis() - resources.challengeTime;


                //L'utente ha accettato in tempo, preparo il necessario per la sfida e lancio il
                //Challenge Handler
                if (elapsedTime < WQSettings.CHALLENGE_REQUEST_TIMEOUT) {
                    System.out.println("IN TEMPO");

                    //Sospendo le chiavi dei due utenti
                    key.interestOps(0);
                    challengerKey.interestOps(0);

                    ClientResources challengerResources = (ClientResources) challengerKey.attachment();

                    //Imposto il loro stato come 'BUSY', in modo tale da bloccare le richieste di sfida di
                    //altri utenti fintanto che sono in partita
                    challengerResources.isBusy = true;
                    resources.isBusy = true;

                    //Lancio un nuovo thread che gestirà la sfida
                    ChallengeHandler h = new ChallengeHandler(key, challengerKey, database);
                    Thread t = new Thread(h);
                    executor.execute(t);
                } else {
                    //Gestione TIMEOUT
                    System.out.println("TIMEOUT ");
                    ByteBufferIO.prepareString(resources.buffer, "TIMEOUT");
                    resources.challengeTime = 0;
                    key.interestOps(SelectionKey.OP_WRITE);
                }
                break;
            case "REFUSE":
                //Invio il messaggio di rifiuto allo sfidante
                SelectionKey challengerKey2 = keyMap.get(args[1]);
                ClientResources challengerResource2 = (ClientResources) challengerKey2.attachment();
                ByteBufferIO.prepareString(challengerResource2.buffer, StatusCode.REFUSED);
                challengerKey2.interestOps(SelectionKey.OP_WRITE);
                break;
        }
    }


    public void run() {
        ServerSocketChannel serverChannel;

        try {
            //Effettuo il binding TCP sulla porta specificata nelle impostazioni
            serverChannel = ServerSocketChannel.open();
            ServerSocket ss = serverChannel.socket();
            InetSocketAddress address = new InetSocketAddress(WQSettings.TCP_PORT);
            ss.bind(address);
            System.out.println("In ascolto sulla porta " + WQSettings.TCP_PORT);

            //Imposto il channel come non bloccante e lo registro sul selettore
            //in attesa di nuove connessioni
            serverChannel.configureBlocking(false);
            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            //Nuovo DatagramSocket, che verrà utilizzato per l'invio di messaggi UDP
            datagramSocket = new DatagramSocket();
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }


        //Classico ciclo da select
        while (!Thread.interrupted()) {
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
                    //Accetto nuove connessioni, imposto il canale come non bloccante,
                    //registro il client sul Selector e lo metto in attesa di lettura
                    //infine alloco le risorse necessarie al client (buffer e info
                    //varie)
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        System.out.println("Accettata connessione da " + client);
                        client.configureBlocking(false);
                        SelectionKey key2 = client.register(selector, SelectionKey.OP_READ);
                        ClientResources resources = new ClientResources();
                        System.out.println(client.getLocalAddress().toString());
                        key2.attach(resources);
                    }
                    //Ho qualcosa da leggere, dunque effettuo una lettura ed
                    //eseguo il parsing e la conseguente operazione richiesta
                    else if (key.isReadable()) {
                        ClientResources resources = (ClientResources) key.attachment();
                        SocketChannel client = (SocketChannel) key.channel();

                        ByteBuffer input = resources.buffer;
                        input.clear();
                        int read_bytes = client.read(input);
                        input.flip();

                        //Gestisco l'eventuale disconnessione del client
                        if (read_bytes == -1)
                            handleDisconnect(key);
                        else {
                            String line = StandardCharsets.UTF_8.decode(input).toString();
                            commandParser(line, client, key);
                        }
                    }
                    //Ho qualcosa da scrivere: recupero il buffer associato al client
                    //e scrivo nel canale il suo contenuto
                    else if (key.isWritable()) {
                        ClientResources k = (ClientResources) key.attachment();
                        SocketChannel client = (SocketChannel) key.channel();

                        ByteBuffer output = k.buffer;
                        int written_bytes = 0;

                        while (output.hasRemaining() && written_bytes != -1)
                            written_bytes = client.write(output);

                        //Gestisco l'eventuale disconnessione del client
                        if (written_bytes == -1)
                            handleDisconnect(key);
                        //Mi preparo per la prossima lettura
                        else
                            key.interestOps(SelectionKey.OP_READ);
                    }
                } catch (IOException ex) {
                    //In caso di problemi chiudo la connessione col client
                    handleDisconnect(key);
                }
            }
        }
        executor.shutdown();
    }


    //Gestisce la disconnessione di un utente
    private void handleDisconnect(SelectionKey key) {
        key.cancel();
        try {
            key.channel().close();
            ClientResources resources = (ClientResources) key.attachment();
            String username = resources.username;

            //Rimuovo l'utente disconnesso dalla lista di utenti connessi
            if (username != null) {
                System.out.println("Arrivederci " + username);
                loggedUsers.remove(username);
                System.out.println(loggedUsers.keySet());
            }

        } catch (IOException cex) {
            cex.printStackTrace();
        }
    }


    private int login(SocketChannel client, ClientResources resources, SelectionKey key, String username, String password, String udp_port) {
        //Verifico che le credenziali fornite siano corrette
        int check = database.checkCredentials(username, password);

        if(check == StatusCode.OK) {
            if (!loggedUsers.containsKey(username)) {
                loggedUsers.put(username, (InetSocketAddress) client.socket().getRemoteSocketAddress());
                System.out.println(loggedUsers.keySet());
                resources.username = username;
                keyMap.put(username, key);
                resources.udp_port = Integer.parseInt(udp_port);
                return StatusCode.OK;
            } else
                return StatusCode.USER_ALREADY_LOGGED;
        }

        return check;
}


    private int aggiungi_amico(String usernameA, String usernameB){
        return database.makeFriends(usernameA, usernameB);
    }



    private String lista_amici(String username){
        return database.getFriendList(username);
    }

    private int mostra_punteggio(String username) {
        return database.getScore(username);
    }

    private String mostra_classifica(String username) {
        return database.getLeaderboard(username);
    }

    private void sfida(ClientResources resources, String usernameA, String usernameB){
        //Controllo che l'utente sia online
        if (loggedUsers.containsKey(usernameB)) {
            ClientResources friend = (ClientResources) keyMap.get(usernameB).attachment();
            System.out.println("richiesta di sfida da " + usernameA + " [" + loggedUsers.get(usernameA).getAddress()
                    + ":" + resources.udp_port + "] a "
                    + usernameB + " [" + loggedUsers.get(usernameB).getAddress() + ":" + friend.udp_port + "]");
            //Controlla che l'utente da sfidare non sia già impegnato in un'altra partita
            if(!friend.isBusy) {
                //Imposto il tempo in cui l'amico riceve la richiesta di sfida
                friend.challengeTime = System.currentTimeMillis();
                //Mando la richiesta via UDP
                sendUDP(loggedUsers.get(usernameB), friend.udp_port, "challenge " + usernameA);
            }
            //Se l'utente è occupato restituisco il relativo codice
            else
                ByteBufferIO.prepareString(resources.buffer, StatusCode.BUSY_FRIEND);
        }
    }


}

