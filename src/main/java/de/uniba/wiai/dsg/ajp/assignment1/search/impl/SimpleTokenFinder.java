package de.uniba.wiai.dsg.ajp.assignment1.search.impl;

import de.uniba.wiai.dsg.ajp.assignment1.search.SearchTask;
import de.uniba.wiai.dsg.ajp.assignment1.search.TokenFinder;
import de.uniba.wiai.dsg.ajp.assignment1.search.TokenFinderException;

import java.io.FileWriter;
import java.io.IOException;

import java.io.PrintWriter;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;


public class SimpleTokenFinder implements TokenFinder {

    private SearchTask task;
    private StringBuilder ignoreOutput = new StringBuilder();
    private StringBuilder foundOutput = new StringBuilder();
    private List<String> ignoreNames = new ArrayList<>();

    int totalCount = 0;

    public SimpleTokenFinder() {
        /*
         * DO NOT REMOVE
         *
         * REQUIRED FOR GRADING
         */
    }

    @Override
    public void search(final SearchTask task) throws TokenFinderException {
        validate(task);
        this.task = task;
        try {
            if (!(task.getIgnoreFile() == null || task.getIgnoreFile().equals(""))) {
                ignoreNames = Files.readAllLines(Path.of(task.getIgnoreFile()));
            }
        } catch (IOException e) {
            throw new TokenFinderException("Error while reading ignore file.");
        }

        parseDirectory(Path.of(this.task.getRootFolder()));
        String message = String.format("The project includes **%s** %d times.\n", task.getToken(), totalCount);
        System.out.println(ignoreOutput.toString() + foundOutput + message);
        try {
            print(ignoreOutput.toString() + foundOutput + message);
        } catch (IOException e) {
            throw new TokenFinderException("Error while writing output to file.");
        }
    }

    /**
     * Helper method go through to Directory
     *
     * @param rootFolder the directory the method works on
     * @throws TokenFinderException wraps up occurring exceptions
     */
    private void parseDirectory(Path rootFolder) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootFolder)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    checker(path, this.task.getIgnoreFile());
                } else if (Files.isRegularFile(path)) {
                    if (path.getFileName().toString().endsWith(this.task.getFileExtension())) {
                        parseFile(path);
                    }
                }
            }
        } catch (IOException | TokenFinderException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to check IgnoreFiles
     *
     * @param path       checked this path in IgnoreFiles
     * @param ignoreFile which Files is Ignore
     * @throws IOException wraps up occurring exceptions
     */
    private void checker(Path path, String ignoreFile) {
        if (ignoreNames.contains(path.getFileName().toString())) {
            String content = path + " directory was ignored.\n\n";
            ignoreOutput.append(content);
        } else {
            parseDirectory(path);
        }
    }

    /**
     * helper method to find a Token
     *
     * @param file the directory the method works on
     * @throws TokenFinderException wraps up occurring exceptions
     */
    private void parseFile(Path file) throws TokenFinderException {
        int count = 0;
        int row = 1;
        try {
            List<String> contents = Files.readAllLines(file);
            for (String line : contents) {
                if (!contents.isEmpty()) {
                    int column = 0;
                    while (column != -1) {
                        column = line.indexOf(this.task.getToken(), column);
                        if (column != -1) {
                            count++;

                            StringBuilder content = new StringBuilder(line);
                            content.insert(column, "**").insert(column + task.getToken().length() + 2, "**");
                            foundOutput.append(String.format("%s:%d,%d> %s\n", file, row, column, content));
                            column += this.task.getToken().length();
                        }
                    }
                    row++;
                }
            }
            totalCount += count;

            String message = String.format("%s includes **%s** %d times.\n\n", file, this.task.getToken(), count);
            foundOutput.append(message);

        } catch (IOException e) {
            throw new TokenFinderException("Token was not found");
        }
    }

    private void print(String message) throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(task.getResultFile(), false), true);
        out.write(message);
        out.close();
    }

    private void validate(final SearchTask task) throws TokenFinderException {
        if ((task.getIgnoreFile() != null && !task.getIgnoreFile().equals("")) && !Files.isRegularFile(Path.of(task.getIgnoreFile()))) { //We allow empty ignore file names, no files will be ignored
            throw new TokenFinderException("Ignore file is not a file or is an invalid file");
        }
        if (task.getRootFolder() == null || !Files.isDirectory(Path.of(task.getRootFolder()))) {
            throw new TokenFinderException("Root folder is null or not a directory");
        }
        if (task.getResultFile() == null || task.getResultFile().equals("")) {
            throw new TokenFinderException("Result file is null or not a file");
        }
        if (task.getToken() == null || task.getToken().equals("")) {
            throw new TokenFinderException("Token is null or empty");
        }
        if (task.getFileExtension() == null) {
            throw new TokenFinderException("File extension is null");
        }
    }

}



