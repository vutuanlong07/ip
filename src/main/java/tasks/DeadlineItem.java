package tasks;

import time.DateTimeFormatter;

import java.time.LocalDateTime;

public final class DeadlineItem extends TaskItem {
    private LocalDateTime deadline;

    public DeadlineItem(String content, LocalDateTime deadline, boolean marked) {
        super(content, ItemTag.Deadline, marked);
        this.deadline = deadline;
    }
    public DeadlineItem(String content, LocalDateTime deadline) {
        super(content, ItemTag.Deadline);
        this.deadline = deadline;
    }

    public LocalDateTime deadline() {
        return this.deadline;
    }

    @Override
    public String toString() {
        return super.toString() + " (by: " + DateTimeFormatter.formatDateTime(this.deadline()) + ")";
    }
}
