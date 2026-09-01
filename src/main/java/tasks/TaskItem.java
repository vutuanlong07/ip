package tasks;

import java.time.LocalDateTime;

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

    private boolean isMarked;
    private final String content;
    private final ItemTag tag;

    public TaskItem(String content, ItemTag tag, boolean isMarked) {
        this.isMarked = isMarked;
        this.content = content;
        this.tag = tag;
    }
    public TaskItem(String content, ItemTag tag) {
        this(content, tag, false);
    }

    public boolean isMarked() {
        return this.isMarked;
    }

    public boolean isUnmarked() {
        return !this.isMarked;
    }

    public String content() {
        return this.content;
    }

    public ItemTag tag() {
        return this.tag;
    }

    public boolean mark() {
        return this.isMarked != (this.isMarked = true);
    }

    public boolean unmark() {
        return this.isMarked != (this.isMarked = false);
    }

    public boolean isBefore(LocalDateTime end) {
        return false;
    }

    public boolean isAfter(LocalDateTime start) {
        return false;
    }

    @Override
    public String toString() {
        return (this.tag() == null? "[?]" : this.tag().toString()) + (this.isMarked()? " [x] " : " [ ] ") + this.content();
    }
}