package marquee.tasks;

public enum TaskTag {
    TODO("T"),
    DEADLINE("D"),
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
