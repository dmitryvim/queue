package com.example;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * @author dmitry.mikhailovich@gmail.com
 */
public class FileHandlerTest {

    private File file;

    private FileHandler fileHandler;

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionCauseOfLockedFile() throws Exception {
        FileChannel channel = FileChannel.open(this.file.toPath());
        try (FileLock lock = channel.tryLock()) {
            this.fileHandler.writeLine("line");
        }
    }

    @Test
    public void shouldWriteNewLine() throws Exception {
        // given
        String line = "new line";

        // when
        this.fileHandler.writeLine(line);

        // then
        List<String> lines = fileLines();
        assertEquals(line, lines.get(lines.size() - 1));
    }


    @Test
    public void shouldReplaceSecondLine() throws Exception {
        // given
        FileHandler.SameSizeLineTransformer transformer = line -> line.equals("second") ? "dnoces" : null;

        // when
        this.fileHandler.replaceLineWith(transformer);

        // then
        List<String> lines = fileLines();
        assertEquals(lines.size(), 3);
        assertEquals(lines.get(0), "first");
        assertEquals(lines.get(1), "dnoces");
        assertEquals(lines.get(2), "third");
    }

    @Test
    public void shouldRemoveSecondLine() throws Exception {
        // when
        this.fileHandler.removeLineWithPredicate("second"::equals, line -> !"third".equals(line));

        // then
        List<String> lines = fileLines();
        assertEquals(lines.size(), 2);
        assertEquals(lines.get(0), "first");
        assertEquals(lines.get(1), "third");
    }

    @Test
    public void shouldRemoveNotThirdLine() throws Exception {
        // when
        this.fileHandler.removeLineWithPredicate("third"::equals, line -> !"second".equals(line));

        // then
        List<String> lines = fileLines();
        assertEquals(lines.size(), 3);
        assertEquals(lines.get(0), "first");
        assertEquals(lines.get(1), "second");
        assertEquals(lines.get(2), "third");
    }

    @Before
    public void createTempFile() throws Exception {
        this.file = File.createTempFile("queue_service_file_handler", "test");
        fillFile("first", "second", "third");
        this.fileHandler = new FileHandler(this.file);
    }

    @After
    public void removeTempFile() throws Exception {
        this.file.delete();
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