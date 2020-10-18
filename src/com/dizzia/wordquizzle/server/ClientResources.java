package com.dizzia.wordquizzle.server;

import com.dizzia.wordquizzle.commons.ByteBufferIO;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ClientResources {
    private String username;
    SelectionKey challenged;
    ByteBuffer buffer;

    public int port;
    int translatedWords = 0;
    int correct_answers = 0;
    int wrong_answers = 0;
    public int score = 0;
    long challengeTime = 0L;
    int isWinner = 0;



    public ClientResources(SocketChannel client) {
        buffer = ByteBuffer.allocate(ByteBufferIO.MAX_STRING_LENGTH);
        Socket socket = client.socket();
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

}
