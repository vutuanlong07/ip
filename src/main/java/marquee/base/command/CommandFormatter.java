package marquee.base.command;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Formatter and parser for {@code Command}
 * <h1>
 * Command format
 * <p>
 * A valid command contains the following components in order
 * <ul>
 *     <li>
 *     Command name/code name
 *     <li>
 *     Parameter
 *     <li>
 *     Flag components (repeat for every flag)
 *     <ul>
 *         <li>forward slash {@code /} and flag name</li>
 *         <li>flag value (optional depending on the flag)</li>
 *     </ul>
 * </ul>
 * The command components should be separated by 1 or more spaces <code>&nbsp;</code>.
 * <p>
 * To include a forward slash in the parameter or flag value, prepend a backslash to it ({@code \/}).
 *
 * @see Command
 * @see Code
 */
public final class CommandFormatter {
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

    /**
     * Parses the given string as a {@code Command} according to the class-defined format.
     *
     * @param input the string to parse
     * @return the parsed {@code Command}
     * @throws IllegalArgumentException if there are no supported command names found
     * @throws UnknownFlagException   if an unrecognized flag is found
     * @throws DuplicateFlagException if a duplicate flag is found
     * @implSpec Subclasses must call this method first, only after this method
     *           throws an {@code IllegalArgumentException}can the subclass continue parsing the command
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
