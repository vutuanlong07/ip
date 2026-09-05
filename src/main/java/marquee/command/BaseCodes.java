package marquee.command;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import marquee.base.command.Code;

/**
 * A dictionary of the base codes that {@code Marquee} support.
 */
public final class BaseCodes {
    /** Ends the chat session */
    public static final Code EXIT = new Code("bye", Collections.emptyList());
    /** Loads the checklist from the save file */
    public static final Code LOAD = new Code("load", Collections.emptyList());
    /** Saves the checklist to the save file */
    public static final Code SAVE = new Code("save", Collections.emptyList());
    /** Lists all tasks in the checklist */
    public static final Code LIST = new Code("list", Collections.emptyList());
    /** Searches for tasks that matches the filters */
    public static final Code FIND = new Code("find", Stream.of("from", "to", "completed", "incomplete").toList());
    /** Create a new to-do task and add it to the checklist */
    public static final Code TODO = new Code("todo", List.of("completed"));
    /** Create a new task with deadline and add it to the checklist */
    public static final Code DEADLINE = new Code("deadline", Stream.of("by", "completed", "incomplete").toList());
    /** Create a new event and add it to the checklist */
    public static final Code EVENT = new Code("event", Stream.of("from", "to", "completed", "incomplete").toList());
    /** Delete the tasks at the given indices */
    public static final Code DELETE = new Code("delete", Collections.singletonList(null));
    /** Delete all tasks in the checklist */
    public static final Code DELETE_ALL = new Code("delete all", Collections.emptyList());
    /** Delete all tasks that matches the filters */
    public static final Code DELETE_MATCHING = new Code("delete matching", Stream.of("from", "to", "completed", "incomplete").toList());
    /** Mark the tasks at the given indices as completed */
    public static final Code MARK = new Code("mark", Collections.singletonList(null));
    /** Mark all tasks in the checklist as completed */
    public static final Code MARK_ALL = new Code("mark all", Collections.emptyList());
    /** Mark all tasks that matches the filters as completed*/
    public static final Code MARK_MATCHING = new Code("mark matching", Stream.of("from", "to", "completed", "incomplete").toList());
    /** Remove the mark from the tasks at the given indices */
    public static final Code UNMARK = new Code("unmark", Collections.singletonList(null));
    /** Remove the mark from all tasks in the checklist */
    public static final Code UNMARK_ALL = new Code("unmark all", Collections.emptyList());
    /** Remove the mark from all tasks that matches the filters */
    public static final Code UNMARK_MATCHING = new Code("unmark matching", Stream.of("from", "to", "completed", "incomplete").toList());

    /** List of codes defined in this class */
    public static final List<Code> LIST_OF_CODES = List.of(
            EXIT, LOAD, SAVE, LIST, FIND,
            TODO, DEADLINE, EVENT,
            DELETE, DELETE_ALL, DELETE_MATCHING,
            MARK, MARK_ALL, MARK_MATCHING,
            UNMARK, UNMARK_ALL, UNMARK_MATCHING
    );
}