public class EventItem extends TaskItem {
    private String start;
    private String end;

    public EventItem(String content, String start, String end) {
        super(content);
        this.start = start;
        this.end = end;
    }

    public String start() {
        return this.start;
    }

    public String end() {
        return this.end;
    }

    @Override
    public String toString() {
        return "[E]" + (this.marked()? "[x] " : "[ ] ") + this.content() + " (from: " + this.start() + " to: " + this.end() + ")";
    }
}
