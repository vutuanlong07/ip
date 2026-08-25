public final class EventItem extends TaskItem {
    private String start;
    private String end;

    public EventItem(String content, String start, String end, boolean marked) {
        super(content, ItemTag.Event, marked);
        this.start = start;
        this.end = end;
    }
    public EventItem(String content, String start, String end) {
        super(content, ItemTag.Event);
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
        return super.toString() + " (from: " + this.start() + " to: " + this.end() + ")";
    }
}
