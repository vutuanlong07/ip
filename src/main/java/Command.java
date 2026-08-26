import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Command(String code, String parameter, Map<String, String> flags) {
    private static final String flagDelimiter = "\\/";
    private static final Pattern commandPattern = Pattern.compile("^\\s*(\\w+)");
    private static final Pattern contentPattern = Pattern.compile("\\s*(\\S.*?|)\\s*(?=<DELIM>|\\z)".replace("<DELIM>", flagDelimiter));
    private static final Pattern flagPattern = Pattern.compile("<DELIM>(\\w+)".replace("<DELIM>", flagDelimiter));

    public static Command parseCommand(String input) {
        Matcher commandMatcher = commandPattern.matcher(input);
        Matcher contentMatcher = contentPattern.matcher(input);
        Matcher flagMatcher = flagPattern.matcher(input);

        String code = "";
        String argument = "";
        Map<String, String> flags = new HashMap<>();
        if (commandMatcher.find()) {
            code = commandMatcher.group(1);

            if (contentMatcher.find(commandMatcher.end())) {
                argument = contentMatcher.group(1);

                while (flagMatcher.find(contentMatcher.end())) {
                    if (contentMatcher.find(flagMatcher.end())) {
                        flags.put(flagMatcher.group(1), contentMatcher.group(1));
                    }
                }
            }
        }

        return new Command(code, argument, flags);
    }
}
