package org.cs2103t.marquee.core.task;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Base class for all tasks.
 * <p>
 * Provide fields for task type, description, starting time, ending time and completion status.
 *
 * @see TaskTag
 */
public class Task {
    public static final String TAG_COLUMN = "tag";
    public static final String DESCRIPTION_COLUMN = "description";
    public static final String MARK_COLUMN = "mark";
    public static final String START_COLUMN = "start";
    public static final String END_COLUMN = "end";

    private TaskTag tag;
    private String description;
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean isMarked;

    /**
     * Creates a new {@code Task} with the given description, starting time
     * and ending time, then mark it as either completed or incomplete.
     *
     * @param description the description of the task
     * @param start when the task starts
     * @param end when the task ends
     * @param isMarked whether the task has been completed or not
     * @throws NullPointerException if description is {@code null}
     */
    public Task(TaskTag tag, String description, LocalDateTime start, LocalDateTime end, boolean isMarked)
            throws NullPointerException {
        if (description == null) {
            throw new NullPointerException("Description cannot be null");
        }
        this.description = description;
        this.isMarked = isMarked;
        this.start = start;
        this.end = end;
    }

    /**
     * Gets the tag of this task.
     *
     * @return the tag of this task
     */
    public final TaskTag getTag() {
        return this.tag;
    }

    /**
     * Sets the tag of this task.
     *
     * @param newTag the new tag for this task
     */
    public final void setTag(TaskTag newTag) {
        if (newTag == null) {
            throw new NullPointerException("Description cannot be null");
        } else {
            this.tag = newTag;
        }
    }

    /**
     * Gets the description of this task.
     *
     * @return the description of this task
     */
    public final String getDescription() {
        return this.description;
    }

    /**
     * Sets the description of this task.
     *
     * @param newDescription the new description for this task
     */
    protected final void setDescription(String newDescription) throws NullPointerException {
        if (newDescription == null) {
            throw new NullPointerException("Description cannot be null");
        }
        this.description = newDescription;
    }

    /**
     * Gets the starting time of this event, or {@code null} if not applicable.
     *
     * @return the starting time of this event, or {@code null} if not applicable
     */
    public final LocalDateTime getStart() {
        return this.start;
    }

    /**
     * Sets the starting time of this event.
     *
     * @param newStart the new starting time for this event, or {@code null} if not applicable
     */
    protected final void setStart(LocalDateTime newStart) {
        this.start = newStart;
    }

    /**
     * Checks if the task starts strictly after the given time.
     *
     * @param startTime the reference time
     * @return whether the task starts after the given time
     */
    public final boolean startsAfter(LocalDateTime startTime) {
        return this.start != null && this.start.isAfter(startTime);
    }

    /**
     * Checks if the task starts after or at the given time.
     *
     * @param startTime the reference time
     * @return whether the task starts after or at the given time
     */
    public final boolean startsAfterInclusive(LocalDateTime startTime) {
        return this.start != null && !this.start.isBefore(startTime);
    }

    /**
     * Gets the ending time of this event, or {@code null} if not applicable.
     *
     * @return the ending time of this event, or {@code null} if not applicable
     */
    public final LocalDateTime getEnd() {
        return this.end;
    }

    /**
     * Sets the ending time of this event.
     *
     * @param newEnd the new ending time for this event, or {@code null} if not applicable
     */
    protected final void setEnd(LocalDateTime newEnd) {
        this.end = newEnd;
    }

    /**
     * Checks if the task ends strictly before the given time.
     *
     * @param endTime the reference time
     * @return whether the task ends before the given time
     */
    public final boolean endsBefore(LocalDateTime endTime) {
        return this.end != null && this.end.isBefore(endTime);
    }

    /**
     * Checks if the task ends before or at the given time.
     *
     * @param endTime the reference time
     * @return whether the task ends before or at the given time
     */
    public final boolean endsBeforeInclusive(LocalDateTime endTime) {
        return this.end != null && !this.end.isAfter(endTime);
    }

    /**
     * Gets the completion status of this task.
     *
     * @return whether this task has been completed or not
     */
    public final boolean isMarked() {
        return this.isMarked;
    }

    /**
     * Sets the task mark status.
     *
     * @param newMark the new mark status for the task
     * @return whether the mark status was changed
     */
    public final boolean setMark(boolean newMark) {
        return this.isMarked != (this.isMarked = newMark);
    }

    /**
     * Marks the task as completed.
     *
     * @return whether the task was incomplete before
     */
    public final boolean mark() {
        return setMark(true);
    }

    /**
     * Marks the task as incomplete.
     *
     * @return whether the task was completed before
     */
    public final boolean unmark() {
        return setMark(false);
    }

    /**
     * Returns a mapping of property names to their values.
     * <p>
     * Used for storing tasks in a file.
     *
     * @return a map from property name to string representation of their values
     * @implSpec The property name produced by this method must reproduce the same task
     *           when passed to {@link #fromValueMap} with respect to {@code Task} attributes.
     */
    public Map<String, String> toValueMap() {
        return Map.of(
                TAG_COLUMN, this.getTag().getLabel(),
                DESCRIPTION_COLUMN, this.getDescription(),
                MARK_COLUMN, Boolean.toString(this.isMarked()),
                START_COLUMN, this.getStart().toString(),
                END_COLUMN, this.getEnd().toString()
        );
    }

    /**
     * Assigns values to this task's attributes by attribute names.
     * <p>
     * Used for reconstructing tasks from files.
     *
     * @param values a map from property name to string representation of their values
     * @return a {@code Task} with the given properties
     */
    public static Task fromValueMap(Map<String, String> values) {
        return new Task(
                TaskTag.fromLabel(values.get(TAG_COLUMN)),
                values.get(DESCRIPTION_COLUMN),
                LocalDateTime.parse(values.get(START_COLUMN)),
                LocalDateTime.parse(values.get(END_COLUMN)),
                Boolean.parseBoolean(values.get(MARK_COLUMN))
        );
    }

    @Override
    public String toString() {
        return this.getTag().toString() + (this.isMarked() ? " [x] " : " [ ] ") + this.getDescription();
    }
}
