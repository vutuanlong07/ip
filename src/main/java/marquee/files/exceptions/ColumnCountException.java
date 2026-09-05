package marquee.files.exceptions;

public class ColumnCountException extends IllegalArgumentException {
    public ColumnCountException() {
        super("Inconsistent column count");
    }
}
