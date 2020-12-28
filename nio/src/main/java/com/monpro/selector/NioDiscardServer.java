package com.monpro.selector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NioDiscardServer {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public static void startServer() {

        try {
            Selector selector = Selector.open();

            // get channel
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

            // non-block channel
            serverSocketChannel.configureBlocking(false);

            // bind the connection to port 8080
            serverSocketChannel.bind(new InetSocketAddress(8080));

            // register channel and event to the selector
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (selector.select() > 0) {
                Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

                while (selectedKeys.hasNext()) {
                    SelectionKey selectionKey = selectedKeys.next();

                    if (selectionKey.isAcceptable()) {
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        // register read event
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    } else if (selectionKey.isReadable()) {
                        // get channel based on the event
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        int length = 0;
                        while ((length = socketChannel.read(byteBuffer)) > 0) {
                            byteBuffer.flip();
                            // directly discard teh data
                            logger.info("discard data", new String(byteBuffer.array(), 0, length));
                            byteBuffer.clear();
                        }
                        socketChannel.close();
                    }
                    selectedKeys.remove();
                }
            }
            serverSocketChannel.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
