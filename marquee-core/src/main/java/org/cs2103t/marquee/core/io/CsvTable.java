package marquee.base.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Representation of a CSV table.
 */
public final class CsvTable {
    private final List<String> columns;
    private final List<Record> values;
    private final String separator;

    /**
     * Representation of a CSV row.
     */
    public class Record {
        private final Map<String, String> fields;

        private Record(String... fields) throws IllegalArgumentException {
            if (fields.length != columns.size()) {
                throw new IllegalArgumentException("Inconsistent column count");
            }
            this.fields = new HashMap<>();
            IntStream.range(0, columns.size()).forEach(i -> this.fields.put(columns.get(i), fields[i]));
        }

        private Record(Map<String, String> fieldsByName) throws UnknownColumnException {
            this.fields = new HashMap<>();
            columns.forEach(columnName -> this.fields.put(columnName, ""));
            fieldsByName.forEach(this::setField);
        }

        public String getField(String columnName) throws UnknownColumnException {
            String field = this.fields.get(columnName);
            if (field == null) {
                throw new UnknownColumnException("Unknown column name: " + columnName, columnName);
            }
            return field;
        }

        public void setField(String columnName, String value) throws NullPointerException, UnknownColumnException {
            if (value == null) {
                throw new NullPointerException("CSV Field cannot be null");
            }
            if (!fields.containsKey(columnName)) {
                throw new UnknownColumnException("Unknown column name: " + columnName, columnName);
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
            return String.join(CsvTable.this.getSeparator(), columns.stream().map(fields::get).toList());
        }
    }

    /**
     * Creates a new CSV table with the given header and separator.
     *
     * @param columns    the header containing the column names
     * @param separator the field separator
     * @throws DuplicateColumnException if duplicate column names are found in the header
     */
    public CsvTable(List<String> columns, String separator) throws DuplicateColumnException {
        this.columns = new ArrayList<>(columns);
        this.separator = separator;
        this.values = new ArrayList<>();
    }

    public String getSeparator() {
        return separator;
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
     * Creates a new {@code Record} with the same columns as the CSV table and the given field values.
     *
     * @param fields the field values in the same order as the columns
     * @return the new record
     * @throws IllegalArgumentException if the number of fields is not the same as the columns
     */
    public Record createRecord(String... fields) throws IllegalArgumentException {
        return new Record(fields);
    }

    /**
     * Creates a new {@code Record} with the same columns as the CSV table
     * but only fill the given field values.
     *
     * @param fields a mapping from field names to field values
     * @return the new record, with uninitialized field set to an empty string {@code ""}
     * @throws UnknownColumnException if an unrecognized column name is found
     */
    public Record createPartialRecord(Map<String, String> fields) throws UnknownColumnException {
        return new Record(fields);
    }

    /**
     * Creates a new column in the CSV table with the initial value given.
     *
     * @param columnName the name of the new column
     * @param initVal    the initial value of the column
     */
    public void newColumn(String columnName, String initVal) throws DuplicateColumnException {
        if (columns.contains(columnName)) {
            throw new DuplicateColumnException("Column already exist", columnName);
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

    /**
     * Reads the file at the given filepath and, with the given separator, extract CSV data from the file
     * into a new {@code CsvTable}, then return the new {@code CsvTable}.
     *
     * @param filepath  the filepath to read from
     * @param separator the expected field separator
     * @return the new {@link CsvTable} with columns and rows from the file
     * @throws NoSuchFileException if no file is found at the location
     * @throws IOException if the file can't be accessed
     * @throws ParseException if the file isn't in the CSV format
     * @throws DuplicateColumnException if a duplicate column name is found
     * @throws IllegalArgumentException if the rows have inconsistent column count
     */
    public static CsvTable readFile(Path filepath, String separator)
            throws NoSuchFileException, IOException, ParseException,
            DuplicateColumnException, IllegalArgumentException {
        if (!Files.isRegularFile(filepath)) {
            throw new NoSuchFileException(filepath.toString());
        }
        if (!Files.isReadable(filepath)) {
            throw new IOException("File is not accessible");
        }

        String content = Files.readString(filepath);
        Matcher unpairedNewlineMatcher = Pattern.compile("\\r(?!\\n)|(?<!\\r)\\n").matcher(content);
        if (unpairedNewlineMatcher.find()) {
            throw new ParseException("Unpaired newline or carriage return", unpairedNewlineMatcher.start());
        }

        List<String> lines = List.of(content.split("(?:\\r\\n)+"));
        if (lines.isEmpty() || lines.getFirst().isEmpty()) {
            return new CsvTable(List.of(), separator);
        }
        List<String> header = List.of(lines.getFirst().split(separator, -1));

        CsvTable csv = new CsvTable(header, separator);
        lines.stream().skip(1)
                .map(line -> Pattern.compile(separator, Pattern.LITERAL).split(line, -1))
                .forEach(csv::addNew);
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
        Path temp = null;
        try {
            temp = Files.createTempFile(filepath.getParent(), null, null);
            Files.writeString(temp, Stream.concat(
                    Stream.of(String.join(csv.getSeparator(), csv.getColumns())),
                    csv.getValues().stream().map(Record::toString)
            ).collect(Collectors.joining("\r\n")));
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
