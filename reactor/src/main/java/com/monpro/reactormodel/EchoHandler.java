package com.monpro.reactormodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.nio.ch.sctp.SendFailed;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class EchoHandler {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    final SocketChannel socketChannel;
    final SelectionKey selectionKey;
    final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    static final int RECEIVING = 0, SENDING = 1;
    int state = RECEIVING;

    EchoHandler(Selector selector, SocketChannel sc) throws IOException {
        socketChannel = sc;
        socketChannel.configureBlocking(false);
        selectionKey = socketChannel.register(selector, 0);
        // attach handler itself to the selected key
        selectionKey.attach(this);
        selectionKey.interestOps(SelectionKey.OP_READ);
        selector.wakeup();
    }

    public void run() throws IOException {
        if (state == SENDING) {
            socketChannel.write(byteBuffer);
            byteBuffer.clear();
            selectionKey.interestOps(SelectionKey.OP_CONNECT);
            state = RECEIVING;
        } else if (state == RECEIVING) {
            int length = 0;
            while ((length = socketChannel.read(byteBuffer)) > 0) {
                logger.info(new String(byteBuffer.array(), 0, length));
            }
            byteBuffer.flip();
            selectionKey.interestOps(SelectionKey.OP_WRITE);
            state = SENDING;
        }
    }
}
