package marquee.tasks;

/**
 * Tags for {@code TodoTask}
 *
 * @see TodoTask
 * @author Vu Tuan Long
 */
public enum TaskTag {
    /** Tag for a to-do task */
    TODO("T"),
    /** Tag for a deadline */
    DEADLINE("D"),
    /** Tag for an event */
    EVENT("E");

    private final String label;

    /**
     * Gets the {@code TaskTag} with the given label
     *
     * @param label the label displayed by the {@code TaskTag} when invoking {@link #toString()}
     * @return the {@code TaskTag} with the given label
     */
    public static TaskTag fromLabel(String label) {
        for (TaskTag tag : values()) {
            if (tag.label.equals(label)) {
                return tag;
            }
        }
        return null;
    }

    TaskTag(String label) {
        this.label = label;
    }

    /**
     * Gets the label of this tag. The label is what would be displayed
     * when the tag is converted to a {@code String}
     *
     * @return the label of this tag
     */
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "[" + label + "]";
    }
}
