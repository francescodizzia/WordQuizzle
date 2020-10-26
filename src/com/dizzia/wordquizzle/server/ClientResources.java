package com.dizzia.wordquizzle.server;

import com.dizzia.wordquizzle.commons.ByteBufferIO;

import java.nio.ByteBuffer;

public class ClientResources {
    //Informazioni base dell'utente
    String username;
    ByteBuffer buffer;
    int udp_port;

    //Informazioni relative alla sfida in cui l'utente si trova
    boolean isBusy;
    int challengeScore;
    long challengeTime;
    int translatedWords;
    int correct_answers;
    int wrong_answers;
    int isWinner;


    public ClientResources() {
        reset();
        buffer = ByteBuffer.allocate(ByteBufferIO.MAX_STRING_LENGTH);
    }


    //Reinizializza le variabili
    public void reset(){
        translatedWords = 0;
        correct_answers = 0;
        wrong_answers = 0;
        challengeTime = 0;
        challengeScore = 0;
        isWinner = 0;
        isBusy = false;
    }
}
