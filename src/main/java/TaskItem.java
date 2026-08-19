public abstract class TaskItem {
    private boolean marked;
    private String content;

    public TaskItem(String content) {
        this.marked = false;
        this.content = content;
    }

    public boolean marked() {
        return marked;
    }

    public String content() {
        return content;
    }

    public boolean mark() {
        return this.marked != (this.marked = true);
    }

    public boolean unmark() {
        return this.marked != (this.marked = false);
    }

    @Override
    public String toString() {
        return (marked? "[?][x] " : "[?][ ] ") + content;
    }
}