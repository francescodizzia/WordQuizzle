package com.dizzia.wordquizzle.server;

import com.dizzia.wordquizzle.commons.ByteBufferIO;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ClientResources {
    private String username;
    ByteBuffer buffer;

    public ClientResources() {
        buffer = ByteBuffer.allocate(ByteBufferIO.MAX_STRING_LENGTH);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
