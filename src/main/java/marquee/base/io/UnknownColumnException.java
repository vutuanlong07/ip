package marquee.base.io;

/**
 * Thrown when a {@link marquee.base.io.CsvTable.Record} tried to access an unknown column.
 */
public class UnknownColumnException extends IllegalArgumentException {
    private final String columnName;

    /**
     * Construct a {@code UnknownColumnException} for a given column
     * with the given message.
     *
     * @param message the detail message
     * @param columnName the name of the unknown column
     */
    public UnknownColumnException(String message, String columnName) {
        super(message);
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }
}
