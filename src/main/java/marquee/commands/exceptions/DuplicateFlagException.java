package marquee.commands.exceptions;

/**
 * Exception class for when a duplicate flag is encountered.
 */
public class DuplicateFlagException extends IllegalArgumentException {
    /**  */
    private final String flagName;

    /**
     * Constructs a {@code DuplicateFlagException} with the name of the duplicate flag.
     *
     * @param flagName the name of the duplicate flag
     */
    public DuplicateFlagException(String flagName) {
        super("Duplicate flag names: " + flagName);
        this.flagName = flagName;
    }

    /**
     * Gets the name of the duplicate flag that caused this exception.
     *
     * @return the name of the duplicate flag
     */
    public String getFlagName() {
        return flagName;
    }
}
