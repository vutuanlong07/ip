package tasks;

import time.DateTimeFormatter;

import java.time.LocalDateTime;

public final class DeadlineItem extends TaskItem {
    private final LocalDateTime deadline;

    public DeadlineItem(String content, LocalDateTime deadline, boolean isMarked) {
        super(content, ItemTag.Deadline, isMarked);
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
