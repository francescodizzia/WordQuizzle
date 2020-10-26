package com.dizzia.wordquizzle.server;

import com.dizzia.wordquizzle.commons.ByteBufferIO;
import com.dizzia.wordquizzle.commons.WQSettings;
import com.dizzia.wordquizzle.database.Database;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class ChallengeHandler implements Runnable {
    //Chiavi dei due giocatori
    private final SelectionKey player1Key;
    private final SelectionKey player2Key;

    //Lista delle parole scelte
    private final ArrayList<String> chosenWords;

    //Vector contenente le traduzioni
    private final Vector<ArrayList<String>> translatedWords;

    //Riferimento alla tabella degli utenti connessi
    private final ConcurrentHashMap<String, InetSocketAddress> loggedUsers;

    //Riferimento al database degli utenti
    private final Database database;

    //Riferimento al vecchio Selector (quello del ServerHandler)
    private final Selector oldSelector;


    //Numero di parole da tradurre
    private final int N = WQSettings.N_WORDS;

    //Tiene traccia del numero di utenti che ha concluso la partita
    private int finished = 0;

    //Flag per concludere la partita
    private boolean close = false;



    //Inizializzo il necessario per la sfida e seleziono le N parole casuali e le traduzioni associate
    public ChallengeHandler(SelectionKey player1Key, SelectionKey player2Key, Database database) {
        this.database = database;
        loggedUsers = ServerHandler.loggedUsers;
        this.player1Key = player1Key;
        this.player2Key = player2Key;
        oldSelector = ServerHandler.selector;
        chosenWords = ServerHandler.wqDictionary.getDistinctWords(N);
        translatedWords = new Vector<>();

        for(String chosenWord: chosenWords){
            translatedWords.add(WQDictionary.getTranslatedWords(chosenWord));
        }

        System.out.println(Arrays.toString(translatedWords.toArray()));
    }


    public void run() {
        SocketChannel player1 = (SocketChannel) player1Key.channel();
        SocketChannel player2 = (SocketChannel) player2Key.channel();

        //Tengo traccia del tempo durante il quale la sfida ha avuto inizio
        long startTime = System.currentTimeMillis();

        //Nuovo selettore dedicato alla sfida
        Selector selector;
        try {
            selector = Selector.open();
            //Registro i due utenti nel nuovo Selector e imposto il loro interestOps su WRITE
            player1.register(selector, SelectionKey.OP_WRITE, player1Key.attachment());
            player2.register(selector, SelectionKey.OP_WRITE, player2Key.attachment());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }


        while(!close){
            try {
                //Metto il timeout della select a 200 millisecondi in modo da aggiornare poco più avanti
                //il tempo trascorso dall'inzio della partita
                selector.select(200);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            //Aggiorno il tempo trascorso
            long elapsedTime = System.currentTimeMillis() - startTime;

            //Gestisco il timeout
            if(elapsedTime >= WQSettings.CHALLENGE_TIMEOUT){
                handleTimeout();
                System.out.println("Sono passati " + WQSettings.CHALLENGE_TIMEOUT + " secondi, timeout!");
                break;
            }

            //Se entrambi gli utenti hanno finito la partita, posso procedere con il calcolo del vincitore
            //e l'invio del resoconto finale
            if(finished == 2) {
                try {
                    handleEndReport();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }

            Iterator<SelectionKey> keysIterator = selector.selectedKeys().iterator();

            while (keysIterator.hasNext()) {
                SelectionKey key = keysIterator.next();
                keysIterator.remove();
                try {
                    //Gestisco la lettura
                    if (key.isReadable())
                        handleRead(key);
                    //Gestisco la scrittura
                     else if (key.isWritable())
                        handleWrite(key);
                } catch (IOException e) {
                    //Gestisco eventuali disconnessioni
                    handleDisconnect(key);
                }


            }
        }

        System.out.println("FINE DELLA SFIDA");
}

    //Gestisco la disconnessione rimuovendo l'utente dalla lista di utenti online
    //e aggiorno il contatore degli utenti che hanno finito la
    //partita
    private void handleDisconnect(SelectionKey key) {
        finished++;
        System.out.println("Un giocatore si è disconnesso.");
        key.cancel();
        try {
            key.channel().close();
            ClientResources resources = (ClientResources) key.attachment();

            String username = resources.username;
            if(username != null) {
                System.out.println("Arrivederci " + username);
                loggedUsers.remove(username);
                System.out.println(loggedUsers.keySet());
            }

        } catch (IOException cex) {
            cex.printStackTrace();
        }
    }

    //Gestisco il timeout in modo identico alla fase finale della partita
    private void handleTimeout() {
        System.out.println("Effettivo timeout");
        try {
            handleEndReport();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Step finale della partita: calcolo il vincitore, assegno eventuali punti extra e
    //invio il resoconto ad ogni utente
    public void handleEndReport() throws IOException {
        ClientResources P1_R = (ClientResources) player1Key.attachment();
        ClientResources P2_R = (ClientResources) player2Key.attachment();

        SocketChannel socketP1 = (SocketChannel) player1Key.channel();
        SocketChannel socketP2 = (SocketChannel) player2Key.channel();

        System.out.println(P1_R.username + " (P1) score: " + P1_R.challengeScore + "    (P2) score: " + P2_R.challengeScore);

        //Calcolo il vincitore
        if (P1_R.challengeScore > P2_R.challengeScore) {
            P1_R.isWinner = 1;
            P2_R.isWinner = -1;
            P1_R.challengeScore += WQSettings.WINNER_EXTRA_POINTS;
            database.updateScore(P1_R.username, database.getScore(P1_R.username) + WQSettings.WINNER_EXTRA_POINTS);
            ServerHandler.serialize();
        }
        else if (P1_R.challengeScore < P2_R.challengeScore) {
            P1_R.isWinner = -1;
            P2_R.isWinner = 1;
            P2_R.challengeScore += WQSettings.WINNER_EXTRA_POINTS;
            database.updateScore(P2_R.username, database.getScore(P2_R.username) + WQSettings.WINNER_EXTRA_POINTS);
            ServerHandler.serialize();
        }

        try {
            //Scrivo il messaggio di resoconto finale
            if (player1Key.isValid())
                ByteBufferIO.writeString(socketP1, P1_R.buffer, "FIN " + P1_R.isWinner + " " + P1_R.correct_answers +
                        " " + P1_R.wrong_answers + " " + P2_R.challengeScore);

            if (player2Key.isValid())
                ByteBufferIO.writeString(socketP2, P2_R.buffer, "FIN " + P2_R.isWinner + " " + P2_R.correct_answers +
                        " " + P2_R.wrong_answers + " " + P1_R.challengeScore);


            //Reimposto le informazioni della sfida due utenti
            P1_R.reset();
            P2_R.reset();


            //Registro le due chiavi nel vecchio Selector
            if (player1Key.isValid()) {
                player1Key.interestOps(0);
                socketP1.register(oldSelector, SelectionKey.OP_READ, player1Key.attachment());
                System.out.println("P1 ritorna al vecchio selector");
            }
            if (player2Key.isValid()){
                player2Key.interestOps(0);
                socketP2.register(oldSelector, SelectionKey.OP_READ, player2Key.attachment());
                System.out.println("P2 ritorna al vecchio selector");
            }

            //Risveglio il vecchio Selector
            oldSelector.wakeup();
            close = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    //Aggiorno il contatore degli utenti che hanno concluso e se
    //entrambi hanno finito vado alla conclusione della partita
    public void handleEndGame(SelectionKey key) throws IOException {
        finished++;

        if(finished == 2)
           handleEndReport();
        else
            key.interestOps(0);
    }



    //Legge la traduzione fornita dal client e verifica la correttezza, aumentando o diminuendo
    //il punteggio e serializzandolo nel database
    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ClientResources resources = (ClientResources) key.attachment();

        if (resources != null) {
            ByteBufferIO.readString(client, resources.buffer);

            String m = StandardCharsets.UTF_8.decode(resources.buffer).toString();

            //Se la parola letta rientra nelle traduzioni accettate dal sistema
            if(translatedWords.get(resources.translatedWords-1).contains(m)) {
                System.out.println("[" + resources.username + "] Traduzione della parola #" + (resources.translatedWords) + " CORRETTA (+2)");
                resources.challengeScore += WQSettings.RIGHT_ANSWER_POINTS;
                resources.correct_answers++;
                database.updateScore(resources.username, database.getScore(resources.username) + WQSettings.RIGHT_ANSWER_POINTS);
            }
            //Se la traduzione risulta sbagliata
            else {
                System.out.println("[" + resources.username + "] Traduzione della parola #" + (resources.translatedWords) + " ERRATA (-1)");
                resources.challengeScore += WQSettings.WRONG_ANSWER_POINTS;
                resources.wrong_answers++;
                database.updateScore(resources.username, database.getScore(resources.username) + WQSettings.WRONG_ANSWER_POINTS);
            }

            ServerHandler.serialize();
        }

        key.interestOps(SelectionKey.OP_WRITE);
    }





    //Mando la prossima parola da tradurre al client e controllo se l'utente in questione
    //ha finito
    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ClientResources resources = (ClientResources) key.attachment();

        if (resources.translatedWords >= N)
            handleEndGame(key);
        else{
            ByteBufferIO.writeString(client, resources.buffer, chosenWords.get(resources.translatedWords));
            resources.translatedWords++;
            key.interestOps(SelectionKey.OP_READ);
        }
    }





}
