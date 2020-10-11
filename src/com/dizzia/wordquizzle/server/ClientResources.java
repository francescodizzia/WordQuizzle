package com.dizzia.wordquizzle.server;

import com.dizzia.wordquizzle.commons.ByteBufferIO;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ClientResources {
    private String username;
    ByteBuffer buffer;
    InetSocketAddress address;



    public ClientResources(SocketChannel client) {
        buffer = ByteBuffer.allocate(ByteBufferIO.MAX_STRING_LENGTH);
        Socket socket = client.socket();
        address = (InetSocketAddress) socket.getLocalSocketAddress();
    }


    public InetSocketAddress getAddress(){
        return address;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
