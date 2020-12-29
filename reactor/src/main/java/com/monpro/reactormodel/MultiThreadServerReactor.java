package com.monpro.reactormodel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadServerReactor {

    ServerSocketChannel serverSocketChannel;
    AtomicInteger next = new AtomicInteger(0);
    Selector[] selectors = new Selector[2];
    SubReactor[] subReactors = null;

    MultiThreadServerReactor() throws IOException {
        selectors[0] = Selector.open();
        selectors[1] = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();

        InetSocketAddress address =
                new InetSocketAddress(8080);
        serverSocketChannel.socket().bind(address);
        serverSocketChannel.configureBlocking(false);

        // first selector will watch the on_accept event
        SelectionKey selectionKey =
                serverSocketChannel.register(selectors[0], SelectionKey.OP_ACCEPT);
        selectionKey.attach(new AcceptorHandler());

        // on reactor will be responsible for one selector
        subReactors[0] = new SubReactor(selectors[0]);
        subReactors[1] = new SubReactor(selectors[1]);
    }

    class SubReactor implements Runnable {
        final Selector selector;

        public SubReactor(Selector selector) {
            this.selector = selector;
        }

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    selector.select();
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = keys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey selectionKey = iterator.next();
                        dispatch(selectionKey);
                    }
                    keys.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void dispatch(SelectionKey sk) {
        Runnable handler = (Runnable) sk.attachment();
        if (handler != null) {
            handler.run();
        }
    }

    class AcceptorHandler implements Runnable {
        @Override
        public void run() {
            try {
                SocketChannel channel = serverSocketChannel.accept();
                if (channel != null) {
                    // TODO: add multithread handler
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // atomic check within the range of selectors
            if (next.incrementAndGet() == selectors.length) {
                next.set(0);
            }
        }
    }
}
