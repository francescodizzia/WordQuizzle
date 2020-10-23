package com.dizzia.wordquizzle.server;

import com.dizzia.wordquizzle.commons.IO;
import com.dizzia.wordquizzle.commons.WQSettings;
import com.dizzia.wordquizzle.database.Database;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Vector;

public class ChallengeHandler implements Runnable {
    private final SelectionKey player1Key;
    private final SelectionKey player2Key;
    private final Vector<String> chosenWords;
    Selector oldSelector;
    Database database;

    int finished = 0;
    int N = WQSettings.N_WORDS;


    public ChallengeHandler(SelectionKey player1Key, SelectionKey player2Key, WQDictionary wqDictionary, Selector oldSelector, Database database) {
        this.database = database;
        this.player1Key = player1Key;
        this.player2Key = player2Key;
        this.oldSelector = oldSelector;
        chosenWords = wqDictionary.getDistinctWords(N);
    }


    public void run() {
        SocketChannel player1 = (SocketChannel) player1Key.channel();
        SocketChannel player2 = (SocketChannel) player2Key.channel();
        long startTime = System.currentTimeMillis();

        try {
            Selector selector = Selector.open();
            player1.register(selector, SelectionKey.OP_WRITE, player1Key.attachment());
            player2.register(selector, SelectionKey.OP_WRITE, player2Key.attachment());


            while(!Thread.interrupted()){
                selector.select(200);

                long elapsedTime = System.currentTimeMillis() - startTime;
                if(elapsedTime >= WQSettings.CHALLENGE_TIMEOUT){
                    handleTimeout();
                    System.out.println("Sono passati " + WQSettings.CHALLENGE_TIMEOUT + " secondi, addiooo");
                    break;
                }

//                if(finished == 2)
//                    break;


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
            handleDisconnect();
        }

        System.out.println("FINE DELLA SFIDA2");
    }

    private void handleDisconnect() {
        System.out.println("Un giocatore si è disconnesso, termino la partita.");
    }

    private void handleTimeout() {
        System.out.println("Effettivo timeout");
        try {
            handleEndReport();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void handleEndReport() throws IOException {
        ClientResources P1_R = (ClientResources) player1Key.attachment();
        ClientResources P2_R = (ClientResources) player2Key.attachment();

        SocketChannel socketP1 = (SocketChannel) player1Key.channel();
        SocketChannel socketP2 = (SocketChannel) player2Key.channel();

        System.out.println(P1_R.getUsername() + " (P1) score: " + P1_R.challengeScore + "    (P2) score: " + P2_R.challengeScore);

        if (P1_R.challengeScore > P2_R.challengeScore) {
            P1_R.isWinner = 1;
            P2_R.isWinner = -1;
            P1_R.challengeScore += WQSettings.WINNER_EXTRA_POINTS;
            database.updateScore(P1_R.getUsername(), database.getScore(P1_R.getUsername()) + WQSettings.WINNER_EXTRA_POINTS);
            ServerHandler.serialize();
        }
        else if (P1_R.challengeScore < P2_R.challengeScore) {
            P1_R.isWinner = -1;
            P2_R.isWinner = 1;
            P2_R.challengeScore += WQSettings.WINNER_EXTRA_POINTS;
            database.updateScore(P2_R.getUsername(), database.getScore(P2_R.getUsername()) + WQSettings.WINNER_EXTRA_POINTS);
            ServerHandler.serialize();
        }

        try {
            IO.writeString(socketP1, P1_R.buffer, "FIN " + P1_R.isWinner + " " + P1_R.correct_answers +
                    " " + P1_R.wrong_answers + " " + P2_R.challengeScore);

            IO.writeString(socketP2, P2_R.buffer, "FIN " + P2_R.isWinner + " " + P2_R.correct_answers +
                    " " + P2_R.wrong_answers + " " + P1_R.challengeScore);

            player1Key.interestOps(0);
            player2Key.interestOps(0);

            P1_R.reset();
            P2_R.reset();

            socketP1.register(oldSelector, SelectionKey.OP_READ, player1Key.attachment());
            socketP2.register(oldSelector, SelectionKey.OP_READ, player2Key.attachment());
            oldSelector.wakeup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleEndGame(SelectionKey key) throws IOException {
        finished++;

        if(finished == 2)
           handleEndReport();
        else
            key.interestOps(0);
    }



    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ClientResources resources = (ClientResources) key.attachment();

        if (resources != null) {
           String s = resources.getUsername();
            IO.read(client, resources.buffer);

            String m = StandardCharsets.UTF_8.decode(resources.buffer).toString();
            if(WQDictionary.getTranslatedWords(chosenWords.get(resources.translatedWords-1)).contains(m)) {
                System.out.println("[" + s + "] Traduzione della parola #" + (resources.translatedWords) + " CORRETTA (+2)");
                resources.challengeScore += WQSettings.RIGHT_ANSWER_POINTS;
                resources.correct_answers++;
                database.updateScore(resources.getUsername(), database.getScore(resources.getUsername()) + WQSettings.RIGHT_ANSWER_POINTS);
            }
            else {
                System.out.println("[" + s + "] Traduzione della parola #" + (resources.translatedWords) + " SBAGLIATA (-1)");
                resources.challengeScore += WQSettings.WRONG_ANSWER_POINTS;
                resources.wrong_answers++;
                database.updateScore(resources.getUsername(), database.getScore(resources.getUsername()) + WQSettings.WRONG_ANSWER_POINTS);
            }

            ServerHandler.serialize();
        }

        key.interestOps(SelectionKey.OP_WRITE);
    }





    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ClientResources resources = (ClientResources) key.attachment();

        if (resources.translatedWords >= N)
            handleEndGame(key);
        else{
            IO.writeString(client, resources.buffer, chosenWords.get(resources.translatedWords));
            resources.translatedWords++;
            key.interestOps(SelectionKey.OP_READ);
        }
    }





}
