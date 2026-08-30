package tasks;

import time.DateTimeFormatter;

import java.time.LocalDateTime;

public final class EventItem extends TaskItem {
    private LocalDateTime start;
    private LocalDateTime end;

    public EventItem(String content, LocalDateTime start, LocalDateTime end, boolean marked) {
        super(content, ItemTag.Event, marked);
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        this.start = start;
        this.end = end;
    }
    public EventItem(String content, LocalDateTime start, LocalDateTime end) {
        super(content, ItemTag.Event);
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        this.start = start;
        this.end = end;
    }

    public LocalDateTime start() {
        return this.start;
    }

    public LocalDateTime end() {
        return this.end;
    }

    @Override
    public String toString() {
        return super.toString() + " (from: " + DateTimeFormatter.formatDateTime(this.start()) + " to: " + DateTimeFormatter.formatDateTime(this.end()) + ")";
    }
}
