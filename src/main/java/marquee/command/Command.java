package marquee.command;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Record class containing details of a command such as
 * the command code, the parameter, and set flags.
 *
 * @param code      the name of the command, represented by a {@link Code}
 * @param parameter the parameter of the command
 * @param flags     the flags set and their values
 */
public record Command(Code code, String parameter, Map<String, String> flags) {

    private static final String FLAG_DELIMITER = "/";
    private static final String FLAG_DELIMITER_ESCAPE = "/";

    private static final Pattern CODE_PATTERN = Pattern.compile(
            "\\G\\s*(?<code>"
                    + Arrays.stream(Code.values())
                    .sorted(Comparator
                            .<Code>comparingInt(code -> code.getCodeString().length())
                            .reversed()
                    )
                    .map(Code::getCodeString)
                    .collect(Collectors.joining("|"))
                    + ")\\b\\s*"
    );

    private static final Pattern FLAG_PATTERN = Pattern.compile(
            "\\s*"
                    + "(?:"
                        + FLAG_DELIMITER_ESCAPE + "(?<flagDelimiterEscaped>" + FLAG_DELIMITER + ")"
                    + "|"
                        + FLAG_DELIMITER + "(?<flagName>\\S*\\b)"
                    + ")\\s*"
    );

    /**
     * Parses the given string as a {@code Command}.
     * <p>
     *     A command string should be in the format of:
     *     <ol>
     *         <li>command name</li>
     *         <li>parameter</li>
     *         <li>forward slash {@code /} and flag name</li>
     *         <li>flag value (optional depending on the flag)</li>
     *     </ol>
     *     (repeat 3 to 5 for every flag)
     * </p>
     * <p>
     *     The command components should be separated by 1 or more spaces <code>&nbsp;</code>
     * </p>
     * <p>
     *     To include a forward slash in the parameter or flag value, prepend a backslash to it ({@code \/})
     * </p>
     *
     * @param input the string to parse
     * @return the parsed {@code Command}
     * @throws UnknownFlagException     if a flag not applicable to the command is found
     * @throws DuplicateFlagException   if a flag appears more than once
     * @throws IllegalArgumentException if there are no supported command names found
     */
    public static Command parseCommand(String input)
            throws UnknownFlagException, DuplicateFlagException, IllegalArgumentException {
        Matcher codeMatcher = CODE_PATTERN.matcher(input);
        if (!codeMatcher.find()) {
            throw new IllegalArgumentException("Unknown command");
        }
        Code code = Code.fromCodeString(codeMatcher.group("code"));
        List<String> expectedFlags = code.getFlagNames();

        Matcher flagMatcher = FLAG_PATTERN.matcher(input);
        Map<String, String> flags = new HashMap<>();
        StringBuilder argument = new StringBuilder();
        String lastFlagName = null;
        int index = codeMatcher.end();
        while (flagMatcher.find(index)) {
            argument.append(input, index, flagMatcher.start());

            if (flagMatcher.group("flagDelimiterEscaped") != null) {
                argument.append(flagMatcher.group().replace(FLAG_DELIMITER_ESCAPE + FLAG_DELIMITER, FLAG_DELIMITER));
                index = flagMatcher.end();
                continue;
            }

            flags.put(lastFlagName, argument.toString());
            argument.setLength(0);

            lastFlagName = flagMatcher.group("flagName");
            index = flagMatcher.end();

            if (!expectedFlags.contains(lastFlagName)) {
                throw new UnknownFlagException(lastFlagName, code);
            }

            if (flags.containsKey(lastFlagName)) {
                throw new DuplicateFlagException(lastFlagName);
            }
        }
        flags.put(lastFlagName, argument.append(input, index, input.length()).toString());

        return new Command(code, flags.remove(null), flags);
    }
}
