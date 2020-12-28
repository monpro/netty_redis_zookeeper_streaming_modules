package com.monpro.buffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.nio.IntBuffer;


public class BufferUse {
    static IntBuffer intBuffer = null;
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private static void allocatTest() {
        intBuffer = IntBuffer.allocate(20);
        logger.debug("--after allocate--");
        logger.debug("position=" + intBuffer.position());
        logger.debug("limit=" + intBuffer.limit());
        logger.debug("capacity=" + intBuffer.capacity());
    }

    private static void addDataToBuffer(int num) {
        for(int i = 0; i < num; i++) {
            intBuffer.put(i);
        }
        logger.debug("--after adding data--");
        logger.debug("position=" + intBuffer.position());
        logger.debug("limit=" + intBuffer.limit());
        logger.debug("capacity=" + intBuffer.capacity());
    }

    private static void flip() {
        intBuffer.flip();
        logger.debug("--after flip--");
        logger.debug("position=" + intBuffer.position());
        logger.debug("limit=" + intBuffer.limit());
        logger.debug("capacity=" + intBuffer.capacity());
    }

    private static void readDate(int num) {
        logger.debug("--read data--");

        for(int i = 0; i < num; i++) {
            logger.debug("read" + intBuffer.get());
        }
        logger.debug("--after read--");
        logger.debug("position=" + intBuffer.position());
        logger.debug("limit=" + intBuffer.limit());
        logger.debug("capacity=" + intBuffer.capacity());
        logger.debug("mark=" + intBuffer.mark());
    }

    private static void rewind() {
        intBuffer.rewind();
        logger.debug("--after rewind--");
        logger.debug("position=" + intBuffer.position());
        logger.debug("limit=" + intBuffer.limit());
        logger.debug("capacity=" + intBuffer.capacity());
    }

    private static void reset() {
        intBuffer.reset();
        logger.debug("--after reset--");
        logger.debug("position=" + intBuffer.position());
        logger.debug("limit=" + intBuffer.limit());
        logger.debug("capacity=" + intBuffer.capacity());
    }

    public static void main(String[] args) {
        logger.debug("allocate memory");
        allocatTest();

        logger.debug("adding data");
        addDataToBuffer(10);

        logger.debug("flip");
        flip();

        logger.debug("read");
        readDate(10);

        logger.debug("rewind");
        rewind();
        readDate(10);

    }
}
