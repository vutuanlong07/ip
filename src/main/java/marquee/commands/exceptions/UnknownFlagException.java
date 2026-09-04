package marquee.commands.exceptions;

import marquee.commands.Code;

public class UnknownFlagException extends IllegalArgumentException {
    private final String flagName;
    private final Code code;

    public UnknownFlagException(String flagName, Code code) throws NullPointerException {
        if (code == null) {
            throw new NullPointerException();
        }
        super("Unknown flag name in " + code.getCodeString() + ": " + flagName);
        this.flagName = flagName;
        this.code = code;
    }

    public String getFlagName() {
        return flagName;
    }

    public Code getCode() {
        return code;
    }
}
