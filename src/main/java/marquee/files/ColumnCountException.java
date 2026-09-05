package marquee.files;

public class ColumnCountException extends IllegalArgumentException {
    public ColumnCountException() {
        super("Inconsistent column count");
    }
}
