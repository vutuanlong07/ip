package tasks;

public final class TodoItem extends TaskItem {
    public TodoItem(String content, boolean marked) {
        super(content, ItemTag.Todo, marked);
    }
    public TodoItem(String content) {
        super(content, ItemTag.Todo);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
