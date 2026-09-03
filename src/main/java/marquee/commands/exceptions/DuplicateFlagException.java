package marquee.commands.exceptions;

public class DuplicateFlagException extends IllegalArgumentException {
    private final String flagName;

    public DuplicateFlagException(String flagName) {
        super("Duplicate flag names: " + flagName);
        this.flagName = flagName;
    }

    public String getFlagName() {
        return flagName;
    }
}
