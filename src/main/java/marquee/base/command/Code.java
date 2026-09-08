package marquee.base.command;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A {@code Code} defines the name of a command and the flags it accepts.
 * <p>
 * Do not create new instances repeatedly. All instances are tracked and may cause collisions.
 *
 * @implNote The empty flag {@code ""} is considered the argument and cannot be used
 *           as a separate flag in any command.
 */
public final class Code {
    private static final Map<String, Code> DICTIONARY = new HashMap<>();

    private final String name;
    private final List<String> flagNames;

    /**
     * Create a new {@code Code}.
     * <p>
     * All code instances must have unique command names are different.
     * The argument is considered to be the parameter of the empty flag {@code ""}.
     *
     * @param name      the name of the code, which is what would be
     *                  used to invoke the command through the command line
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
        this.flagNames = List.copyOf(flagNames);
        DICTIONARY.put(name, this);
    }

    /**
     * Create a new {@code Code}.
     * <p>
     * All code instances must have unique command names are different.
     * The argument is considered to be the parameter of the empty flag {@code ""}.
     *
     * @param name      the name of the code, which is what would be
     *                  used to invoke the command through the command line
     * @param flagNames list of flags that the command accepts
     * @throws NullPointerException     if the name is {@code null}
     * @throws IllegalArgumentException if a code with this name already exist
     */
    public Code(String name, String... flagNames) {
        this(name, List.of(flagNames));
    }

    /**
     * Gets the {@code Code} with the given name.
     *
     * @param name the name of the {@code Code}
     * @return the {@code Code} with the given name, or {@code null} if there are none
     */
    public static Code getCode(String name) {
        return DICTIONARY.get(name);
    }

    /**
     * Returns an unmodifiable view of available command codes.
     *
     * @return an unmodifiable {@link Collection} of available command codes
     */
    public static Collection<Code> getAvailableCodes() {
        return Collections.unmodifiableCollection(DICTIONARY.values());
    }

    public String getName() {
        return name;
    }

    public List<String> getFlagNames() {
        return flagNames;
    }

    @Override
    public String toString() {
        return this.getName() + "(" + String.join(", ", this.getFlagNames()) + ")";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Code && this.getName().equals(((Code) obj).getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, flagNames);
    }

}
