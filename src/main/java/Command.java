import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Command(String code, String parameter, Map<String, String> flags) {
    private static final String flagDelimiter = "\\/";
    private static final Pattern commandPattern = Pattern.compile("^\\s*(?<code>\\w+)");
    private static final Pattern contentPattern = Pattern.compile("\\s*(?<content>\\S.*?|)\\s*(?=<DELIM>|\\z)".replace("<DELIM>", flagDelimiter));
    private static final Pattern flagPattern = Pattern.compile("<DELIM>(?<flag>\\w+)".replace("<DELIM>", flagDelimiter));

    public static Command parseCommand(String input) {
        Matcher commandMatcher = commandPattern.matcher(input);
        Matcher contentMatcher = contentPattern.matcher(input);
        Matcher flagMatcher = flagPattern.matcher(input);

        String code = "";
        String argument = "";
        Map<String, String> flags = new HashMap<>();
        if (commandMatcher.find()) {
            code = commandMatcher.group("code");

            if (contentMatcher.find(commandMatcher.end())) {
                argument = contentMatcher.group("content");

                while (flagMatcher.find(contentMatcher.end())) {
                    if (contentMatcher.find(flagMatcher.end())) {
                        flags.put(flagMatcher.group("flag"), contentMatcher.group("content"));
                    }
                }
            }
        }

        return new Command(code, argument, flags);
    }
}
