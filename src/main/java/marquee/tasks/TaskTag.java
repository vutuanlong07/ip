package marquee.tasks;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for tags used by {@code TodoTask} and its subclasses.
 */
public record TaskTag(String label) {
    private static final Map<String, TaskTag> TAG_BY_LABEL = new HashMap<>();

    /**
     * Gets the {@code TaskTag} with the given label
     *
     * @param label the label displayed by the {@code TaskTag} when invoking {@link #label ()}
     * @return the {@code TaskTag} with the given label
     */
    public static TaskTag fromLabel(String label) {
        return TAG_BY_LABEL.getOrDefault(label, null);
    }

    /**
     * Create a new tag. Tag instances are considered unique if their labels are different.
     * All tag instances must be unique. Reuse old instances if you need to tag multiple tasks with the same tag.
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
     * Returns the decorated version of the tag for printing.
     *
     * @return a decorated version of the tag
     */
    @Override
    public String toString() {
        return "[" + this.label() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TaskTag && this.label().equals(((TaskTag) obj).label());
    }
}