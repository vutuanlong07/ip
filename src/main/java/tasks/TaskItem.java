package tasks;

public abstract class TaskItem {
    public enum ItemTag {
        Todo("T"),
        Deadline("D"),
        Event("E");

        public final String label;

        public static ItemTag fromLabel(String label) {
            for (ItemTag tag : values()) {
                if (java.util.Objects.equals(tag.label, label)) {
                    return tag;
                }
            }
            return null;
        }

        ItemTag(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return "[" + label + "]";
        }
    }

    private boolean marked;
    private final String content;
    private final ItemTag tag;

    public TaskItem(String content, ItemTag tag, boolean marked) {
        this.marked = marked;
        this.content = content;
        this.tag = tag;
    }
    public TaskItem(String content, ItemTag tag) {
        this(content, tag, false);
    }

    public boolean marked() {
        return this.marked;
    }

    public String content() {
        return this.content;
    }

    public ItemTag tag() {
        return this.tag;
    }

    public boolean mark() {
        return this.marked != (this.marked = true);
    }

    public boolean unmark() {
        return this.marked != (this.marked = false);
    }

    @Override
    public String toString() {
        return (this.tag() == null? "[?]" : this.tag().toString()) + (this.marked()? " [x] " : " [ ] ") + this.content();
    }
}