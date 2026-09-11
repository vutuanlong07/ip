package org.cs2103t.marquee.cli.command;

import org.cs2103t.marquee.core.Marquee;

/**
 * Data class for codes that base {@code Marquee} support.
 *
 * @see Marquee
 */
public final class Codes {
    /** Ends the chat session */
    public static final Code EXIT = new Code("bye");
    /** Loads the checklist from the save file */
    public static final Code LOAD = new Code("load");
    /** Saves the checklist to the save file */
    public static final Code SAVE = new Code("save");
    /** Lists all tasks in the checklist */
    public static final Code LIST = new Code("list");
    /** Searches for tasks that matches the filters */
    public static final Code FIND = new Code("find",
            "", "from", "to", "completed", "incomplete"
    );
    /** Create a new to-do task and add it to the checklist */
    public static final Code TODO = new Code("todo", "", "completed");
    /** Create a new task with deadline and add it to the checklist */
    public static final Code DEADLINE = new Code("deadline",
            "", "by", "completed", "incomplete"
    );
    /** Create a new event and add it to the checklist */
    public static final Code EVENT = new Code("event",
            "", "from", "to", "completed", "incomplete"
    );
    /** Delete the tasks at the given indices */
    public static final Code DELETE = new Code("delete", "");
    /** Delete all tasks in the checklist */
    public static final Code DELETE_ALL = new Code("delete-all");
    /** Delete all tasks that matches the filters */
    public static final Code DELETE_MATCHING = new Code("delete-matching",
            "", "from", "to", "completed", "incomplete"
    );
    /** Mark the tasks at the given indices as completed */
    public static final Code MARK = new Code("mark", "");
    /** Mark all tasks in the checklist as completed */
    public static final Code MARK_ALL = new Code("mark-all");
    /** Mark all tasks that matches the filters as completed*/
    public static final Code MARK_MATCHING = new Code("mark-matching",
            "", "from", "to", "completed", "incomplete"
    );
    /** Remove the mark from the tasks at the given indices */
    public static final Code UNMARK = new Code("unmark", "");
    /** Remove the mark from all tasks in the checklist */
    public static final Code UNMARK_ALL = new Code("unmark-all");
    /** Remove the mark from all tasks that matches the filters */
    public static final Code UNMARK_MATCHING = new Code("unmark-matching",
            "", "from", "to", "completed", "incomplete"
    );

    // prevent instantiation
    private Codes() {}
}
