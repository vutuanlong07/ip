public class ToDoItem extends TaskItem {
    public ToDoItem(String content) {
        super(content);
    }

    @Override
    public String toString() {
        return "[T]" + (this.marked()? "[x] " : "[ ] ") + this.content();
    }
}
