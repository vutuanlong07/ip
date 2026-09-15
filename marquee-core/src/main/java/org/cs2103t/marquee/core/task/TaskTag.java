package org.cs2103t.marquee.core.task;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.cs2103t.marquee.core.DuplicateKeyException;

/**
 * Base class for tags used by {@code Task}.
 * <p>
 * Do not create new instances repeatedly. All instances are tracked and may cause collisions.
 */
public final class TaskTag {
    private static final Map<String, TaskTag> TAG_BY_LABEL = new HashMap<>();
    private static final Set<Character> SPECIAL_CHARACTERS = Set.of('-', '_', '.', ' ');

    private final String label;
    private boolean stale;

    /**
     * Create a new {@code TaskTag}.
     * <p>
     * Tag names can only contain alphanumeric characters {@code a-z} {@code A-Z} {@code 0-9},
     * hyphens {@code -}, underscores {@code _}, periods {@code .} and spaces <code>&nbsp;</code>.
     * <p>
     * All tag instances must have unique labels.
     *
     * @param label the label of the tag, which is what
     *              would be displayed when {@link #toString()} is invoked
     * @throws NullPointerException if the label is {@code null}
     * @throws DuplicateKeyException if a tag with this label already exist
     * @throws IllegalArgumentException if an invalid character is found
     */
    public TaskTag(String label) throws NullPointerException, DuplicateKeyException, IllegalArgumentException {
        if (label == null) {
            throw new NullPointerException("Tag label cannot be null");
        }
        if (TAG_BY_LABEL.containsKey(label)) {
            throw new DuplicateKeyException("Tag already exists", label);
        }
        int invalidCharCodePoint = label.chars()
                .dropWhile(c -> Character.isLetterOrDigit(c) || SPECIAL_CHARACTERS.contains((char) c))
                .findAny().orElse(-1);
        if (invalidCharCodePoint != -1) {
            throw new IllegalArgumentException("Illegal character in tag name: '" + (char) invalidCharCodePoint + "'");
        }

        this.label = label;
        this.stale = false;
        TAG_BY_LABEL.put(label, this);
    }

    /**
     * Remove the tag from the available list and mark it as stale.
     * <p>
     * Stale tags will throw and exception on every method call.
     *
     * @param tag
     */
    public static void removeTag(TaskTag tag) {
        tag.stale = true;
        TAG_BY_LABEL.remove(tag.label);
    }

    /**
     * Creates a new {@code TaskTag} with the given label if it doesn't exist yet,
     * then return the {@code TaskTag} associated with this label.
     *
     * @param label the label of the tag to get
     * @return the tag associated with {@code label}
     */
    public static TaskTag createOrGet(String label) {
        TaskTag current = getTaskTag(label);
        return current != null
                ? current
                : new TaskTag(label);
    }

    /**
     * Gets the {@code TaskTag} with the given label.
     *
     * @param label the label displayed by the {@code TaskTag} when invoking {@link #toString()}
     * @return the {@code TaskTag} with the given label, or {@code null} if there are none
     */
    public static TaskTag getTaskTag(String label) {
        return TAG_BY_LABEL.get(label);
    }

    /**
     * Returns an unmodifiable view of available task tags<p>
     *
     * @return an unmodifiable {@link Collection} of available task tags
     */
    public static Collection<TaskTag> getAvailableTags() {
        return TAG_BY_LABEL.values();
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

    /**
     * {@inheritDoc Object}
     * @throws UnsupportedOperationException if the tag is stale
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof TaskTag && this.toString().equals(((TaskTag) obj).toString());
    }

    /**
     * {@inheritDoc Object}
     * @throws UnsupportedOperationException if the tag is stale
     */
    @Override
    public int hashCode() {
        if (stale) {
            throw new UnsupportedOperationException("Stale tag");
        }
        return Objects.hash(label);
    }
}
