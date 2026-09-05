package marquee.io;

public class ColumnCountException extends IllegalArgumentException {
    public ColumnCountException() {
        super("Inconsistent column count");
    }
}
