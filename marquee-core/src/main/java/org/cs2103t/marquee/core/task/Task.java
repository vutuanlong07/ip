package org.cs2103t.marquee.core.task;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

/**
 * Base class for all tasks.
 * <p>
 * Provide fields for task type, description, starting time, ending time and completion status.
 *
 * @see TaskTag
 */
public class Task {
    public static final String TAG_COLUMN = "tag";
    public static final String DESCRIPTION_COLUMN = "description";
    public static final String MARK_COLUMN = "mark";
    public static final String START_COLUMN = "start";
    public static final String END_COLUMN = "end";

    private static final String TAG_DELIMITER = ",";
    private static final Pattern TAG_DELIMITER_PATTERN = Pattern.compile(TAG_DELIMITER);

    private final StringProperty description = new SimpleStringProperty(this, "description");
    private final BooleanProperty isMarked = new SimpleBooleanProperty(this, "isMarked");
    private final ObjectProperty<LocalDateTime> start = new SimpleObjectProperty<>(this, "start");
    private final ObjectProperty<LocalDateTime> end = new SimpleObjectProperty<>(this, "end");
    private final SetProperty<TaskTag> tags =
            new SimpleSetProperty<>(this, "tags", FXCollections.observableSet());

    /**
     * Creates a new {@code Task} with the given description, starting time
     * and ending time, then mark it as either completed or incomplete.
     *
     * @param description the description of the task
     * @param start when the task starts
     * @param end when the task ends
     * @param isMarked whether the task has been completed or not
     * @throws NullPointerException if tag or description is {@code null}
     * @throws IllegalArgumentException if description is empty
     */
    public Task(String description, boolean isMarked, LocalDateTime start, LocalDateTime end, TaskTag... tags)
            throws NullPointerException, IllegalArgumentException {
        this.setDescription(description);
        this.setMark(isMarked);
        this.setStart(start);
        this.setEnd(end);
        for (TaskTag tag : tags) {
            this.addTag(tag);
        }
    }

    public Task() {
        this("New Task", false, null, null);
    }

    /**
     * Gets the tag of this task.
     *
     * @return the tag of this task
     */
    public ObservableSet<TaskTag> getTags() {
        return tags.get();
    }

    /**
     * Sets the tag of this task. This operation overrides old tags.
     *
     * @param newTags the new tags for this task
     */
    public void setTags(ObservableSet<TaskTag> newTags) {
        if (newTags == null) {
            throw new NullPointerException("Tag list cannot be null");
        } else {
            tags.forEach(this::removeTag);
            newTags.forEach(this::addTag);
        }
    }

    /**
     * Adds a tag to this task.
     *
     * @param newTag the new tag for this task
     * @return whether the task already has the given tag
     * @throws NullPointerException if the given tag is {@code null}
     * @throws IllegalArgumentException if the given tag is stale
     */
    public boolean addTag(TaskTag newTag) {
        if (newTag == null) {
            throw new NullPointerException("Tag cannot be null");
        } else {
            newTag.onTagAdded(this);
            return tags.add(newTag);
        }
    }

    /**
     * Removes a tag from this task.
     *
     * @param oldTag the old tag to remove from this task
     * @return whether the task didn't have the given tag
     * @throws NullPointerException if the given tag is {@code null}
     */
    public boolean removeTag(TaskTag oldTag) {
        if (oldTag == null) {
            throw new NullPointerException("Tag cannot be null");
        } else {
            oldTag.onTagRemoved(this);
            return tags.remove(oldTag);
        }
    }

    /**
     * Gets the description of this task.
     *
     * @return the task description
     */
    public String getDescription() {
        return this.description.get();
    }

    /**
     * Sets the description of this task.
     *
     * @param newDescription the new task description
     */
    public void setDescription(String newDescription) throws NullPointerException {
        if (newDescription == null) {
            throw new NullPointerException("Description cannot be null");
        }
        if (newDescription.isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        this.description.set(newDescription);
    }

    /**
     * Gets the description property of this task.
     *
     * @return the description property
     */
    public StringProperty descriptionProperty() {
        return this.description;
    }

    /**
     * Gets the starting time of this event, or {@code null} if not applicable.
     *
     * @return the starting time, or {@code null} if not applicable
     */
    public LocalDateTime getStart() {
        return start.get();
    }

    /**
     * Sets the starting time of this event.
     *
     * @param newStart the new starting time, or {@code null} if not applicable
     */
    public void setStart(LocalDateTime newStart) {
        start.set(newStart);
    }

    /**
     * Gets the starting time property of this event.
     *
     * @return the starting time property
     */
    public ObjectProperty<LocalDateTime> startProperty() {
        return start;
    }

    /**
     * Checks if the task starts strictly after the given time.
     *
     * @param startTime the reference time
     * @return whether the task starts after the given time
     */
    public boolean startsAfter(LocalDateTime startTime) {
        return this.start.get() != null && this.start.get().isAfter(startTime);
    }

    /**
     * Checks if the task starts after or at the given time.
     *
     * @param startTime the reference time
     * @return whether the task starts after or at the given time
     */
    public boolean startsAfterInclusive(LocalDateTime startTime) {
        return this.start.get() != null && !this.start.get().isBefore(startTime);
    }

    /**
     * Gets the ending time of this event, or {@code null} if not applicable.
     *
     * @return the ending time, or {@code null} if not applicable
     */
    public LocalDateTime getEnd() {
        return end.get();
    }

    /**
     * Sets the ending time of this event.
     *
     * @param newEnd the new ending time, or {@code null} if not applicable
     */
    public void setEnd(LocalDateTime newEnd) {
        end.set(newEnd);
    }

    /**
     * Gets the ending time of this event, or {@code null} if not applicable.
     *
     * @return the ending time, or {@code null} if not applicable
     */
    public ObjectProperty<LocalDateTime> endProperty() {
        return end;
    }

    /**
     * Checks if the task ends strictly before the given time.
     *
     * @param endTime the reference time
     * @return whether the task ends before the given time
     */
    public boolean endsBefore(LocalDateTime endTime) {
        return this.end.get() != null && this.end.get().isBefore(endTime);
    }

    /**
     * Checks if the task ends before or at the given time.
     *
     * @param endTime the reference time
     * @return whether the task ends before or at the given time
     */
    public boolean endsBeforeInclusive(LocalDateTime endTime) {
        return this.end.get() != null && !this.end.get().isAfter(endTime);
    }

    /**
     * Gets the mark status of this task.
     *
     * @return whether this task is marked
     */
    public boolean isMarked() {
        return isMarked.get();
    }

    /**
     * Sets the task mark status.
     *
     * @param newMark the new mark status for the task
     * @return whether the mark status was changed
     */
    public boolean setMark(boolean newMark) {
        if (isMarked.get() != newMark) {
            isMarked.set(newMark);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the marked property of this task.
     *
     * @return the marked property
     */
    public BooleanProperty markProperty() {
        return isMarked;
    }

    /**
     * Marks the task as completed.
     *
     * @return whether the task was incomplete before
     */
    public boolean mark() {
        return setMark(true);
    }

    /**
     * Marks the task as incomplete.
     *
     * @return whether the task was completed before
     */
    public boolean unmark() {
        return setMark(false);
    }

    /**
     * Returns a mapping of property names to their values.
     * <p>
     * Used for storing tasks in a file.
     *
     * @return a map from property name to string representation of their values
     * @implSpec The property name produced by this method must reproduce the same task
     *           when passed to {@link #fromValueMap} with respect to {@code Task} attributes.
     */
    public Map<String, String> toValueMap() {
        return Map.of(
                TAG_COLUMN, this.getTags().stream()
                        .map(TaskTag::toString)
                        .collect(Collectors.joining(TAG_DELIMITER)),
                DESCRIPTION_COLUMN, this.getDescription(),
                START_COLUMN, this.getStart() == null ? "" : this.getStart().toString(),
                END_COLUMN, this.getEnd() == null ? "" : this.getEnd().toString(),
                MARK_COLUMN, Boolean.toString(this.isMarked())
        );
    }

    /**
     * Creates a task with the given properties.
     * <p>
     * Used for reconstructing tasks from files.
     *
     * @param values a map from property name to string representation of their values
     * @return a {@code Task} with the given properties
     */
    public static Task fromValueMap(Map<String, String> values) {
        Task newTask = new Task();
        newTask.setValue(values);
        return newTask;
    }

    /**
     * Assigns values to this task's properties.
     * <p>
     * Used for reconstructing tasks from files.
     *
     * @param values a map from property name to string representation of their values
     */
    public void setValue(Map<String, String> values) {
        if (!(values.get(TAG_COLUMN) == null || values.get(TAG_COLUMN).isEmpty())) {
            this.tags.clear();
            this.tags.addAll(TAG_DELIMITER_PATTERN.splitAsStream(values.get(TAG_COLUMN))
                    .map(TaskTag::getTaskTag)
                    .collect(Collectors.toSet()));
        }
        if (!(values.get(DESCRIPTION_COLUMN) == null || values.get(DESCRIPTION_COLUMN).isEmpty())) {
            this.setDescription(values.get(DESCRIPTION_COLUMN));
        }
        if (!(values.get(MARK_COLUMN) == null || values.get(MARK_COLUMN).isEmpty())) {
            this.setMark(Boolean.parseBoolean(values.get(MARK_COLUMN)));
        }
        if (!(values.get(START_COLUMN) == null || values.get(START_COLUMN).isEmpty())) {
            this.setStart(LocalDateTime.parse(values.get(START_COLUMN)));
        }
        if (!(values.get(END_COLUMN) == null || values.get(END_COLUMN).isEmpty())) {
            this.setEnd(LocalDateTime.parse(values.get(END_COLUMN)));
        }
    }

    @Override
    public String toString() {
        return this.getTags().stream().map(tag -> "[" + tag + "]").collect(Collectors.joining(" "))
                + (this.isMarked() ? " [x] " : " [ ] ") + this.getDescription();
    }
}
