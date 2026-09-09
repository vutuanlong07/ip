package marquee.task;

import marquee.base.task.TaskTag;

/**
 * Data class for task tags used by {@code TodoTask}, {@code DeadlineTask} and {@code EventTask}.
 *
 * @see TodoTask
 * @see DeadlineTask
 * @see EventTask
 */
public class BaseTags {
    /** Tag for a task with deadline */
    public static final TaskTag DEADLINE_TAG = new TaskTag("D");
    /** Tag for an event */
    public static final TaskTag EVENT_TAG = new TaskTag("E");
    /** Tag for a to-do task */
    public static final TaskTag TODO_TAG = new TaskTag("T");

    // prevent instantiation
    private BaseTags() {}
}
