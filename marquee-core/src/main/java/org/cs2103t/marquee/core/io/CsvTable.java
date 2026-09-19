package org.cs2103t.marquee.core.io;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.cs2103t.marquee.core.DuplicateKeyException;

/**
 * Representation of a CSV table.
 */
public final class CsvTable {
    private final Set<String> columns;
    private final List<Record> values;

    /**
     * Representation of a CSV row.
     */
    public final class Record {
        private final Map<String, String> fields;

        private Record() {
            fields = columns.stream().collect(Collectors.toMap(columnName -> columnName, _ -> ""));
        }

        private Record(Map<String, String> fieldsByName) throws NoSuchElementException {
            this();
            columns.forEach(columnName -> fields.put(columnName, ""));
            fieldsByName.forEach(this::setField);
        }

        public CsvTable getParent() {
            return CsvTable.this;
        }

        public String getField(String columnName) throws NoSuchElementException {
            String field = fields.get(columnName);
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
            return String.join(",", columns.stream().map(fields::get).map(CsvTable::quote).toList());
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Record
                    && getParent() == ((Record) other).getParent()
                    && fields.equals(((Record) other).fields);
        }
    }

    /**
     * Creates a new CSV table with the given columns.
     *
     * @param columns a collection of the column names
     * @throws DuplicateKeyException if duplicate column names was given
     */
    public CsvTable(List<String> columns) throws DuplicateKeyException {
        this.columns = new HashSet<>(columns);
        this.values = new ArrayList<>();
    }

    /**
     * Creates a new CSV table with the given columns.
     *
     * @param columns a collection of the column names
     * @throws DuplicateKeyException if duplicate column names was given
     */
    public CsvTable(String... columns) throws DuplicateKeyException {
        this.columns = new HashSet<>(List.of(columns));
        this.values = new ArrayList<>();
    }

    /**
     * Returns an escaped version of the input.
     *
     * @param input the string to escape
     * @return CSV-escaped input string
     */
    public static String quote(String input) {
        return input == null || input.isEmpty()
                ? "\"\""
                : input.chars()
                .filter(c -> c == '"' || c == ',' || c == '\r' || c == '\n')
                .findAny().isPresent()
                  ? '"' + input.replace("\"", "\"\"") + '"'
                  : input;
    }

    public int getColumnCount() {
        return columns.size();
    }

    public Set<String> getColumns() {
        return Collections.unmodifiableSet(columns);
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
     * and all fields left empty {@code ""}.
     *
     * @return the new record, with uninitialized field set to an empty string {@code ""}
     */
    public Record createRecord() {
        return new Record();
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
     * Adds the given {@code Record} to the table.
     *
     * @param rows the {@link Record} to add
     */
    public void add(Record... rows) {
        values.addAll(List.of(rows));
    }

    private static int findNextSpecial(String string, int start) {
        int i = start;
        for (; i < string.length(); i++) {
            if (string.charAt(i) == '"' || string.charAt(i) == ','
                    || (string.charAt(i) == '\r' && i + 1 < string.length() && string.charAt(i + 1) == '\n')) {
                break;
            }
        }
        return i;
    }

    /**
     * Reads the file at the given filepath and, with the given separator, extract CSV data from the file
     * into a new {@code CsvTable}, then return the new {@code CsvTable}.
     *
     * @param filepath the filepath to read from
     * @return the new {@link CsvTable} with columns and rows from the file
     * @throws NoSuchFileException if no file is found at the location
     * @throws AccessDeniedException if the file can't be accessed
     * @throws ParseException if the file isn't in the proper CSV format
     * @throws IOException if an unexpected I/O error occurs while reading the file
     * @throws DuplicateKeyException if a duplicate column name is found
     */
    public static CsvTable readFile(Path filepath)
            throws NoSuchFileException, AccessDeniedException, ParseException,
            IOException, DuplicateKeyException {
        if (!Files.isRegularFile(filepath)) {
            throw new NoSuchFileException(filepath.toString());
        }
        if (!Files.isReadable(filepath)) {
            throw new AccessDeniedException(filepath.toString());
        }

        String content = Files.readString(filepath);
        List<String> columnNames = new ArrayList<>();

        StringBuilder fieldBuilder = new StringBuilder();
        boolean isEscaped = false;
        int fieldStart = 0;
        int fieldEnd;
        boolean headerIsNotEmpty = false;
        while ((fieldEnd = findNextSpecial(content, fieldStart)) < content.length()) {
            fieldBuilder.append(content, fieldStart, fieldEnd);
            char current = content.charAt(fieldEnd);
            if (current == '"') {
                if (isEscaped) {
                    if (fieldEnd + 1 < content.length()
                            && content.charAt(fieldEnd + 1) == '"') {
                        fieldStart = fieldEnd + 2;
                        fieldBuilder.append('"');
                    } else {
                        fieldStart = fieldEnd + 1;
                        isEscaped = false;
                    }
                } else {
                    fieldStart = fieldEnd + 1;
                    isEscaped = true;
                }
            } else if (current == ',') {
                fieldStart = fieldEnd + 1;
                if (isEscaped) {
                    fieldBuilder.append(',');
                } else {
                    columnNames.add(fieldBuilder.toString());
                    fieldBuilder.setLength(0);
                }
            } else {
                fieldStart = fieldEnd + 2;
                if (isEscaped) {
                    fieldBuilder.append("\r\n");
                } else {
                    break;
                }
            }
            headerIsNotEmpty = true;
        }
        if (headerIsNotEmpty || !fieldBuilder.isEmpty()) {
            columnNames.add(fieldBuilder.toString());
            fieldBuilder.setLength(0);

            if (isEscaped) {
                throw new ParseException("Escaped field not closed", fieldStart);
            }
        }

        CsvTable csv = new CsvTable(columnNames);
        while (fieldEnd < content.length()) {
            Record newRecord = csv.createRecord();
            int fieldCount = 0;
            boolean isNotEmpty = false;
            while (true) {
                fieldEnd = findNextSpecial(content, fieldStart);
                fieldBuilder.append(content, fieldStart, fieldEnd);
                if (fieldEnd == content.length()) {
                    fieldStart = fieldEnd;
                    break;
                }
                char current = content.charAt(fieldEnd);
                if (current == '"') {
                    if (isEscaped) {
                        if (fieldEnd + 1 < content.length()
                                && content.charAt(fieldEnd + 1) == '"') {
                            fieldStart = fieldEnd + 2;
                            fieldBuilder.append('"');
                        } else {
                            fieldStart = fieldEnd + 1;
                            isEscaped = false;
                        }
                    } else {
                        fieldStart = fieldEnd + 1;
                        isEscaped = true;
                    }
                } else if (current == ',') {
                    fieldStart = fieldEnd + 1;
                    if (isEscaped) {
                        fieldBuilder.append(',');
                    } else {
                        newRecord.setField(columnNames.get(fieldCount), fieldBuilder.toString());
                        fieldBuilder.setLength(0);
                        fieldCount++;
                        if (fieldCount > columnNames.size()) {
                            throw new ParseException("Too many fields", fieldStart);
                        }
                    }
                } else {
                    fieldStart = fieldEnd + 2;
                    if (isEscaped) {
                        fieldBuilder.append("\r\n");
                    } else {
                        break;
                    }
                }
                isNotEmpty = true;
            }
            if (isNotEmpty || !fieldBuilder.isEmpty()) {
                newRecord.setField(columnNames.get(fieldCount), fieldBuilder.toString());
                fieldBuilder.setLength(0);
                fieldCount++;

                if (isEscaped) {
                    throw new ParseException("Escaped field not closed", fieldStart);
                } else if (fieldCount < columnNames.size()) {
                    throw new ParseException("Not enough fields", fieldStart);
                } else {
                    csv.add(newRecord);
                }
            }
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
                            .map(CsvTable::quote)
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
