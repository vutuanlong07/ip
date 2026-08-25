public final class DeadlineItem extends TaskItem {
    private String deadline;

    public DeadlineItem(String content, String deadline) {
        super(content, ItemTag.Deadline);
        this.deadline = deadline;
    }

    public String deadline() {
        return this.deadline;
    }

    @Override
    public String toString() {
        return super.toString() + " (by: " + this.deadline() + ")";
    }
}
