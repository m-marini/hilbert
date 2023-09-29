package org.mmarini.hilbert.model;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Writes a csv file
 */
public class CSVWriter implements Closeable {

    /**
     * Returns the csv writer to file
     *
     * @param filename the filename
     * @param colNames the list of columns
     * @throws FileNotFoundException in caso of error
     */
    public static CSVWriter create(String filename, List<String> colNames) throws FileNotFoundException {
        return create(new File(filename), colNames);
    }


    /**
     * Returns the csv writer to file
     *
     * @param file     the file
     * @param colNames the list of columns
     * @throws FileNotFoundException in caso of error
     */
    private static CSVWriter create(File file, List<String> colNames) throws FileNotFoundException {
        return new CSVWriter(new PrintWriter(file), colNames);
    }

    private final PrintWriter writer;
    private final List<String> colNames;

    /**
     * Creates the csv writer
     *
     * @param writer   the writer
     * @param colNames the list of columns
     */
    public CSVWriter(PrintWriter writer, List<String> colNames) {
        this.writer = writer;
        this.colNames = colNames;
        writer.println(colNames.stream()
                .map(name -> "\"" + name + "\"")
                .collect(Collectors.joining(",")));
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    /**
     * Writes a row
     *
     * @param row the row
     * @return the writer
     */
    public CSVWriter write(Map<String, Number> row) {
        writer.println(colNames.stream()
                .map(key ->
                        row.containsKey(key)
                                ? row.get(key).toString()
                                : ""
                )
                .collect(Collectors.joining(",")));
        return this;
    }
}
