package com.example;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * The FileHandler works with file (used for queue storage) implementing file multi process lock
 * and functions specialized for FileQueueService
 *
 * @author dmitry.mikhailovich@gmail.com
 */
class FileHandler {

    private final static String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * if file is locked,
     * ACCESS_FILE_RETRY_COUNT is the count of retries to get access to file
     */
    private final static int ACCESS_FILE_RETRY_COUNT = 10;

    /**
     * if file is locked,
     * ACCESS_FILE_TIMEOUT is the period between retries to get access to queue file
     */
    private final static int ACCESS_FILE_TIMEOUT = 100;

    private final static int BYTE_BUFFER_SIZE = 255;

    /**
     * file working with
     */
    private final File file;

    FileHandler(File file) {
        this.file = file;
    }

    /**
     * add line to the end of file
     */
    void writeLine(String line) {
        runWorkerOnRandomAccessFile(randomAccessFile -> {
            long size = randomAccessFile.length();
            randomAccessFile.seek(size);
            randomAccessFile.writeBytes(line + LINE_SEPARATOR);
        });
    }

    /**
     * transformer replace only one line, if it returns null then line shouldn't be replace
     * the modified line should be the same size as initial line
     */
    void replaceLineWith(SameSizeLineTransformer transformer) {
        runWorkerOnRandomAccessFile(randomAccessFile -> {
            String line;
            String newLine = null;
            while ((line = randomAccessFile.readLine()) != null && newLine == null) {
                newLine = transformer.transform(line);
                if (newLine != null && line.length() == newLine.length()) {
                    long currentPosition = randomAccessFile.getFilePointer();
                    randomAccessFile.seek(currentPosition - line.length() - LINE_SEPARATOR.length());
                    randomAccessFile.writeBytes(newLine);
                }
            }
        });
    }

    /**
     * looks for the first line to remove accept {@param removePredicate} condition until
     * it delete one line or {@param readingPredicate} condition fail
     */
    void removeLineWithPredicate(Predicate<String> removePredicate, Predicate<String> readingPredicate) {
        runWorkerOnRandomAccessFile(randomAccessFile -> {
            String line;
            boolean toRemove = false;
            while ((line = randomAccessFile.readLine()) != null // check end of file
                    && readingPredicate.test(line) // check if we should look for line more
                    && !(toRemove = removePredicate.test(line))) { // check if it is line to be removed
                // empty
            }

            if (toRemove) {
                long writePointer = randomAccessFile.getFilePointer() - line.length() - LINE_SEPARATOR.length();
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
        });
    }

    private void runWorkerOnRandomAccessFile(RandomAccessFileWorker worker) {
        runWorkerOnRandomAccessFile(worker, ACCESS_FILE_RETRY_COUNT, ACCESS_FILE_TIMEOUT);
    }

    /**
     * runWorkerOnRandomAccessFile implements functionality of file locks using actions from provided worker
     */
    private void runWorkerOnRandomAccessFile(RandomAccessFileWorker worker, int retryCount, int timeout) {
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
                    worker.work(randomAccessFile);
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

    /**
     * Functional interface to read/write file via RandomAccessFile
     */
    @FunctionalInterface
    private interface RandomAccessFileWorker {
        void work(RandomAccessFile file) throws IOException;
    }

    @FunctionalInterface
    interface LinesTransformer {
        List<String> transform(List<String> lines);
    }

    @FunctionalInterface
    interface SameSizeLineTransformer {
        String transform(String line);
    }
}
