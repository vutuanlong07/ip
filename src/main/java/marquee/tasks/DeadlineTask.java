package marquee.tasks;

import java.time.LocalDateTime;

import marquee.time.DateTimeFormatter;

/**
 * Representation of a task that must be completed within a deadline.
 * It is modeled as a {@link TodoTask} that has an ending time.
 */
public final class DeadlineTask extends TodoTask {
    private final LocalDateTime deadline;

    /**
     * Creates a new task with deadline with the specified description
     * and mark it as either completed or incomplete.
     *
     * @param description the description of the task
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
