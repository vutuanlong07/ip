package marquee.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Code {
    EXIT("bye", List.of()),
    LOAD("load", List.of()),
    SAVE("save", List.of()),
    LIST("list", List.of()),
    FIND("find", List.of("from", "to", "completed", "incomplete")),
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
