package org.cs2103t.marquee.cli.task;

import org.cs2103t.marquee.core.task.TaskTag;

/**
 * Data class for task tags used by {@code TodoTask}, {@code DeadlineTask} and {@code EventTask}.
 *
 * @see TodoTask
 * @see DeadlineTask
 * @see EventTask
 */
public class TaskTags {
    /** Tag for a to-do task */
    public static final TaskTag TODO_TAG = new TaskTag("T");
    /** Tag for a task with deadline */
    public static final TaskTag DEADLINE_TAG = new TaskTag("D");
    /** Tag for an event */
    public static final TaskTag EVENT_TAG = new TaskTag("E");

    // prevent instantiation
    private TaskTags() {}
}
