package marquee.task;

import java.time.LocalDateTime;
import java.util.Map;

import marquee.base.task.Task;
import marquee.base.task.TaskTag;
import marquee.base.time.DateTimeFormatter;

/**
 * Representation of a task that must be completed within a deadline.
 *
 * @see Task
 */
public final class DeadlineTask extends Task {
    private static final String DESCRIPTION_COLUMN = "description";
    private static final String DEADLINE_COLUMN = "end";

    /**
     * Creates a new {@code DeadlineTask} with the given description
     * and deadline, then mark it as either completed or incomplete.
     *
     * @param description the description of the task
     * @param deadline    when the deadline is up
     * @param isMarked    whether the task has been completed or not
     */
    public DeadlineTask(String description, LocalDateTime deadline, boolean isMarked) {
        super(description, null, deadline, isMarked);
    }

    /**
     * Creates a new {@code DeadlineTask} with the given description
     * and deadline, then mark it as incomplete.
     *
     * @param description the description of the task
     * @param deadline    when the deadline is up
     */
    public DeadlineTask(String description, LocalDateTime deadline) {
        this(description, deadline, false);
    }

    /**
     * @see Task#Task()
     */
    public DeadlineTask() {
        super();
    }

    /**
     * Get the deadline of this task.
     * <p>
     * Alias of {@link #getEnd()}.
     *
     * @return the deadline of this task
     */
    public LocalDateTime getDeadline() {
        return this.getEnd();
    }

    private void setDeadline(LocalDateTime newDeadline) {
        this.setEnd(newDeadline);
    }

    @Override
    public TaskTag<DeadlineTask> getTaskTag() {
        return BaseTags.DEADLINE_TAG;
    }

    @Override
    public Map<String, String> toValueMap() {
        return Map.of(
                DESCRIPTION_COLUMN, this.getDescription(),
                DEADLINE_COLUMN, this.getDeadline().toString()
        );
    }

    @Override
    public void fromValueMap(Map<String, String> values) {
        this.setDescription(values.get(DESCRIPTION_COLUMN));
        this.setDeadline(LocalDateTime.parse(values.get(DEADLINE_COLUMN)));
    }

    @Override
    public String toString() {
        return super.toString()
                + " (complete by " + DateTimeFormatter.formatDateTime(this.getDeadline()) + ")";
    }
}
