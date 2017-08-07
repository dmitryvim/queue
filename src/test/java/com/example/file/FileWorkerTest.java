package com.example.file;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * @author dmitry.mikhaylovich@bostongene.com
 */
public class FileWorkerTest {

    private File file;

    private FileWorker fileWorker;

    @Before
    public void createTempFile() throws Exception {
        this.file = File.createTempFile("queue_service_file_worker", "test");
        fillFile("first", "second", "third");
        this.fileWorker = new FileWorker(this.file);
    }

    @After
    public void removeTempFile() throws Exception {
        this.file.delete();
    }

    @Test
    public void readFirstLine() throws Exception {
        // when
        String firstLine = this.fileWorker.readFirstLine();

        // then
        assertEquals("first", firstLine);
    }

    @Test
    public void writeLine() throws Exception {
        // given
        String line = "new line";

        // when
        this.fileWorker.writeLine(line);

        // then
        List<String> lines = fileLines();
        assertEquals(line, lines.get(lines.size() - 1));
    }

    @Test
    public void removeFirstLine() throws Exception {
        // when
        String firstLine = this.fileWorker.readFirstLine();
        this.fileWorker.removeFirstLine(firstLine);

        // then
        List<String> lines = fileLines();
        assertEquals("second", lines.get(0));
    }

    private void fillFile(String... lines) throws Exception {
        try (FileWriter fileWriter = new FileWriter(this.file)) {
            Arrays.stream(lines).forEach(line -> {
                try {
                    fileWriter.append(line).append("\n");
                } catch (IOException e) {
                    throw new IllegalStateException("Test failed, cuzz unable to write new file");
                }
            });
        }
    }

    private List<String> fileLines() throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(this.file))) {
            return reader.lines().collect(Collectors.toList());
        }
    }

}