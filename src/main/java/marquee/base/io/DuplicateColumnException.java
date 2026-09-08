package marquee.base.io;

/**
 * Thrown to indicate CSV file has a duplicate column name.
 */
public class DuplicateColumnException extends IllegalArgumentException {
    private final String columnName;

    /**
     * Construct a {@code DuplicateColumnException} for a given column
     * with the given message.
     *
     * @param message the detail message
     * @param columnName the name of the duplicate column
     */
    public DuplicateColumnException(String message, String columnName) {
        super(message);
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }
}
