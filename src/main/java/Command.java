import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record Command(Code code, String parameter, Map<String, String> flags) {
    public enum Code {
        EXIT("bye"),
        LIST("list"),
        TODO("todo"),
        DEADLINE("deadline"),
        EVENT("event"),
        DELETE("delete"),
        DELETE_MANY("delete many"),
        MARK("mark"),
        MARK_MANY("mark many"),
        UNMARK("unmark"),
        UNMARK_MANY("unmark many");

        private static final Map<String, Code> BY_CODE_STRING = new HashMap<>();

        static {
            for (Code code : values()) {
                BY_CODE_STRING.put(code.getCodeString(), code);
            }
        }

        private final String codeString;

        Code(String codeString) {
            this.codeString = codeString;
        }

        public static Code fromCodeString(String codeString) {
            return BY_CODE_STRING.get(codeString);
        }

        public String getCodeString() {
            return this.codeString;
        }
    }

    private static final String FLAG_DELIMITER = "/";

    private static final Map<Code, Map<String, Pattern>> AVAILABLE_COMMANDS = Stream
            .<Map.Entry<Code, List<String>>>of(
                    Map.entry(Code.EXIT, List.of()),
                    Map.entry(Code.LIST, List.of("from", "to", "completed")),
                    Map.entry(Code.TODO, List.of("completed")),
                    Map.entry(Code.DEADLINE, List.of("by", "completed")),
                    Map.entry(Code.EVENT, List.of("from", "to", "completed")),
                    Map.entry(Code.DELETE, List.of()),
                    Map.entry(Code.DELETE_MANY, List.of("from", "to", "completed", "containing")),
                    Map.entry(Code.MARK, List.of()),
                    Map.entry(Code.MARK_MANY, List.of("from", "to", "completed", "containing")),
                    Map.entry(Code.UNMARK, List.of()),
                    Map.entry(Code.UNMARK_MANY, List.of("from", "to", "completed", "containing"))
            )
            .map(pair -> Map.entry(
                    pair.getKey(),
                    pair.getValue().stream()
                            .map(flagName -> Map.entry(
                                    flagName,
                                    Pattern.compile("\\s+" + FLAG_DELIMITER + flagName + "\\b\\s*")
                            ))
                            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue))

            ))
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

    private static final Pattern CODE_PATTERN = Pattern.compile(
            "^\\s*(?<code>"
                    + Arrays.stream(Code.values()).map(Code::getCodeString).collect(Collectors.joining("|"))
                    + ")\\b\\s*"
    );

    public static Command parseCommand(String input) {
        Matcher codeMatcher = CODE_PATTERN.matcher(input);
        if (!codeMatcher.find()) {
            throw new IllegalArgumentException("Malformed command");
        }

        Code code = Code.fromCodeString(codeMatcher.group("code"));
        if (!AVAILABLE_COMMANDS.containsKey(code)) {
            throw new IllegalArgumentException("Unknown command: " + code);
        }

        Map<String, Matcher> flagMatchers = AVAILABLE_COMMANDS.get(code).entrySet().stream()
                .map(pair -> Map.entry(
                        pair.getKey(),
                        pair.getValue().matcher(input)
                ))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        List<Integer> delimiters = new ArrayList<>();
        delimiters.add(codeMatcher.end());
        delimiters.add(input.length());
        Map<Integer, String> flagPositions = new HashMap<>();
        flagPositions.put(codeMatcher.end(), "");
        for (Map.Entry<String, Matcher> pair : flagMatchers.entrySet()) {
            if (pair.getValue().find()) {
                flagPositions.put(pair.getValue().end(), pair.getKey());
                delimiters.add(pair.getValue().start());
                delimiters.add(pair.getValue().end());
            }
        }
        delimiters.sort(Integer::compareTo);

        Map<String, String> flags = new HashMap<>();
        for (int i = 0; i < delimiters.size(); i += 2) {
            String flagName = flagPositions.get(delimiters.get(i));
            if (flags.containsKey(flagName)) {
                throw new IllegalArgumentException("Duplicate flags: " + flagName);
            }
            flags.put(flagName, input.substring(delimiters.get(i), delimiters.get(i + 1)));
        }

        return new Command(code, flags.remove(""), flags);
    }
}
