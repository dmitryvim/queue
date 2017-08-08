package com.example.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author dmitry.mikhaylovich@bostongene.com
 */
public class FileHandler {

    private final static String LINE_SEPARATOR = System.getProperty("line.separator");

    private final static int ACCESS_FILE_RETRY_COUNT = 10;

    private final static int ACCESS_FILE_TIMEOUT = 100;

    private final File file;

    public FileHandler(File file) {
        this.file = file;
    }

    public void writeLine(String line) {
        runWorkerOnRandomAccessFile(randomAccessFile -> {
            long size = randomAccessFile.length();
            randomAccessFile.seek(size);
            randomAccessFile.writeBytes(line + LINE_SEPARATOR);
        });
    }

    public void transform(LinesTransformer transformer) {
        runWorkerOnRandomAccessFile(randomAccessFile -> {
            String line;
            List<String> lines = new ArrayList<>();
            while ((line = randomAccessFile.readLine()) != null) {
                lines.add(line);
            }
            randomAccessFile.seek(0);
            // really I know about problem, in case rw operation failed we can loose our data
            transformer.transform(lines).forEach(newLine -> {
                try {
                    randomAccessFile.writeBytes(newLine + LINE_SEPARATOR);
                } catch (IOException e) {
                    throw new IllegalStateException("Error on rewriting random access file", e);
                }
            });
            randomAccessFile.setLength(randomAccessFile.getFilePointer());
        });
    }

    private void runWorkerOnRandomAccessFile(RandomAccessFileWorker worker) {
        runWorkerOnRandomAccessFile(worker, ACCESS_FILE_RETRY_COUNT, ACCESS_FILE_TIMEOUT);
    }

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

    @FunctionalInterface
    private interface RandomAccessFileWorker {
        void work(RandomAccessFile file) throws IOException;
    }

    @FunctionalInterface
    interface LinesTransformer {
        List<String> transform(List<String> lines);
    }
}
