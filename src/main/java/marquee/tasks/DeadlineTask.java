package marquee.tasks;

import java.time.LocalDateTime;

import marquee.time.DateTimeFormatter;

public final class DeadlineTask extends TodoTask {
    private final LocalDateTime deadline;

    public DeadlineTask(String content, LocalDateTime deadline, boolean isMarked) {
        super(TaskTag.DEADLINE, content, isMarked);
        this.deadline = deadline;
    }
    public DeadlineTask(String content, LocalDateTime deadline) {
        this(content, deadline, false);
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
