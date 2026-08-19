public abstract class TaskItem {
    private boolean marked;
    private String content;

    public TaskItem(String content) {
        this.marked = false;
        this.content = content;
    }

    public boolean marked() {
        return this.marked;
    }

    public String content() {
        return this.content;
    }

    public boolean mark() {
        return this.marked != (this.marked = true);
    }

    public boolean unmark() {
        return this.marked != (this.marked = false);
    }

    @Override
    public String toString() {
        return "[?]" + (this.marked()? "[x] " : "[ ] ") + this.content();
    }
}