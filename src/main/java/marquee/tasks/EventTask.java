package marquee.tasks;

import java.time.LocalDateTime;

import marquee.time.DateTimeFormatter;

/**
 * Representation of an event. It is modeled as a {@link TodoTask}
 * that has a starting time and an ending time.
 */
public final class EventTask extends TodoTask {
    private final LocalDateTime start;
    private final LocalDateTime end;

    /**
     * Creates a new event with the specified description
     * and mark it as either completed or incomplete.
     *
     * @param description description of the event
     * @param start       when the event starts
     * @param end         when the event ends
     * @param isMarked    whether the task is completed or not
     */
    public EventTask(String description, LocalDateTime start, LocalDateTime end, boolean isMarked) throws IllegalArgumentException {
        super(TaskTag.EVENT, description, isMarked);
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        this.start = start;
        this.end = end;
    }
    
    /**
     * Creates a new event with the specified description.
     *
     * @param description the description of the task
     */
    public EventTask(String description, LocalDateTime start, LocalDateTime end) throws IllegalArgumentException {
        this(description, start, end, false);
    }

    /**
     * Get the starting time of this event.
     * @return the starting time of this event
     */
    public LocalDateTime getStart() {
        return this.start;
    }

    /**
     * Get the ending time of this event.
     * @return the ending time of this event
     */
    public LocalDateTime getEnd() {
        return this.end;
    }

    @Override
    public boolean isBefore(LocalDateTime time) {
        return this.end.isBefore(time);
    }

    @Override
    public boolean isAfter(LocalDateTime time) {
        return this.start.isAfter(time);
    }

    @Override
    public String toString() {
        return super.toString()
                + " (from " + DateTimeFormatter.formatDateTime(this.getStart())
                + " to " + DateTimeFormatter.formatDateTime(this.getEnd()) + ")";
    }
}
