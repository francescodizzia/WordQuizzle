package com.dizzia.wordquizzle.commons;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ByteBufferIO {
    public static int MAX_STRING_LENGTH = 128;
    static int INTEGER_BYTE_SIZE = 4;

    public static int readInt(SocketChannel channel) throws IOException {
        ByteBuffer input = ByteBuffer.allocateDirect(4);
        input.clear();
        channel.read(input);
        input.flip();
        return input.getInt();
    }

    public static void writeInt(SocketChannel channel, int integer) throws IOException {
        ByteBuffer output = ByteBuffer.allocate(4);
        output.clear();
        output.putInt(integer);
        output.flip();
        channel.write(output);
    }

    public static String readString(SocketChannel channel) throws IOException {
        ByteBuffer input = ByteBuffer.allocateDirect(MAX_STRING_LENGTH);
        input.clear();
        channel.read(input);
        input.flip();
        return StandardCharsets.UTF_8.decode(input).toString();
    }


    public static void writeString(SocketChannel channel, String message) throws IOException {
        ByteBuffer output = ByteBuffer.allocateDirect(MAX_STRING_LENGTH);
        output.clear();
        byte[] bytes = message.getBytes();
        output.put(bytes);
        output.flip();
        channel.write(output);
    }


}
