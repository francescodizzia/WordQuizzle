package com.dizzia.wordquizzle.server;

import com.dizzia.wordquizzle.commons.ByteBufferIO;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ClientResources {
    private String username;
    ByteBuffer buffer;
    public int port;
    public int challengeScore = 0;


    SelectionKey challenged = null;
    int translatedWords = 0;
    int correct_answers = 0;
    int wrong_answers = 0;
    long challengeTime = 0;
    int isWinner = 0;


    public ClientResources() {
        buffer = ByteBuffer.allocate(ByteBufferIO.MAX_STRING_LENGTH);
//        Socket socket = client.socket();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getUDP_port() {
        return port;
    }

    public void reset(){
        challenged = null;
        translatedWords = 0;
        correct_answers = 0;
        wrong_answers = 0;
        challengeTime = 0;
        challengeScore = 0;
        isWinner = 0;
    }
}
