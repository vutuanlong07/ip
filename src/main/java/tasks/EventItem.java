package tasks;

import time.DateTimeFormatter;

import java.time.LocalDateTime;

public final class EventItem extends TaskItem {
    private final LocalDateTime start;
    private final LocalDateTime end;

    public EventItem(String content, LocalDateTime start, LocalDateTime end, boolean isMarked) {
        super(content, ItemTag.Event, isMarked);
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        this.start = start;
        this.end = end;
    }
    public EventItem(String content, LocalDateTime start, LocalDateTime end) throws IllegalArgumentException {
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
    public boolean isBefore(LocalDateTime start) {
        return this.end.isBefore(start);
    }

    @Override
    public boolean isAfter(LocalDateTime end) {
        return this.start.isAfter(end);
    }

    @Override
    public String toString() {
        return super.toString() + " (from: " + DateTimeFormatter.formatDateTime(this.start()) + " to: " + DateTimeFormatter.formatDateTime(this.end()) + ")";
    }
}
