package tasks;

public final class TodoItem extends TaskItem {
    public TodoItem(String content, boolean isMarked) {
        super(content, ItemTag.Todo, isMarked);
    }
    public TodoItem(String content) {
        super(content, ItemTag.Todo);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
