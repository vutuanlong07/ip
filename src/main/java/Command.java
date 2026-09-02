import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public record Command(Code code, String parameter, Map<String, String> flags) {
    public enum Code {
        EXIT("bye", List.of()),
        SAVE("save", List.of()),
        LIST("list", List.of()),
        SEARCH("search", List.of("from", "to", "completed", "incomplete")),
        TODO("todo", List.of("completed")),
        DEADLINE("deadline", List.of("by", "completed", "incomplete")),
        EVENT("event", List.of("from", "to", "completed", "incomplete")),
        DELETE("delete", List.of()),
        DELETE_ALL("delete all", List.of()),
        DELETE_MATCHING("delete matching", List.of("from", "to", "completed", "incomplete")),
        MARK("mark", List.of()),
        MARK_ALL("mark all", List.of()),
        MARK_MATCHING("mark matching", List.of("from", "to", "completed", "incomplete")),
        UNMARK("unmark", List.of()),
        UNMARK_ALL("unmark all", List.of()),
        UNMARK_MATCHING("unmark matching", List.of("from", "to", "completed", "incomplete"));

        private static final Map<String, Code> BY_CODE_STRING = new HashMap<>();

        static {
            for (Code code : values()) {
                BY_CODE_STRING.put(code.getCodeString(), code);
            }
        }

        private final String codeString;
        private final List<String> flagNames;

        Code(String codeString, List<String> flagNames) {
            this.codeString = codeString;
            this.flagNames = flagNames;
        }

        public static Code fromCodeString(String codeString) {
            return BY_CODE_STRING.get(codeString);
        }

        public String getCodeString() {
            return this.codeString;
        }

        public List<String> getFlagNames() {
            return this.flagNames;
        }
    }

    private static final String FLAG_DELIMITER = "/";

    private static final Map<Code, Map<String, Pattern>> AVAILABLE_COMMANDS = Arrays.stream(Code.values())
            .map(code -> Map.entry(
                    code,
                    code.getFlagNames().stream()
                            .map(flagName -> Map.entry(
                                    flagName,
                                    Pattern.compile("\\s+" + FLAG_DELIMITER + flagName + "\\b\\s*")
                            ))
                            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue))

            ))
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

    private static final Pattern CODE_PATTERN = Pattern.compile(
            "^\\s*(?<code>"
                    + Arrays.stream(Code.values())
                    .sorted(Comparator
                            .<Code>comparingInt(code -> code.getCodeString().length())
                            .reversed()
                    )
                    .map(Code::getCodeString)
                    .collect(Collectors.joining("|"))
                    + ")\\b\\s*"
    );

    public static Command parseCommand(String input) throws IllegalArgumentException {
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
