package com.dizzia.wordquizzle.commons;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class IO {


    public static void prepareString(ByteBuffer buffer, String string){
        buffer.clear();
        buffer.put(string.getBytes());
        buffer.flip();
    }

    public static void prepareInt(ByteBuffer buffer, int integer){
        buffer.clear();
        buffer.putInt(integer);
        buffer.flip();
    }


    public static void writeString(SocketChannel channel, ByteBuffer buffer, String string) throws IOException {
        prepareString(buffer, string);
        channel.write(buffer);
    }

    public static void writeInt(SocketChannel channel, ByteBuffer buffer, int integer) throws IOException {
        prepareInt(buffer, integer);
        channel.write(buffer);
    }

    public static int read(SocketChannel channel, ByteBuffer buffer) throws IOException {
        buffer.clear();
        int read_byte = channel.read(buffer);
        buffer.flip();

        return read_byte;
    }

}
