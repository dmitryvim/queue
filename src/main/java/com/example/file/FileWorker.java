package com.example.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author dmitry.mikhaylovich@bostongene.com
 */
//TODO add tests to fileworker
public class FileWorker {

    private final static String LINE_SEPARATOR = System.getProperty("line.separator");

    private final static int ACCESS_FILE_RETRY_COUNT = 10;

    private final static int ACCESS_FILE_TIMEOUT = 100;

    private final static int BYTE_BUFFER_SIZE = 255;

    private final File file;

    public FileWorker(File file) {
        this.file = file;
    }

    public String readFirstLine() {
        if (this.file.exists()) {
            return runWorkerOnRandomAccessFile(RandomAccessFile::readLine);
        } else {
            return null;
        }
    }

    public void writeLine(String line) {
        runWorkerOnRandomAccessFile(randomAccessFile -> {
            long size = randomAccessFile.length();
            randomAccessFile.seek(size);
            randomAccessFile.writeBytes(line + LINE_SEPARATOR);
            return null;
        });
    }

    public void removeFirstLine(String firstLine) {
        // depends on queue size there are two ways to solve this problem: for large queues it is better to use temp file

        if (this.file.exists()) {
            runWorkerOnRandomAccessFile(randomAccessFile -> {
                String line = randomAccessFile.readLine();
                if (Objects.equals(line, firstLine)) {
                    long writePointer = 0;
                    long readPointer = randomAccessFile.getFilePointer();
                    byte buffer[] = new byte[BYTE_BUFFER_SIZE];
                    int read;
                    while ((read = randomAccessFile.read(buffer)) > 0) {
                        readPointer += read;
                        randomAccessFile.seek(writePointer);
                        randomAccessFile.write(buffer, 0, read);
                        writePointer += read;
                        randomAccessFile.seek(readPointer);
                    }
                    randomAccessFile.setLength(writePointer);
                }
                return null;
            });
        }
    }

    private String runWorkerOnRandomAccessFile(RandomAccessFileWorker worker) {
        return runWorkerOnRandomAccessFile(worker, ACCESS_FILE_RETRY_COUNT, ACCESS_FILE_TIMEOUT);
    }

    private String runWorkerOnRandomAccessFile(RandomAccessFileWorker worker, int retryCount, int timeout) {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(this.file, "rw")) {
            FileChannel channel = randomAccessFile.getChannel();
            FileLock lock = null;
            try {
                lock = channel.tryLock();
                while (lock == null && --retryCount > 0) {
                    TimeUnit.MILLISECONDS.sleep(timeout);
                    lock = channel.tryLock();
                }
                if (lock == null) {
                    throw new IllegalStateException("Unable to get access to file");
                } else {
                    return worker.work(randomAccessFile);
                }
            } finally {
                if (lock != null) {
                    lock.close();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to work with file.", e);
        } catch (InterruptedException e) {
            // really, here I need more complicated mechanism, but not for test task
            throw new IllegalStateException("Interrupted exception occurred", e);
        }
    }

    @FunctionalInterface
    private interface RandomAccessFileWorker {
        String work(RandomAccessFile file) throws IOException;
    }
}
