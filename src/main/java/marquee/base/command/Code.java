package marquee.base.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@code Code} defines the name of a command and the flags it accepts.
 * Do not create new instances repeatedly. All instances are tracked and may cause collisions.
 *
 * @param name the command name associated with this code
 */
public record Code(String name, List<String> flagNames) {
    /** Character sequence that starts a flag */
    public static final String FLAG_DELIMITER = "/";
    /** Character sequence that escapes the {@link #FLAG_DELIMITER} */
    public static final String ESCAPE_SEQUENCE = "\\";

    private static final Map<String, Code> DICTIONARY = new HashMap<>();

    /**
     * Create a new {@code Code}. Code instances are considered unique
     * if their command names are different. All code instances must be unique.
     * The argument is considered to be the parameter of the empty flag {@code ""}.
     *
     * @param name      the name of the code, which is what would be
     *                   used to invoke the command through the command line
     * @param flagNames list of flags that the command accepts
     * @throws NullPointerException     if the name is {@code null}
     * @throws IllegalArgumentException if a code with this name already exist
     */
    public Code(String name, List<String> flagNames) {
        if (name == null) {
            throw new NullPointerException("Code name cannot be null");
        }
        if (DICTIONARY.containsKey(name)) {
            throw new IllegalArgumentException("Code already exists");
        }

        this.name = name;
        this.flagNames = flagNames;
        DICTIONARY.put(name, this);
    }

    /**
     * Gets the {@code Code} with the given name
     *
     * @param name the name of the {@code Code}
     * @return the {@code Code} with the given name, or {@code null} if there are none
     */
    public static Code fromLabel(String name) {
        return DICTIONARY.get(name);
    }

    /**
     * Returns a {@code List} view of available command codes.
     *
     * @return an unmodifiable {@link List} of available command codes
     */
    public static List<Code> getAvailableCodes() {
        return List.copyOf(DICTIONARY.values());
    }

    @Override
    public String toString() {
        return this.name() + " " + String.join(" /", this.flagNames());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Code && this.name().equals(((Code) obj).name());
    }
}
