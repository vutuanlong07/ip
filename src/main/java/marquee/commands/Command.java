package marquee.commands;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import marquee.commands.exceptions.DuplicateFlagException;
import marquee.commands.exceptions.UnknownFlagException;

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

    public static Command parseCommand(String input) throws IllegalArgumentException {
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
