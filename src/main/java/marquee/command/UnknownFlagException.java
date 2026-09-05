package marquee.command;

/**
 * Exception class for when an unknown flag for a command is encountered.
 */
public class UnknownFlagException extends IllegalArgumentException {
    private final String flagName;
    private final Code code;

    /**
     * Constructs an {@code UnknownFlagException} with the name of the unknown flag
     * and the {@code Code} of the command it is found in.
     *
     * @param flagName the name of the unknown flag
     * @param code     the {@link Code} of the command being parsed
     * @throws NullPointerException if the {@code Code} is {@code null}
     */
    public UnknownFlagException(String flagName, Code code) throws NullPointerException {
        if (code == null) {
            throw new NullPointerException();
        }
        super("Unknown flag name in " + code.getCodeString() + ": " + flagName);
        this.flagName = flagName;
        this.code = code;
    }

    /**
     * Gets the name of the unknown flag that caused this exception.
     *
     * @return the name of the unknown flag
     */
    public String getFlagName() {
        return flagName;
    }

    /**
     * Gets the {@code Code} of the command being parsed when this exception was raised.
     *
     * @return the {@link Code} of the command
     */
    public Code getCode() {
        return code;
    }
}
