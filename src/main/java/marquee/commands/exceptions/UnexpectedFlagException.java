package marquee.commands.exceptions;

public class UnexpectedFlagException extends IllegalArgumentException {
    private final String flagName;

    public UnexpectedFlagException(String flagName) {
        super("Unexpected flag name: " + flagName);
        this.flagName = flagName;
    }

    public String getFlagName() {
        return flagName;
    }
}
