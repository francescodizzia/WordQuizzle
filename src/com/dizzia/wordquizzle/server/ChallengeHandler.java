package com.dizzia.wordquizzle.server;

import com.dizzia.wordquizzle.commons.StatusCode;
import com.sun.org.apache.bcel.internal.generic.Select;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class ChallengeHandler implements Runnable {
    SelectionKey userKey;

    public ChallengeHandler(SelectionKey userKey) {
        this.userKey = userKey;
    }

    public void run() {
        SocketChannel channel = (SocketChannel) userKey.channel();

        try {
            //Selector selector = Selector.open();
            Selector selector = Selector.open();

            channel.configureBlocking(false);

            SelectionKey challengeKey = channel.register(selector, SelectionKey.OP_READ, userKey.attachment());

            System.out.println("SECONDO: " + ((ClientResources)challengeKey.attachment()).getUsername());

            while(true){
                selector.select();
                Iterator<SelectionKey> keysIterator = selector.selectedKeys().iterator();

                while (keysIterator.hasNext()) {
                    SelectionKey key = keysIterator.next();
                    keysIterator.remove();
                    System.out.println("Rimossa key " + key);

                    if(!key.isValid())
                        continue;
                    if (key.isReadable()) {
                        System.out.println("READABLE2");
                        ClientResources resources = (ClientResources) key.attachment();
                        String s = null;
                        String m = null;
                        if (resources != null) {
                            s = resources.getUsername();
                            ByteBuffer input = resources.buffer;
                            input.clear();
                            int read_byte = channel.read(input);
                            input.flip();
                            String line = StandardCharsets.UTF_8.decode(input).toString();
                            m = line;
                        }

                        System.out.println("Ciao fratello " + s);
                        System.out.println("S2 ho letto: " + m);


                        key.interestOps(0);
                        //key.interestOps(SelectionKey.OP_WRITE);
                    } else if (key.isWritable()) {
                        System.out.println("WRITABLE2");
                        //key.interestOps(SelectionKey.OP_READ);
                    }

                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


//        try {
//            //Selector selector = Selector.open();
//            Selector selector = Selector.open();
//
//            channel.configureBlocking(false);
//
//            SelectionKey challengeKey = channel.register(selector, SelectionKey.OP_READ, userKey.attachment());
//
//            System.out.println("SECONDO: " + ((ClientResources)challengeKey.attachment()).getUsername());
//
//            while(true){
//                selector.select();
//                Iterator<SelectionKey> keysIterator = selector.selectedKeys().iterator();
//
//                while (keysIterator.hasNext()) {
//                    SelectionKey key = keysIterator.next();
//                    keysIterator.remove();
//                    System.out.println("Rimossa key " + key);
//
//                    if(!key.isValid())
//                        continue;
//                   if (key.isReadable()) {
//                        System.out.println("READABLE2");
//                        ClientResources res = (ClientResources) key.attachment();
//                        String s = null;
//                        if (res != null) {
//                            s = res.getUsername();
//                        }
//
//                        System.out.println("Ciao fratello " + s);
//                        //key.interestOps(SelectionKey.OP_WRITE);
//                    } else if (key.isWritable()) {
//                        System.out.println("WRITABLE2");
//                        //key.interestOps(SelectionKey.OP_READ);
//                    }
//
//                }
//            }
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
            }

}
