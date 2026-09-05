package marquee.task;

import java.time.LocalDateTime;

import marquee.task.tag.TaskTag;

/**
 * Representation of a generic task with no starting or ending time.
 *
 * @see Task
 */
public final class TodoTask extends Task {
    /** Tag for a to-do task */
    private static final TaskTag TODO_TASK_TAG = new TaskTag("T");

    @Override
    public TaskTag getTaskTag() {
        return TODO_TASK_TAG;
    }

    /**
     * Creates a new {@code TodoTask} with the given description,
     * then mark it as either completed or incomplete.
     *
     * @param description the description of the task
     * @param isMarked    whether the task has been completed or not
     */
    public TodoTask(String description, boolean isMarked){
        super(description, null, null, isMarked);
    }

    /**
     * Creates a new {@code TodoTask} with the given description
     *
     * @param description the description of the task
     */
    public TodoTask(String description) {
        this(description, false);
    }

    @Override
    public LocalDateTime getStart() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("To-do task does not have a starting time");
    }

    @Override
    public LocalDateTime getEnd() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("To-do task does not have an ending time");
    }
}