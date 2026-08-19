public class DeadlineItem extends TaskItem {
    private String deadline;

    public DeadlineItem(String content, String deadline) {
        super(content);
        this.deadline = deadline;
    }

    public String deadline() {
        return this.deadline;
    }

    @Override
    public String toString() {
        return "[D]" + (this.marked()? "[x] " : "[ ] ") + this.content() + " (by: " + this.deadline() + ")";
    }
}
