package marquee.files;

public class ColumnNameException extends IllegalArgumentException {
    private final String columnName;

    public ColumnNameException(String message, String columnName) {
        super(message);
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }
}
