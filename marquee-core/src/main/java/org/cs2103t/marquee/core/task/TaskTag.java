package org.cs2103t.marquee.core.task;

import java.text.ParseException;
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
        TAG_BY_LABEL.put(label, this);
    }

    /**
     * Gets the {@code TaskTag} with the given label.
     *
     * @param label the label displayed by the {@code TaskTag} when invoking {@link #getLabel()}
     * @return the {@code TaskTag} with the given label, or {@code null} if there are none
     */
    public static TaskTag fromLabel(String label) {
        return TAG_BY_LABEL.get(label);
    }

    /**
     * Parses the string as a {@code TaskTag}.
     *
     * @param input the input string, following the format of {@link #toString()}
     * @return the parsed {@code TaskTag}, or null if the tag with this label hasn't been defined
     * @throws ParseException if the input doesn't follow the tag format
     */
    public static TaskTag parseTag(String input) throws ParseException {
        if (!input.startsWith("[")) {
            throw new ParseException("Not a valid tag string", 0);
        } else if (!input.endsWith("]")) {
            throw new ParseException("Not a valid tag string", input.length() - 1);
        } else {
            String label = input.substring(1, input.length() - 1);
            int invalidCharCodePoint = label.chars()
                    .dropWhile(c -> Character.isLetterOrDigit(c) || SPECIAL_CHARACTERS.contains((char) c))
                    .findAny().orElse(-1);
            if (invalidCharCodePoint != -1) {
                throw new ParseException("Illegal character in tag name", label.indexOf(invalidCharCodePoint));
            }

            return TAG_BY_LABEL.get(label);
        }
    }

    /**
     * Returns an unmodifiable view of available task tags<p>
     *
     * @return an unmodifiable {@link Collection} of available task tags
     */
    public static Collection<TaskTag> getAvailableTags() {
        return TAG_BY_LABEL.values();
    }

    public String getLabel() {
        return label;
    }

    /**
     * Returns the decorated version of the tag for printing
     * <p>
     * The result is in the form of {@code [<label>]}.
     *
     * @return a decorated version of the tag
     */
    @Override
    public String toString() {
        return "[" + this.getLabel() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TaskTag && this.getLabel().equals(((TaskTag) obj).getLabel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(label);
    }
}
