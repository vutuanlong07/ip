package org.cs2103t.marquee.core.task;

import java.util.HashSet;
import java.util.Set;

import org.cs2103t.marquee.core.DuplicateKeyException;

/**
 * Base class for tags used by {@code Task}.
 */
public final class TaskTag {
    private static final Set<Character> SPECIAL_CHARACTERS = Set.of('-', '_', '.', ' ');

    private final String label;
    private boolean stale;
    private final Set<Task> taggedTasks;

    /**
     * Create a new {@code TaskTag}.
     * <p>
     * Tag names can only contain alphanumeric characters {@code a-z} {@code A-Z} {@code 0-9},
     * hyphens {@code -}, underscores {@code _}, periods {@code .} and spaces <code>&nbsp;</code>.
     *
     * @param label the label of the tag, which is what
     *              would be displayed when {@link #toString()} is invoked
     * @throws NullPointerException if the label is {@code null}
     * @throws IllegalArgumentException if an invalid character is found
     */
    public TaskTag(String label) throws NullPointerException, DuplicateKeyException, IllegalArgumentException {
        if (label == null) {
            throw new NullPointerException("Tag label cannot be null");
        }
        int invalidCharCodePoint = label.chars()
                .dropWhile(c -> Character.isLetterOrDigit(c) || SPECIAL_CHARACTERS.contains((char) c))
                .findAny().orElse(-1);
        if (invalidCharCodePoint != -1) {
            throw new IllegalArgumentException("Illegal character in tag name: '" + (char) invalidCharCodePoint + "'");
        }

        this.label = label;
        this.stale = false;
        this.taggedTasks = new HashSet<>();
    }

    /**
     * Remove the tag from tasks and mark it as stale.
     * <p>
     * Stale tags will throw an exception when trying to add it to tasks.
     */
    public void dispose() {
        for (Task task : taggedTasks) {
            onTagRemoved(task);
        }
        stale = true;
    }

    void onTagAdded(Task task) {
        if (stale) {
            throw new UnsupportedOperationException("Stale tag");
        }
        this.taggedTasks.add(task);
    }

    void onTagRemoved(Task task) {
        this.taggedTasks.remove(task);
    }

    public boolean isStale() {
        return stale;
    }

    /**
     * Returns the tag's label.
     *
     * @return the label of the tag
     * @throws UnsupportedOperationException if the tag is stale
     */
    @Override
    public String toString() {
        if (stale) {
            throw new UnsupportedOperationException("Stale tag");
        }
        return this.label;
    }
}
