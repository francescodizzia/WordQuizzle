package com.dizzia.wordquizzle.server;

import com.dizzia.wordquizzle.database.Database;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Vector;

public class ChallengeHandler implements Runnable {
    private SelectionKey userKey;
    private SelectionKey sfidante;
    private Vector<String> chosenWords;
    Selector oldSelector;
    Database database;
    int N = 5;

    public ChallengeHandler(SelectionKey userKey, SelectionKey sfidante, WQDictionary wqDictionary, Selector oldSelector, Database database) {
        this.userKey = userKey;
        this.sfidante = sfidante;
        this.oldSelector = oldSelector;
        chosenWords = wqDictionary.getDistinctWords(N);
        this.database = database;
    }

    public void run() {
        SocketChannel channel = (SocketChannel) userKey.channel();
        SocketChannel channel2 = (SocketChannel) sfidante.channel();

        try {
            Selector selector = Selector.open();

            channel.configureBlocking(false);

            channel.register(selector, SelectionKey.OP_WRITE, userKey.attachment());
            channel2.register(selector, SelectionKey.OP_WRITE, sfidante.attachment());

            String player1 = ((ClientResources) userKey.attachment()).getUsername();
            String player2 = ((ClientResources) sfidante.attachment()).getUsername();

            while(true){
                selector.select();
                Iterator<SelectionKey> keysIterator = selector.selectedKeys().iterator();

                while (keysIterator.hasNext()) {
                    SelectionKey key = keysIterator.next();
                    keysIterator.remove();

                    if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ClientResources resources = (ClientResources) key.attachment();
                        String s = null;
                        String m = null;
                        if (resources != null) {
                            s = resources.getUsername();
                            ByteBuffer input = resources.buffer;
                            input.clear();
                            int read_byte = client.read(input);
                            input.flip();
                            m = StandardCharsets.UTF_8.decode(input).toString();
                            if(WQDictionary.getTranslatedWords(chosenWords.get(resources.getTranslatedWords()-1)).contains(m)) {
                                System.out.println("[" + s + "] Traduzione della parola #" + (resources.getTranslatedWords()) + " CORRETTA (+2)");
                                resources.score += 2;
                                database.updateScore(resources.getUsername(), database.getScore(resources.getUsername()) + resources.score);
                                ServerHandler.serialize();
                            }
                            else {
                                System.out.println("[" + s + "] Traduzione della parola #" + (resources.getTranslatedWords()) + " SBAGLIATA (-1)");
                                resources.score--;
                                database.updateScore(resources.getUsername(), database.getScore(resources.getUsername()) + resources.score);
                                ServerHandler.serialize();
                            }
                        }


                        key.interestOps(SelectionKey.OP_WRITE);
                    }
                    else if (key.isWritable()) {
//                        System.out.println("WRITABLE2");
                        SocketChannel client = (SocketChannel) key.channel();
                        ClientResources resources = (ClientResources) key.attachment();

                        if (resources.getTranslatedWords() >= N) {
                            resources.buffer.clear();
                            resources.buffer.put("FINE".getBytes());
                            resources.buffer.flip();
                            client.write(resources.buffer);

                            System.out.println("PLAYER " + resources.getUsername() + " HAI COMPLETATO LA SFIDA");
                            System.out.println("Punti totalizzati: " + resources.score);
//                            database.updateScore(resources.getUsername(), database.getScore(resources.getUsername()) + resources.score);
//                            ServerHandler.serialize();

                            key.interestOps(0);
                            client.register(oldSelector, SelectionKey.OP_READ, key.attachment());
                            oldSelector.wakeup();
                        }
                        else{
                            resources.buffer.clear();
                            resources.buffer.put((chosenWords.get(resources.getTranslatedWords())).getBytes());
                            resources.buffer.flip();

                            client.write(resources.buffer);

                            resources.incrementTranslatedWords();
                            key.interestOps(SelectionKey.OP_READ);
                    }
                    }

                }
            }
        }

        catch (IOException e) {
            System.out.println("GREVE ZIO SEI USCITO");

            //e.printStackTrace();
        }

    }

}
