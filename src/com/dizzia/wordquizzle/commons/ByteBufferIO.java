package com.dizzia.wordquizzle.commons;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ByteBufferIO {
    public static int MAX_STRING_LENGTH = 256;

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
        int written_bytes = 0;
        while(buffer.hasRemaining() && written_bytes != -1)
            written_bytes = channel.write(buffer);
    }




    public static String readString(SocketChannel channel) throws IOException {
        ByteBuffer input = ByteBuffer.allocate(MAX_STRING_LENGTH);
        input.clear();
        channel.read(input);
        input.flip();
        return StandardCharsets.UTF_8.decode(input).toString();
    }


    public static void writeString(SocketChannel channel, String message) throws IOException {
        ByteBuffer output = ByteBuffer.allocate(MAX_STRING_LENGTH);
        output.clear();
        byte[] bytes = message.getBytes();
        System.out.println("+write+: " + message);
        output.put(bytes);
        output.flip();
        channel.write(output);
    }

    public static int readInt(SocketChannel channel) throws IOException {
        ByteBuffer input = ByteBuffer.allocate(4);
        input.clear();
        channel.read(input);
        input.flip();
        return input.getInt();
    }

    public static void readString(SocketChannel channel, ByteBuffer buffer) throws IOException {
        buffer.clear();
        int read_byte = channel.read(buffer);
        buffer.flip();

    }

}
