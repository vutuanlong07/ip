package marquee.task;

import java.util.Map;

import marquee.base.task.Task;
import marquee.base.task.TaskTag;

/**
 * Representation of a generic task with no starting or ending time.
 *
 * @see Task
 */
public final class TodoTask extends Task {
    private static final String DESCRIPTION_COLUMN = "description";

    /**
     * Creates a new {@code TodoTask} with the given description,
     * then mark it as either completed or incomplete.
     *
     * @param description the description of the task
     * @param isMarked    whether the task has been completed or not
     */
    public TodoTask(String description, boolean isMarked) {
        super(description, null, null, isMarked);
    }

    /**
     * Creates a new {@code TodoTask} with the given description,
     * then mark it as incomplete.
     *
     * @param description the description of the task
     */
    public TodoTask(String description) {
        this(description, false);
    }

    /**
     * @see Task#Task()
     */
    public TodoTask() {
        super();
    }

    @Override
    public TaskTag<TodoTask> getTaskTag() {
        return BaseTags.TODO_TAG;
    }

    @Override
    public Map<String, String> toValueMap() {
        return Map.of(
                DESCRIPTION_COLUMN, this.getDescription()
        );
    }

    @Override
    public void fromValueMap(Map<String, String> values) {
        this.setDescription(values.get(DESCRIPTION_COLUMN));
    }
}
