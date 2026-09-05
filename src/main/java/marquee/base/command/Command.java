package marquee.base.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Immutable data class containing details of a command.
 * A command can have an argument and any number of flag-value pairs.
 * Available commands and their flags are defined in {@link Code}.
 */
public class Command {
    private static final Map<String, Code> CODE_BY_NAME = Code.getAvailableCodes().stream()
            .collect(Collectors.toUnmodifiableMap(
                    Code::name,
                    code -> code
            ));
    private static final Pattern CODE_PATTERN = Pattern.compile(
            "\\G\\s*(?<code>"
                    + String.join("|", CODE_BY_NAME.keySet())
                    + ")\\b\\s*"
    );
    private static final Pattern FLAG_PATTERN = Pattern.compile(
            "\\s*(?:" + Pattern.quote(Code.ESCAPE_SEQUENCE)
                    + "(?<flagDelimiterEscaped>" + Pattern.quote(Code.FLAG_DELIMITER) + ")|"
                    + Pattern.quote(Code.FLAG_DELIMITER) + "(?<flagName>\\S*\\b))\\s*"
    );

    private final Code code;
    private final Map<String, String> parameters;

    /**
     * Creates a new {@code Command} instance with the given {@code Code} and parameters.
     * The command argument is defined as the parameter value that the empty string {@code ""} maps to.
     *
     * @param code       the command {@link Code}
     * @param parameters mappings of flag names to parameter values
     * @throws NullPointerException   if {@code code} or {@code parameters} is {@code null}
     * @throws UnknownFlagException   if an unrecognized flag is found
     * @throws DuplicateFlagException if a duplicate flag is found
     */
    public Command(Code code, Map<String, String> parameters)
            throws NullPointerException, UnknownFlagException, DuplicateFlagException {
        if (code == null || parameters == null) {
            throw new NullPointerException();
        }
        this.code = code;
        this.parameters = new HashMap<>();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (!code.flagNames().contains(entry.getKey())) {
                throw new UnknownFlagException(entry.getKey(), code);
            }
            if (this.parameters.containsKey(entry.getKey())) {
                throw new DuplicateFlagException(entry.getKey());
            }

            this.parameters.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Gets the {@code Code} of this command.
     *
     * @return the {@code Code} of this command
     */
    public Code getCode() {
        return this.code;
    }

    /**
     * Gets the argument of this command.
     *
     * @return the argument of this command
     */
    public String getArgument() {
        return this.parameters.get("");
    }

    /**
     * Gets the value of the flag in this command.
     * If an empty string is given, returns the argument instead.
     *
     * @param flagName the name of the flag to get
     * @return the value of the flag
     */
    public String getFlag(String flagName) {
        return this.parameters.get(flagName);
    }

    /**
     * Checks if the command has an argument.
     *
     * @return whether the argument exist or not
     */
    public boolean hasArgument() {
        return this.parameters.containsKey("");
    }

    /**
     * Checks if the flag is set in this command.
     * If an empty string is given, checks for the argument instead.
     *
     * @param flagName the name of the flag to check
     * @return whether the flag is set or not
     */
    public boolean hasFlag(String flagName) {
        return this.parameters.containsKey(flagName);
    }

    /**
     * Returns a view of the parameter map.
     * Use this to take advantage of built-in {@link Map} functionalities.
     *
     * @return an unmodifiable view of the parameter map
     */
    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    /**
     * Parses the given string as a {@code Command} according to the following format.
     * <ol>
     *     <li>command name/code name</li>
     *     <li>parameter</li>
     *     <li>forward slash {@code /} and flag name</li>
     *     <li>flag value (optional depending on the flag)</li>
     * (repeat 3 to 5 for every flag)
     * </ol>
     * <p>
     *     The command components should be separated by 1 or more spaces <code>&nbsp;</code>
     * </p>
     * <p>
     *     To include a forward slash in the parameter or flag value, prepend a backslash to it ({@code \/})
     * </p>
     *
     * @param input the string to parse
     * @return the parsed {@code Command}
     * @throws IllegalArgumentException if there are no supported command names found
     * @throws UnknownFlagException   if an unrecognized flag is found
     * @throws DuplicateFlagException if a duplicate flag is found
     * @implSpec Subclasses must call this method first, only after this method
     *           throws an {@code IllegalArgumentException}can the subclass continue parsing the command
     * @see Code
     */
    public static Command parseCommand(String input)
            throws UnknownFlagException, DuplicateFlagException, IllegalArgumentException {
        Matcher codeMatcher = CODE_PATTERN.matcher(input);
        if (!codeMatcher.find()) {
            throw new IllegalArgumentException("Unknown command");
        }
        Code code = CODE_BY_NAME.get(codeMatcher.group("code"));

        if (codeMatcher.end() == input.length()) {
            return new Command(code, Map.of());
        } else {
            Matcher flagMatcher = FLAG_PATTERN.matcher(input);
            Map<String, String> parameters = new HashMap<>();
            StringBuilder argument = new StringBuilder();

            String lastFlagName = "";
            int i = codeMatcher.end();
            for (; flagMatcher.find(i); i = flagMatcher.end()) {
                argument.append(input, i, flagMatcher.start());

                if (flagMatcher.group("flagDelimiterEscaped") != null) {
                    argument.append(flagMatcher.group().replace(
                            Code.ESCAPE_SEQUENCE + Code.FLAG_DELIMITER,
                            Code.FLAG_DELIMITER
                    ));
                    continue;
                }

                parameters.put(lastFlagName, argument.toString());
                argument.setLength(0);
                lastFlagName = flagMatcher.group("flagName");
            }
            parameters.put(lastFlagName, argument.append(input, i, input.length()).toString());

            return new Command(code, parameters);
        }
    }
}
