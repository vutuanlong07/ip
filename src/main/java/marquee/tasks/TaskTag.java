package marquee.tasks;

/**
 * Tags for {@link TodoTask}
 */
public enum TaskTag {
    /** Tag for a to-do task */
    TODO("T"),
    /** Tag for a deadline */
    DEADLINE("D"),
    /** Tag for an event */
    EVENT("E");

    private final String label;

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

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "[" + label + "]";
    }
}
