package org.cs2103t.marquee.core.io;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.cs2103t.marquee.core.DuplicateKeyException;

/**
 * Representation of a CSV table.
 */
public final class CsvTable {
    private static final Pattern NEWLINE_PATTERN = Pattern.compile("(?:\\r\\n)+");
    private static final Pattern UNPAIRED_NEWLINE_PATTERN = Pattern.compile("\\r(?!\\n)|(?<!\\r)\\n");
    private static final Pattern QUOTE_PATTERN = Pattern.compile("(?<!\")\"(?!\")");

    private final List<String> columns;
    private final List<Record> values;

    /**
     * Representation of a CSV row.
     */
    public class Record {
        private final Map<String, String> fields;

        private Record() {
            this.fields = new HashMap<>();
        }

        private Record(String... fields) throws IllegalArgumentException {
            if (fields.length != columns.size()) {
                throw new IllegalArgumentException("Inconsistent column count");
            }
            this.fields = new HashMap<>();
            IntStream.range(0, columns.size()).forEach(i -> this.setField(columns.get(i), fields[i]));
        }

        private Record(Map<String, String> fieldsByName) throws NoSuchElementException {
            this.fields = new HashMap<>();
            columns.forEach(columnName -> this.fields.put(columnName, ""));
            fieldsByName.forEach(this::setField);
        }

        public String getField(String columnName) throws NoSuchElementException {
            String field = this.fields.get(columnName);
            if (field == null) {
                throw new NoSuchElementException(columnName);
            }
            return field;
        }

        public void setField(String columnName, String value) throws NullPointerException, NoSuchElementException {
            if (value == null) {
                throw new NullPointerException("CSV Field cannot be null");
            }
            if (!fields.containsKey(columnName)) {
                throw new NoSuchElementException(columnName);
            }
            fields.put(columnName, value);
        }

        /**
         * Gets a map of all the fields.
         *
         * @return a mapping from field names to field values
         */
        public Map<String, String> getAllFields() {
            return Collections.unmodifiableMap(fields);
        }

        @Override
        public String toString() {
            return String.join(",", columns.stream().map(fields::get).toList());
        }
    }

    /**
     * Creates a new CSV table with the given columns.
     *
     * @param columns a collection of the column names
     * @throws DuplicateKeyException if duplicate column names was given
     */
    public CsvTable(Collection<String> columns) throws DuplicateKeyException {
        this.columns = new ArrayList<>(columns);
        this.values = new ArrayList<>();
    }

    public List<String> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    /**
     * Returns all the current {@code Record} in the table.
     *
     * @return list of the current {@link Record}
     */
    public List<Record> getValues() {
        return Collections.unmodifiableList(values);
    }

    /**
     * Creates a new {@code Record} with the same columns as the CSV table
     * with the given field values, and the other fields left empty {@code ""}.
     *
     * @param fields a mapping from field names to field values
     * @return the new record, with uninitialized field set to an empty string {@code ""}
     * @throws NoSuchElementException if an unrecognized column name is given
     */
    public Record createRecord(Map<String, String> fields) throws NoSuchElementException {
        return new Record(fields);
    }

    /**
     * Creates a new column in the CSV table with the initial value given.
     *
     * @param columnName the name of the new column
     * @param initVal the initial value of the column
     * @throws DuplicateKeyException if a duplicate column name was given
     */
    public void newColumn(String columnName, String initVal) throws DuplicateKeyException {
        if (columns.contains(columnName)) {
            throw new DuplicateKeyException("Column already exist", columnName);
        }

        columns.add(columnName);
        values.forEach(record -> record.fields.put(columnName, initVal));
    }

    /**
     * Adds a {@code Record} with the given field values to the table.
     *
     * @param fields list of field values in the same order as the columns
     */
    public void addNew(String... fields) {
        values.add(new Record(fields));
    }

    /**
     * Adds the given {@code Record} to the table.
     *
     * @param rows the {@link Record} to add
     */
    public void add(Record... rows) {
        values.addAll(List.of(rows));
    }

    private static List<String> parseRow(String input, int count) throws ParseException {
        List<String> result = new ArrayList<>();
        int fieldStart = 0;
        int fieldEnd = input.indexOf(',', fieldStart);
        fieldEnd = fieldEnd == -1 ? input.length() : fieldEnd;
        for (int i = 0; count < 0 || i < count; i++) {
            if (fieldStart == input.length()) {
                if (count < 0) {
                    break;
                } else {
                    throw new ParseException("Missing fields", fieldStart);
                }
            }

            String rawField = input.substring(fieldStart, fieldEnd);

            result.add(rawField);

            fieldStart = fieldEnd;
            fieldEnd = input.indexOf(',', fieldStart);
            fieldEnd = fieldEnd == -1 ? input.length() : fieldEnd;
        }
        if (fieldStart != input.length()) {
            throw new ParseException("Too many fields", fieldStart);
        }
        return result;
    }

    /**
     * Reads the file at the given filepath and, with the given separator, extract CSV data from the file
     * into a new {@code CsvTable}, then return the new {@code CsvTable}.
     *
     * @param filepath the filepath to read from
     * @param separator the expected field separator
     * @return the new {@link CsvTable} with columns and rows from the file
     * @throws NoSuchFileException if no file is found at the location
     * @throws AccessDeniedException if the file can't be accessed
     * @throws ParseException if the file isn't in the proper CSV format
     * @throws IOException if an unexpected I/O error occurs while reading the file
     * @throws DuplicateKeyException if a duplicate column name is found
     */
    public static CsvTable readFile(Path filepath, String separator)
            throws NoSuchFileException, AccessDeniedException, ParseException,
            IOException, DuplicateKeyException {
        if (!Files.isRegularFile(filepath)) {
            throw new NoSuchFileException(filepath.toString());
        }
        if (!Files.isReadable(filepath)) {
            throw new AccessDeniedException(filepath.toString());
        }

        String content = Files.readString(filepath);
        Matcher unpairedNewlineMatcher = UNPAIRED_NEWLINE_PATTERN.matcher(content);
        if (unpairedNewlineMatcher.find()) {
            throw new ParseException("Unpaired newline or carriage return", unpairedNewlineMatcher.start());
        }

        Matcher newlineMatcher = NEWLINE_PATTERN.matcher(content);
        if (!newlineMatcher.find()) {
            return new CsvTable(Collections.emptyList());
        }
        List<String> columns = parseRow(content.substring(0, newlineMatcher.start()), -1);

        CsvTable csv = new CsvTable(columns);
        for (int i = newlineMatcher.end(); newlineMatcher.find(); i = newlineMatcher.end()) {
            csv.addNew(parseRow(content.substring(i, newlineMatcher.start()), columns.size()).toArray(String[]::new));
        }
        return csv;
    }

    /**
     * Writes the {@code CsvTable} to the filepath, using the separator defined in the {@code CsvTable}.
     * <p>
     * Will create a new file if there are none. This operation is atomic - the file will not be changed if any
     * exceptions occurs.
     *
     * @param filepath the filepath to write to
     * @param csv the {@link CsvTable} to write
     * @throws IOException if the file cannot be written to
     */
    public static void writeFile(Path filepath, CsvTable csv) throws IOException {
        Files.createDirectories(filepath.getParent());
        Path temp = null;
        try {
            temp = Files.createTempFile(filepath.getParent(), null, null);
            Files.writeString(temp,
                    csv.getColumns().stream()
                            .map(column -> "\"" + column.replace("\"", "\"\"") + "\"")
                            .collect(Collectors.joining(","))
                            + "\r\n"
                            + csv.getValues().stream()
                            .map(Record::toString)
                            .collect(Collectors.joining("\r\n")));
            Files.move(temp, filepath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            if (temp != null) {
                Files.deleteIfExists(temp);
            }
        }
    }

    @Override
    public String toString() {
        return "Table(" + String.join(", ", this.getColumns()) + ")";
    }
}
