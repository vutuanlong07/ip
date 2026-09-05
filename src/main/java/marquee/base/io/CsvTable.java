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
import java.util.stream.Stream;

public final class CsvTable {
    private final String[] header;
    private final List<Record> values;
    private final Map<String, Integer> columnsByName;
    private final String separator;

    public class Record {
        private final String[] fields;

        public Record(String... fields) throws ColumnCountException {
            if (fields.length != header.length) {
                throw new ColumnCountException();
            }
            this.fields = fields.clone();
        }

        public Record(Map<String, String> fieldsByName) throws ColumnCountException, ColumnNameException {
            if (fieldsByName.size() != header.length) {
                throw new ColumnCountException();
            }
            this.fields = new String[header.length];
            fieldsByName.forEach(this::setField);
        }

        public String getField(String columnName) throws ColumnNameException {
            Integer columnIndex = columnsByName.get(columnName);
            if (columnIndex == null) {
                throw new ColumnNameException("Unknown column name: " + columnName, columnName);
            }
            return fields[columnsByName.get(columnName)];
        }

        public void setField(String columnName, String value) throws ColumnNameException {
            Integer columnIndex = columnsByName.get(columnName);
            if (columnIndex == null) {
                throw new ColumnNameException("Unknown column name: " + columnName, columnName);
            }
            fields[columnsByName.get(columnName)] = value;
        }

        public String[] getAllFields() {
            return fields.clone();
        }

        @Override
        public String toString() {
            return String.join(CsvTable.this.getSeparator(), fields);
        }
    }

    public CsvTable(List<String> header, String separator) throws ColumnNameException {
        this.header = header.toArray(String[]::new);
        this.separator = separator;
        this.values = new ArrayList<>();
        this.columnsByName = new HashMap<>();
        for (int i = 0; i < this.header.length; i++) {
            this.columnsByName.merge(this.header[i], i, (_, columnIndex) -> {
                throw new ColumnNameException(
                        "Duplicate column name: " + this.header[columnIndex],
                        this.header[columnIndex]
                );
            });
        }
    }

    public String getSeparator() {
        return separator;
    }

    public List<String> getHeader() {
        return List.of(header);
    }

    public List<Record> getValues() {
        return Collections.unmodifiableList(values);
    }

    public void add(String... fields) throws ColumnCountException {
        values.add(new Record(fields));
    }

    public void add(Record row) {
        values.add(row);
    }

    public void addAll(Record... rows) throws ColumnCountException, ColumnNameException {
        values.addAll(List.of(rows));
    }

    public static CsvTable readFile(Path filepath, String separator)
            throws NoSuchFileException, IOException, ParseException, ColumnCountException, ColumnNameException {
        if (!Files.isRegularFile(filepath)) {
            throw new NoSuchFileException(filepath.toString());
        }
        if (!Files.isReadable(filepath) || !Files.isWritable(filepath)) {
            throw new IOException("File is not accessible");
        }

        String content = Files.readString(filepath);
        Matcher unpairedNewlineMatcher = Pattern.compile("\\r(?!\\n)|(?<!\\r)\\n").matcher(content);
        if (unpairedNewlineMatcher.find()) {
            throw new ParseException("Unpaired newline or carriage return", unpairedNewlineMatcher.start());
        }

        List<String> lines = List.of(content.split("(?:\\r\\n)+"));
        if (lines.isEmpty() || lines.getFirst().isEmpty()) {
            throw new ParseException("File is empty", content.length());
        }
        List<String> header = List.of(lines.getFirst().split(separator, -1));

        CsvTable csv = new CsvTable(header, separator);
        lines.stream().skip(1)
                .map(line -> Pattern.compile(separator, Pattern.LITERAL).split(line, -1))
                .forEach(csv::add);
        return csv;
    }

    public static void writeFile(Path filepath, CsvTable csv) throws IOException {
        Path temp = null;
        try {
            temp = Files.createTempFile(filepath.getParent(), null, null);
            Files.writeString(temp, Stream.concat(
                    Stream.of(String.join(csv.getSeparator(), csv.getHeader())),
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
        return "Table(" + String.join(", ", this.getHeader()) + ")";
    }
}
