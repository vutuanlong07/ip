package marquee.tasks;

import java.time.LocalDateTime;

import marquee.time.DateTimeFormatter;

/**
 * Representation of a task with a deadline, modeled as a {@link TodoTask}
 * that has an ending time.
 *
 * @author Vu Tuan Long
 */
public final class DeadlineTask extends TodoTask {
    private final LocalDateTime deadline;

    /**
     * Creates a new task with deadline with the specified description
     * and mark it as either completed or incomplete.
     *
     * @param description description of the task
     * @param deadline    when the deadline is up
     * @param isMarked    whether the task is completed or not
     */
    public DeadlineTask(String description, LocalDateTime deadline, boolean isMarked) {
        super(TaskTag.DEADLINE, description, isMarked);
        this.deadline = deadline;
    }

    /**
     * Creates a new task with deadline with the specified description.
     *
     * @param description description of the task
     * @param deadline    when the deadline is up
     */
    public DeadlineTask(String description, LocalDateTime deadline) {
        this(description, deadline, false);
    }

    public LocalDateTime deadline() {
        return this.deadline;
    }

    @Override
    public boolean isBefore(LocalDateTime time) {
        return this.deadline.isBefore(time);
    }

    @Override
    public boolean isAfter(LocalDateTime time) {
        return this.deadline.isAfter(time);
    }

    @Override
    public String toString() {
        return super.toString()
                + " (by " + DateTimeFormatter.formatDateTime(this.deadline()) + ")";
    }
}
