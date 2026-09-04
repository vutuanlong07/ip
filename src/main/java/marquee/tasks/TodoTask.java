package marquee.tasks;

import java.time.LocalDateTime;

public class TodoTask {
    private boolean isMarked;
    private final String description;
    private final TaskTag tag;

    protected TodoTask(TaskTag tag, String description, boolean isMarked) throws NullPointerException {
        if (tag == null) {
            throw new NullPointerException("Task tag cannot be null");
        }

        this.tag = tag;
        this.description = description;
        this.isMarked = isMarked;
    }
    public TodoTask(String description, boolean isMarked){
        this(TaskTag.TODO, description, isMarked);
    }
    public TodoTask(String description) {
        this(description, false);
    }

    public boolean isMarked() {
        return this.isMarked;
    }

    public String getContent() {
        return this.description;
    }

    public TaskTag getTag() {
        return this.tag;
    }

    public boolean mark() {
        return this.isMarked != (this.isMarked = true);
    }

    public boolean unmark() {
        return this.isMarked != (this.isMarked = false);
    }

    public boolean isBefore(LocalDateTime end) {
        return false;
    }

    public boolean isAfter(LocalDateTime start) {
        return false;
    }

    @Override
    public String toString() {
        return this.getTag().toString() + (this.isMarked()? " [x] " : " [ ] ") + this.getContent();
    }
}