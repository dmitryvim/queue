package com.example.file;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dmitry.mikhaylovich@bostongene.com
 */
//TODO add tests to fileworker
public class FileWorker {

    private final static String LINE_SEPARATOR = System.getProperty("line.separator");

    private final File file;

    public FileWorker(File file) {
        this.file = file;
    }

    public String readFirstLine() {
        if (this.file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(this.file))) {
                return reader.readLine();
            } catch (FileNotFoundException e) {
                //hope it is unable to occur cuzz of chech if file exists
                throw new IllegalStateException("Unable to find file");
            } catch (IOException e) {
                throw new IllegalStateException("Unable to read file");
            }
        } else {
            return null;
        }
    }

    public void writeLine(String line) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.file, true))) {
            writer.append(line).append(LINE_SEPARATOR);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read file");
        }

    }

    public void removeFirstLine(String firstLine) {
        // depends on queue size there are two ways to solve this problem: for large queues it is better to use temp file

        if (this.file.exists()) {
            List<String> lines = null;

            //read content without first line to lines
            try (BufferedReader reader = new BufferedReader(new FileReader(this.file))) {
                String readLine = reader.readLine();
                if (readLine.equals(firstLine)) {
                    lines = reader.lines().collect(Collectors.toList());
                }
            } catch (FileNotFoundException e) {
                //hope it is unable to occur because of check if file exists
                throw new IllegalStateException("Unable to find file");
            } catch (IOException e) {
                throw new IllegalStateException("Unable to read file");
            }

            //write line back to file
            if (lines != null && !lines.isEmpty()) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.file))) {
                    lines.forEach(line -> {
                        try {
                            writer.append(line).append(LINE_SEPARATOR);
                        } catch (IOException e) {
                            throw new IllegalStateException("Unable to write file");
                        }
                    });
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to access file");
                }
            }
        }
    }
}
