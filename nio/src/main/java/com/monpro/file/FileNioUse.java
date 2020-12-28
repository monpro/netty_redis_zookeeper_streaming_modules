package com.monpro.file;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileNioUse {

    private static final int CAPACITY = 2048;

    // read: channel - buffer
    // writer: buffer - channel
    public static void nioReadFile(String fileName) {
        try {
            RandomAccessFile file = new RandomAccessFile(fileName, "rw");
            FileChannel fileChannel = file.getChannel();

            ByteBuffer byteBuffer = ByteBuffer.allocate(CAPACITY);
            int length = fileChannel.read(byteBuffer);
            while (length != -1) {
                byteBuffer.flip();
                byte[] bytes = byteBuffer.array();
                String string = new String(bytes, 0, length);
                length = fileChannel.read(byteBuffer);
                System.out.println(string);
            }

            fileChannel.close();
            byteBuffer.clear();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void nioCopyFile(String srcPath, String destPath) {
        File srcFile = new File(srcPath);
        File destFile = new File(destPath);
        
        try {
            if (!destFile.exists()) {
                destFile.createNewFile();
            }

            FileInputStream fileInputStream = null;
            FileOutputStream fileOutputStream = null;
            FileChannel inChannel = null;
            FileChannel outChannel = null;
            
            try {
                fileInputStream = new FileInputStream(srcFile);
                fileOutputStream = new FileOutputStream(destFile);
                inChannel = fileInputStream.getChannel();
                outChannel = fileOutputStream.getChannel();

                long size = inChannel.size();
                long pos = 0;
                long count = 0;

                // directly transfer bytes between channels
                while (pos < size) {
                    count = size - pos > 2048 ? 2048: size - pos;
                    pos += outChannel.transferFrom(inChannel, pos, count);
                }
                /**
                // use buffer in the middle
                // inputChannel read data to buffer - write data to output channel
                // from buffer
                ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
                int length = inChannel.read(byteBuffer);
                while (length != -1) {
                    byteBuffer.flip();
                    int outLength = 0;
                    while ((outLength = outChannel.write(byteBuffer)) != 0) {
                        System.out.println("write bytes: " + outLength);
                    }
                    length = inChannel.read(byteBuffer);
                    byteBuffer.clear();
                }
                 **/
                outChannel.force(true);
            }
            finally {
                fileInputStream.close();
                fileOutputStream.close();
                inChannel.close();
                outChannel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
