package com.dizzia.wordquizzle.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class ChallengeHandler implements Runnable {
    private SelectionKey userKey;
    private SelectionKey sfidante;
    private Vector<String> chosenWords;
    Selector oldSelector;
    int N = 5;

    public ChallengeHandler(SelectionKey userKey, SelectionKey sfidante, WQDictionary wqDictionary, Selector oldSelector) {
        this.userKey = userKey;
        this.sfidante = sfidante;
        this.oldSelector = oldSelector;
        chosenWords = wqDictionary.getDistinctWords(N);
    }

    public void run() {
        System.out.println("---------------NUOVO SELECTOR----------");
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
                    System.out.println("Rimossa key " + key);

//                    if(!key.isValid())
//                        continue;
                    if (key.isReadable()) {
                        System.out.println("READABLE2");
                        ClientResources resources = (ClientResources) key.attachment();
                        String s = null;
                        String m = null;
                        if (resources != null) {
                            s = resources.getUsername();
                            ByteBuffer input = resources.buffer;
                            input.clear();
                            int read_byte = channel.read(input);
                            input.flip();
                            String line = StandardCharsets.UTF_8.decode(input).toString();
                            m = line;
                        }

                        System.out.println("Ciao fratello " + s);
                        System.out.println("S2 ho letto: " + m);


                        key.interestOps(SelectionKey.OP_WRITE);
                    }
                    else if (key.isWritable()) {
                        System.out.println("WRITABLE2");
                        SocketChannel client = (SocketChannel) key.channel();
                        ClientResources resources = (ClientResources) key.attachment();

                        if (resources.getTranslatedWords() >= N) {
                            key.interestOps(0);
                            client.register(oldSelector, SelectionKey.OP_READ);
                        }
                        else{

                            resources.buffer.clear();
                            resources.buffer.put(("Ciao bro " + resources.getUsername() + "\n" + chosenWords.get(resources.getTranslatedWords())).getBytes());
                            resources.buffer.flip();

                            client.write(resources.buffer);
                            System.out.println("Scrivo2: " + resources.buffer);
                            resources.incrementTranslatedWords();
                            key.interestOps(SelectionKey.OP_READ);
                    }
                    }

                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


//        try {
//            //Selector selector = Selector.open();
//            Selector selector = Selector.open();
//
//            channel.configureBlocking(false);
//
//            SelectionKey challengeKey = channel.register(selector, SelectionKey.OP_READ, userKey.attachment());
//
//            System.out.println("SECONDO: " + ((ClientResources)challengeKey.attachment()).getUsername());
//
//            while(true){
//                selector.select();
//                Iterator<SelectionKey> keysIterator = selector.selectedKeys().iterator();
//
//                while (keysIterator.hasNext()) {
//                    SelectionKey key = keysIterator.next();
//                    keysIterator.remove();
//                    System.out.println("Rimossa key " + key);
//
//                    if(!key.isValid())
//                        continue;
//                   if (key.isReadable()) {
//                        System.out.println("READABLE2");
//                        ClientResources res = (ClientResources) key.attachment();
//                        String s = null;
//                        if (res != null) {
//                            s = res.getUsername();
//                        }
//
//                        System.out.println("Ciao fratello " + s);
//                        //key.interestOps(SelectionKey.OP_WRITE);
//                    } else if (key.isWritable()) {
//                        System.out.println("WRITABLE2");
//                        //key.interestOps(SelectionKey.OP_READ);
//                    }
//
//                }
//            }
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
            }

}
