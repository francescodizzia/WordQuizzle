package com.dizzia.wordquizzle.server;

import com.dizzia.wordquizzle.commons.IO;
import com.dizzia.wordquizzle.database.Database;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class ChallengeHandler implements Runnable {
    private final SelectionKey player1Key;
    private final SelectionKey player2Key;
    private final Vector<String> chosenWords;
    Selector oldSelector;
    Database database;
    int N = 5;
    int TIME_LIMIT = 60 * 1;
    int finished = 0;


    public ChallengeHandler(SelectionKey player1Key, SelectionKey player2Key, WQDictionary wqDictionary, Selector oldSelector, Database database) {
        this.player1Key = player1Key;
        this.player2Key = player2Key;
        this.oldSelector = oldSelector;
        chosenWords = wqDictionary.getDistinctWords(N);
        this.database = database;
    }


    public void handleEndGame(SelectionKey key){
        ClientResources P1_R = (ClientResources) player1Key.attachment();
        ClientResources P2_R = (ClientResources) player2Key.attachment();

        SocketChannel socket1 = (SocketChannel) player1Key.channel();
        SocketChannel socket2 = (SocketChannel) player2Key.channel();

        finished++;

        if(finished == 2){
            int winner;

            if(P1_R.score > P2_R.score)
                winner = 1;
            else if(P1_R.score < P2_R.score)
                winner = 2;
            else
                winner = 0;



            try {
                IO.writeString(socket1, P1_R.buffer, "FIN " + winner + " " + P1_R.correct_answers +
                        " " + P1_R.wrong_answers + " " + (N - P1_R.translatedWords));

                IO.writeString(socket2, P2_R.buffer, "FIN " + winner + " " + P2_R.correct_answers +
                        " " + P2_R.wrong_answers + " " + (N - P2_R.translatedWords));

                player1Key.interestOps(0);
                player2Key.interestOps(0);
                socket1.register(oldSelector, SelectionKey.OP_READ, player1Key.attachment());
                socket2.register(oldSelector, SelectionKey.OP_READ, player2Key.attachment());
                oldSelector.wakeup();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            key.interestOps(0);
        }



    }



    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ClientResources resources = (ClientResources) key.attachment();
        String s = null;
        String m = null;
        if (resources != null) {
            s = resources.getUsername();
            IO.read(client, resources.buffer);

            m = StandardCharsets.UTF_8.decode(resources.buffer).toString();
            if(WQDictionary.getTranslatedWords(chosenWords.get(resources.translatedWords-1)).contains(m)) {
                System.out.println("[" + s + "] Traduzione della parola #" + (resources.translatedWords) + " CORRETTA (+2)");
                resources.score += 2;
                resources.correct_answers++;
                database.updateScore(resources.getUsername(), database.getScore(resources.getUsername()) + resources.score);
                ServerHandler.serialize();
            }
            else {
                System.out.println("[" + s + "] Traduzione della parola #" + (resources.translatedWords) + " SBAGLIATA (-1)");
                resources.score--;
                resources.wrong_answers++;
                database.updateScore(resources.getUsername(), database.getScore(resources.getUsername()) + resources.score);
                ServerHandler.serialize();
            }
        }

        key.interestOps(SelectionKey.OP_WRITE);
    }





    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ClientResources resources = (ClientResources) key.attachment();

        if (resources.translatedWords >= N) {
            IO.writeString(client, resources.buffer, "FINE");

//            System.out.println("PLAYER " + resources.getUsername() + " HAI COMPLETATO LA SFIDA");
//            System.out.println("Punti totalizzati: " + resources.score);

            handleEndGame(key);

//            key.interestOps(0);
//            client.register(oldSelector, SelectionKey.OP_READ, key.attachment());
//            oldSelector.wakeup();
        }
        else{
            IO.writeString(client, resources.buffer, chosenWords.get(resources.translatedWords));
            resources.translatedWords++;
            key.interestOps(SelectionKey.OP_READ);
        }
    }




    public void run() {
        SocketChannel player1 = (SocketChannel) player1Key.channel();
        SocketChannel player2 = (SocketChannel) player2Key.channel();
        long startTime = System.nanoTime();

        try {
            Selector selector = Selector.open();
            player1.register(selector, SelectionKey.OP_WRITE, player1Key.attachment());
            player2.register(selector, SelectionKey.OP_WRITE, player2Key.attachment());


            while(!Thread.interrupted()){
                int S = selector.select(200);
                long actualTime = System.nanoTime();
                long elapsedTime = TimeUnit.SECONDS.convert(actualTime - startTime, TimeUnit.NANOSECONDS);
//                System.out.println(elapsedTime);

                if(elapsedTime >= TIME_LIMIT){
                    System.out.println("Sono passati " + TIME_LIMIT + " secondi, addiooo");
                    break;
                }

                if(finished == 2)
                    break;


                Iterator<SelectionKey> keysIterator = selector.selectedKeys().iterator();

                while (keysIterator.hasNext()) {
                    SelectionKey key = keysIterator.next();
                    keysIterator.remove();

                    if (key.isReadable())
                        handleRead(key);
                    else if (key.isWritable())
                        handleWrite(key);

                }
            }
        }

        catch (IOException e) {
            System.out.println("GREVE ZIO SEI USCITO (1)");

        }

        System.out.println("GREVE ZIO HAI FINITO (2)");
    }

}
