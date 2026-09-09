package marquee.base.task;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Base class for tags used by {@code TodoTask} and its subclasses.
 * <p>
 * Do not create new instances repeatedly. All instances are tracked and may cause collisions.
 */
public final class TaskTag {
    private static final Map<String, TaskTag> TAG_BY_LABEL = new HashMap<>();

    private final String label;

    /**
     * Create a new {@code TaskTag}.
     * <p>
     * All tag instances must have unique labels.
     *
     * @param label the label of the tag, which is what
     *              would be displayed when {@link #toString()} is invoked
     * @throws NullPointerException     if the label is {@code null}
     * @throws IllegalArgumentException if a tag with this label already exist
     */
    public TaskTag(String label) {
        if (label == null) {
            throw new NullPointerException("Tag label cannot be null");
        }
        if (TAG_BY_LABEL.containsKey(label)) {
            throw new IllegalArgumentException("Tag already exists");
        }

        this.label = label;
        TAG_BY_LABEL.put(label, this);
    }

    /**
     * Gets the {@code TaskTag} with the given label
     *
     * @param label the label displayed by the {@code TaskTag} when invoking {@link #getLabel()}
     * @return the {@code TaskTag} with the given label, or {@code null} if there are none
     */
    public static TaskTag fromLabel(String label) {
        return TAG_BY_LABEL.get(label);
    }

    /**
     * Returns an unmodifiable view of available task tags<p>
     *
     * @return an unmodifiable {@link Collection} of available task tags
     */
    public static Collection<TaskTag> getAvailableTags() {
        return TAG_BY_LABEL.values();
    }

    public String getLabel() {
        return label;
    }

    /**
     * Returns the decorated version of the tag for printing<p>
     *
     * @return a decorated version of the tag
     */
    @Override
    public String toString() {
        return "[" + this.getLabel() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TaskTag && this.getLabel().equals(((TaskTag) obj).getLabel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(label);
    }

}
