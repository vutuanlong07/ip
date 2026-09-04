package marquee.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enum of the command codes.
 * Command codes are names of the commands supported by Marquee,
 * as well as their supported flags.
 */
public enum Code {
    /** Ends the chat session */
    EXIT("bye", List.of()),
    /** Loads the checklist from the save file */
    LOAD("load", List.of()),
    /** Saves the checklist to the save file */
    SAVE("save", List.of()),
    /** Lists all tasks in the checklist */
    LIST("list", List.of()),
    /** Searches for tasks that matches the filters */
    FIND("find", List.of("from", "to", "completed", "incomplete")),
    /** Create a new to-do task and add it to the checklist */
    TODO("todo", List.of("completed")),
    /** Create a new task with deadline and add it to the checklist */
    DEADLINE("deadline", List.of("by", "completed", "incomplete")),
    /** Create a new event and add it to the checklist */
    EVENT("event", List.of("from", "to", "completed", "incomplete")),
    /** Delete the tasks at the given indices */
    DELETE("delete", List.of()),
    /** Delete all tasks in the checklist */
    DELETE_ALL("delete all", List.of()),
    /** Delete all tasks that matches the filters */
    DELETE_MATCHING("delete matching", List.of("from", "to", "completed", "incomplete")),
    /** Mark the tasks at the given indices as completed */
    MARK("mark", List.of()),
    /** Mark all tasks in the checklist as completed */
    MARK_ALL("mark all", List.of()),
    /** Mark all tasks that matches the filters as completed*/
    MARK_MATCHING("mark matching", List.of("from", "to", "completed", "incomplete")),
    /** Remove the mark from the tasks at the given indices */
    UNMARK("unmark", List.of()),
    /** Remove the mark from all tasks in the checklist */
    UNMARK_ALL("unmark all", List.of()),
    /** Remove the mark from all tasks that matches the filters */
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

    /**
     * Gets the {@code Code} with the given command name.
     *
     * @param codeString the name of the {@code Code} to retrieve
     * @return the {@code Code} with the given command name
     */
    public static Code fromCodeString(String codeString) {
        return BY_CODE_STRING.get(codeString);
    }

    /**
     * Gets the command name associated with this code.
     *
     * @return the command name
     */
    public String getCodeString() {
        return this.codeString;
    }

    /**
     * Gets the flag names associated with this code.
     *
     * @return the flag names
     */
    public List<String> getFlagNames() {
        return this.flagNames;
    }
}
