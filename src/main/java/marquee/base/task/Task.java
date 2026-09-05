package marquee.base.task;

import java.time.LocalDateTime;

/**
 * Base class for all tasks.
 * Provide fields for description, starting time, ending time and completion status.
 * Subclasses are identified by their associated {@link TaskTag}.
 *
 * @implSpec Subclasses must define their own task tag and unique label,
 * then override {@link #getTaskTag()} to use the new tag.
 * @see TaskTag
 */
public abstract class Task {
    /**
     * Gets the tag of this task.
     *
     * @return the tag of this task
     * @implSpec Must override to use the correct tag
     */
    public abstract TaskTag getTaskTag();

    private final String description;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private boolean isMarked;

    /**
     * Creates a new {@code Task} with the given description, starting time
     * and ending time, then mark it as either completed or incomplete.
     *
     * @param description the description of the task
     * @param start       when the task starts
     * @param end         when the task ends
     * @param isMarked    whether the task has been completed or not
     */
    public Task(String description, LocalDateTime start, LocalDateTime end, boolean isMarked){
        this.description = description;
        this.isMarked = isMarked;
        this.start = start;
        this.end = end;
    }

    /**
     * Convenience function to decorate completion status as a tag for display.
     *
     * @return the completion status of this task as a tag
     */
    protected String getMarkTag() {
        return this.isMarked()? "[x]" : "[ ]";
    }

    /**
     * Gets the description of this task.
     *
     * @return the description of this task
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Get the starting time of this event.
     *
     * @return the starting time of this event
     */
    public LocalDateTime getStart() throws UnsupportedOperationException {
        return this.start;
    }

    /**
     * Get the ending time of this event.
     *
     * @return the ending time of this event
     */
    public LocalDateTime getEnd() throws UnsupportedOperationException {
        return this.end;
    }

    /**
     * Gets the completion status of this task.
     *
     * @return whether this task has been completed or not
     */
    public boolean isMarked() {
        return this.isMarked;
    }

    /**
     * Marks the task as completed.
     *
     * @return whether the task was incomplete before
     */
    public boolean mark() {
        return this.isMarked != (this.isMarked = true);
    }

    /**
     * Marks the task as incomplete.
     *
     * @return whether the task was completed before
     */
    public boolean unmark() {
        return this.isMarked != (this.isMarked = false);
    }

    @Override
    public String toString() {
        return this.getTaskTag().toString() + " " +  this.getMarkTag() + " " + this.getDescription();
    }
}