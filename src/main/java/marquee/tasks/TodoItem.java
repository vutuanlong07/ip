package marquee.tasks;

public final class TodoItem extends TaskItem {
    public TodoItem(String content, boolean isMarked) {
        super(content, ItemTag.TODO, isMarked);
    }
    public TodoItem(String content) {
        super(content, ItemTag.TODO);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
