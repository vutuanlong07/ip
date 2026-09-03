package marquee.tasks;

import java.time.LocalDateTime;

import marquee.time.DateTimeFormatter;

public final class DeadlineItem extends TaskItem {
    private final LocalDateTime deadline;

    public DeadlineItem(String content, LocalDateTime deadline, boolean isMarked) {
        super(content, ItemTag.DEADLINE, isMarked);
        this.deadline = deadline;
    }
    public DeadlineItem(String content, LocalDateTime deadline) {
        super(content, ItemTag.DEADLINE);
        this.deadline = deadline;
    }

    public LocalDateTime deadline() {
        return this.deadline;
    }

    @Override
    public boolean isBefore(LocalDateTime start) {
        return this.deadline.isBefore(start);
    }

    @Override
    public boolean isAfter(LocalDateTime end) {
        return this.deadline.isAfter(end);
    }

    @Override
    public String toString() {
        return super.toString()
                + " (by " + DateTimeFormatter.formatDateTime(this.deadline()) + ")";
    }
}
