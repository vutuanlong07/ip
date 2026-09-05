package marquee.task;

import java.time.LocalDateTime;

import marquee.task.tag.TaskTag;
import marquee.time.DateTimeFormatter;

/**
 * Representation of a task that must be completed within a deadline.
 * It is modeled as a {@link Task} that starts and end at the same time as the deadline.
 */
public final class DeadlineTask extends Task {
    /** Tag for a task with deadline */
    public static final TaskTag DEADLINE_TASK_TAG = new TaskTag("D");

    @Override
    public TaskTag getTaskTag() {
        return DEADLINE_TASK_TAG;
    }

    private final LocalDateTime deadline;

    /**
     * Creates a new {@code DeadlineTask} with the given description
     * and deadline, then mark it as either completed or incomplete.
     *
     * @param description the description of the task
     * @param deadline    when the deadline is up
     * @param isMarked    whether the task has been completed or not
     */
    public DeadlineTask(String description, LocalDateTime deadline, boolean isMarked) {
        super(description, deadline, deadline, isMarked);
        this.deadline = deadline;
    }

    /**
     * Creates a new {@code DeadlineTask} with the given description and deadline
     *
     * @param description the description of the task
     * @param deadline    when the deadline is up
     */
    public DeadlineTask(String description, LocalDateTime deadline) {
        this(description, deadline, false);
    }

    /**
     * Get the deadline of this task.
     * @return the deadline of this task
     */
    public LocalDateTime deadline() {
        return this.deadline;
    }

    @Override
    public String toString() {
        return super.toString()
                + " (complete by " + DateTimeFormatter.formatDateTime(this.deadline()) + ")";
    }
}
