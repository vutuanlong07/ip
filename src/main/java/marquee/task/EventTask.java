package marquee.task;

import java.time.LocalDateTime;
import java.util.Map;

import marquee.base.task.Task;
import marquee.base.task.TaskTag;
import marquee.base.time.DateTimeFormatter;

/**
 * Representation of an event with a concrete starting and ending time.
 *
 * @see Task
 */
public final class EventTask extends Task {
    private static final String DESCRIPTION_COLUMN = "description";
    private static final String START_COLUMN = "start";
    private static final String END_COLUMN = "end";

    /**
     * Creates a new {@code EventTask} with the given description,
     * starting time and ending time, then mark it as either completed or incomplete.
     *
     * @param description the description of the event
     * @param start       when the event starts
     * @param end         when the event ends
     * @param isMarked    whether the event has been completed or not
     */
    public EventTask(String description, LocalDateTime start, LocalDateTime end, boolean isMarked)
            throws IllegalArgumentException {
        super(description, start, end, isMarked);
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    /**
     * Creates a new {@code EventTask} with the given description,
     * starting time and ending time, then mark it as incomplete.
     *
     * @param description the description of the event
     * @param start       when the event starts
     * @param end         when the event ends
     */
    public EventTask(String description, LocalDateTime start, LocalDateTime end) throws IllegalArgumentException {
        this(description, start, end, false);
    }

    /**
     * @see Task#Task()
     */
    public EventTask() {
        super();
    }

    @Override
    public TaskTag<EventTask> getTaskTag() {
        return BaseTags.EVENT_TAG;
    }

    @Override
    public Map<String, String> toValueMap() {
        return Map.of(
                DESCRIPTION_COLUMN, this.getDescription(),
                START_COLUMN, this.getStart().toString(),
                END_COLUMN, this.getEnd().toString()
        );
    }

    @Override
    public void fromValueMap(Map<String, String> values) {
        this.setDescription(values.get(DESCRIPTION_COLUMN));
        this.setStart(LocalDateTime.parse(values.get(START_COLUMN)));
        this.setEnd(LocalDateTime.parse(values.get(END_COLUMN)));
    }

    @Override
    public String toString() {
        return super.toString()
                + " (from " + DateTimeFormatter.formatDateTime(this.getStart())
                + " to " + DateTimeFormatter.formatDateTime(this.getEnd()) + ")";
    }
}
