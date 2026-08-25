public abstract class TaskItem {
    public enum ItemTag {
        Todo('T'),
        Deadline('D'),
        Event('E');

        private char display;

        ItemTag(char display) {
            this.display = display;
        }

        @Override
        public String toString() {
            return "[" + display + "]";
        }
    }

    private boolean marked;
    private String content;
    private ItemTag tag;

    public TaskItem(String content, ItemTag tag) {
        this.marked = false;
        this.content = content;
        this.tag = tag;
    }

    public boolean marked() {
        return this.marked;
    }

    public String content() {
        return this.content;
    }

    public ItemTag tag() {
        return this.tag;
    }

    public boolean mark() {
        return this.marked != (this.marked = true);
    }

    public boolean unmark() {
        return this.marked != (this.marked = false);
    }

    @Override
    public String toString() {
        return (this.tag() == null? "[?]" : this.tag().toString()) + (this.marked()? " [x] " : " [ ] ") + this.content();
    }
}