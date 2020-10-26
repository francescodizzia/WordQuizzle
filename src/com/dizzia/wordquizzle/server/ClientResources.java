package com.dizzia.wordquizzle.server;

import com.dizzia.wordquizzle.commons.ByteBufferIO;

import java.nio.ByteBuffer;

public class ClientResources {
    String username;
    ByteBuffer buffer;
    int udp_port;
    boolean isBusy = false;
    int challengeScore = 0;
    long challengeTime = 0;
    int translatedWords = 0;
    int correct_answers = 0;
    int wrong_answers = 0;
    int isWinner = 0;


    public ClientResources() {
        buffer = ByteBuffer.allocate(ByteBufferIO.MAX_STRING_LENGTH);
    }


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
