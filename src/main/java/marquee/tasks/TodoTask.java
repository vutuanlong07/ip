package marquee.tasks;

import java.time.LocalDateTime;

/**
 * Representation of a task with a description, tag and completion marker.
 *
 * @author Vu Tuan Long
 */
public class TodoTask {
    private boolean isMarked;
    private final String description;
    private final TaskTag tag;

    /**
     * Constructor reserved for subclasses that need to change task tag.
     *
     * @param tag         new tag for this task
     * @param description description of the task
     * @param isMarked    whether the task is completed or not
     * @throws NullPointerException If {@code tag} is {@code null}
     */
    protected TodoTask(TaskTag tag, String description, boolean isMarked) throws NullPointerException {
        if (tag == null) {
            throw new NullPointerException("Task tag cannot be null");
        }

        this.tag = tag;
        this.description = description;
        this.isMarked = isMarked;
    }

    /**
     * Creates a new task item with the specified description
     * and mark it as either completed or incomplete.
     *
     * @param description description of the task
     * @param isMarked    whether the task is completed
     */
    public TodoTask(String description, boolean isMarked){
        this(TaskTag.TODO, description, isMarked);
    }

    /**
     * Creates a new task item with the specified description.
     *
     * @param description description of the task
     */
    public TodoTask(String description) {
        this(description, false);
    }

    public boolean isMarked() {
        return this.isMarked;
    }

    public String getDescription() {
        return this.description;
    }

    public TaskTag getTag() {
        return this.tag;
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

    /**
     * Checks if the task ends before the given time.
     *
     * @param time the time to check against
     * @return whether the task ends before {@code time}
     */
    public boolean isBefore(LocalDateTime time) {
        return false;
    }

    /**
     * Checks if the task starts after the given time.
     *
     * @param time the time to check against
     * @return whether the task starts after {@code time}
     */
    public boolean isAfter(LocalDateTime time) {
        return false;
    }

    @Override
    public String toString() {
        return this.getTag().toString() + (this.isMarked()? " [x] " : " [ ] ") + this.getDescription();
    }
}