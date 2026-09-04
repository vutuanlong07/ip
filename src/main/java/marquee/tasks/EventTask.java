package marquee.tasks;

import java.time.LocalDateTime;

import marquee.time.DateTimeFormatter;

public final class EventTask extends TodoTask {
    private final LocalDateTime start;
    private final LocalDateTime end;

    public EventTask(String content, LocalDateTime start, LocalDateTime end, boolean isMarked) throws IllegalArgumentException {
        super(TaskTag.EVENT, content, isMarked);
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        this.start = start;
        this.end = end;
    }
    public EventTask(String content, LocalDateTime start, LocalDateTime end) throws IllegalArgumentException {
        this(content, start, end, false);
    }

    public LocalDateTime getStart() {
        return this.start;
    }

    public LocalDateTime getEnd() {
        return this.end;
    }

    @Override
    public boolean isBefore(LocalDateTime start) {
        return this.end.isBefore(start);
    }

    @Override
    public boolean isAfter(LocalDateTime end) {
        return this.start.isAfter(end);
    }

    @Override
    public String toString() {
        return super.toString()
                + " (from " + DateTimeFormatter.formatDateTime(this.getStart())
                + " to " + DateTimeFormatter.formatDateTime(this.getEnd()) + ")";
    }
}
