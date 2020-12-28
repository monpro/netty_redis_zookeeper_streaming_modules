package com.monpro.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class SocketNioSender {

    private static Charset charset = Charset.forName("UTF-8");
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private static InetAddress SERVER_IP;

    static {
        try {
            SERVER_IP = InetAddress.getByName("");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private static final int SERVER_PORT = 8080;

    public static void sendFile(String src, String dest) {

        File file = new File(src);
        if (!file.exists()) {
            logger.debug("source file doesn't exist");
            return;
        }

        try {
            FileChannel fileChannel = new FileInputStream(file).getChannel();
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.socket().connect(
                    new InetSocketAddress(SERVER_IP, SERVER_PORT));
            // non-blocking
            socketChannel.configureBlocking(false);

            logger.debug("client connected to the server");

            while (!socketChannel.finishConnect()) {
                // keep looping for waiting
            }

            ByteBuffer byteBuffer = charset.encode(dest);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int fileNameLength = byteBuffer.capacity();

            buffer.putInt(fileNameLength);
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
            logger.info("sending file Name length: ", fileNameLength);

            socketChannel.write(byteBuffer);
            logger.info("sending file: ", dest);

            buffer.putLong(file.length());
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
            logger.info("sending file length", file.length());

            logger.info("start to send file");
            int length = 0;
            int progress = 0;

            while ((length = fileChannel.read(buffer)) > 0) {
                buffer.flip();
                socketChannel.write(buffer);
                buffer.clear();
                progress += length;
                logger.debug(" -- " + (100 * progress / file.length()) + "%-- ");
            }
            if (length == -1) {
                fileChannel.close();
                socketChannel.shutdownOutput();
                socketChannel.close();
            }
            logger.info("finish sending file");


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
