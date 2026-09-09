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
    public static final TaskTag<DeadlineTask> DEADLINE_TAG = new TaskTag<>("D", DeadlineTask.class);
    /** Tag for an event */
    public static final TaskTag<EventTask> EVENT_TAG = new TaskTag<>("E", EventTask.class);
    /** Tag for a to-do task */
    public static final TaskTag<TodoTask> TODO_TAG = new TaskTag<>("T", TodoTask.class);

    // prevent instantiation
    private BaseTags() {}
}
