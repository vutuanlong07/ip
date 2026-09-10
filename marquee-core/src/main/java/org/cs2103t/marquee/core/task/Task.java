package marquee.base.task;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Base class for all tasks.
 * <p>
 * Provide fields for description, starting time, ending time and completion status.
 * Task type is determined by the class' associated {@link TaskTag}.
 *
 * @implSpec <ul>
 *           <li>Subclasses must define their own task tag and unique label,
 *           then override {@link #getTaskTag()} to use the new tag.</li>
 *           <li>Subclasses must implement an empty constructor for use with {@link #fromValueMap}</li>
 *           </ul>
 * @see TaskTag
 */
public abstract class Task {
    public static final String TAG_COLUMN = "tag";

    private String description;
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean isMarked;

    /**
     * Creates a new {@code Task} with the given description, starting time
     * and ending time, then mark it as either completed or incomplete.
     *
     * @param description the description of the task
     * @param start       when the task starts
     * @param end         when the task ends
     * @param isMarked    whether the task has been completed or not
     * @throws NullPointerException if description is {@code null}
     */
    public Task(String description, LocalDateTime start, LocalDateTime end, boolean isMarked)
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
     * Creates a new {@code Task} with an empty description, no starting time
     * or ending time, and mark it as incomplete.
     */
    public Task() {
        this("", null, null, false);
    }

    /**
     * Reconstructs a task from a task tag and a map from field names to their values.
     * <p>
     * Depends on {@link java.lang.reflect}. Use with caution.
     *
     * @param <T> the type of the task to reconstruct
     * @param taskTag the tag of the task class to recreate
     * @param values  a map of field names to the string representation of their values
     *                of the task to reconstruct
     * @return the reconstructed task
     * @throws NoSuchMethodException     if the subclass doesn't implement an empty constructor
     * @throws InvocationTargetException if the subclass constructor throws an exception
     * @throws InstantiationException    if the subclass is abstract
     * @throws IllegalAccessException    if the subclass empty constructor is inaccessible due to access modifiers
     */
    public static <T extends Task> T reconstructTask(TaskTag<T> taskTag, Map<String, String> values)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        T task = taskTag.getTaskClass().getConstructor().newInstance();
        task.fromValueMap(values);
        return task;
    }
    /**
     * Returns a mapping of property names to their values.
     * <p>
     * Used for storing tasks in a file.
     *
     * @return a map from property name to string representation of their values
     * @implSpec The property name produced by this method and used by {@link #fromValueMap} must be consistent.
     */
    public abstract Map<String, String> toValueMap();

    /**
     * Assigns values to this task's attributes by attribute names.
     * <p>
     * Used for reconstructing tasks from files.
     *
     * @param values a map from property name to string representation of their values
     * @implSpec <ul>
     *           <li>The property name produced by this method and used by {@link #toValueMap} must be consistent.</li>
     *           <li>This method should be permissive - unknown keys in the map should be ignored.</li>
     *           </ul>
     */
    public abstract void fromValueMap(Map<String, String> values);

    /**
     * Convenience function to decorate completion status as a tag for display.
     *
     * @return the completion status of this task as a tag
     */
    protected String getMarkTag() {
        return this.isMarked() ? "[x]" : "[ ]";
    }

    /**
     * Gets the tag of this task.
     *
     * @return the tag of this task
     * @implSpec Must return the same constant for every class instance.
     */
    public abstract TaskTag<?> getTaskTag();

    /**
     * Gets the description of this task.
     *
     * @return the description of this task
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the description of this task.
     *
     * @param newDescription the new description of this task
     */
    protected void setDescription(String newDescription) throws NullPointerException {
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
    public LocalDateTime getStart() {
        return this.start;
    }

    /**
     * Sets the starting time of this event.
     *
     * @param newStart the new starting time of this event, or {@code null} if not applicable
     */
    protected void setStart(LocalDateTime newStart) {
        this.start = newStart;
    }

    /**
     * Gets the ending time of this event, or {@code null} if not applicable.
     *
     * @return the ending time of this event, or {@code null} if not applicable
     */
    public LocalDateTime getEnd() {
        return this.end;
    }

    /**
     * Sets the ending time of this event.
     *
     * @param newEnd the new ending time of this event, or {@code null} if not applicable
     */
    protected void setEnd(LocalDateTime newEnd) {
        this.end = newEnd;
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
        return this.getTaskTag().toString() + " " + this.getMarkTag() + " " + this.getDescription();
    }
}
