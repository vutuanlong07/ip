package org.cs2103t.marquee.core;

import java.util.List;
import java.util.stream.IntStream;

import org.cs2103t.marquee.core.command.Code;
import org.cs2103t.marquee.core.task.Task;

/**
 * Data class for all dialogues used by Marquee.
 */
public interface DialogueLibrary {
    /**
     * Get the chatbot banner.
     *
     * @return the chatbot banner
     */
    String banner();

    /**
     * Get greetings message.
     *
     * @return greetings message
     */
    String greetings();

    /**
     * Get session exit message.
     *
     * @return exit message
     */
    String successExit();

    /**
     * Get success message when loading save file.
     *
     * @return loading success message
     */
    String successLoad();

    /**
     * Get success message when saving save file.
     *
     * @return saving success message
     */
    String successSave();

    /**
     * Get message for displaying the current checklist.
     *
     * @param tasks list of tasks to display
     * @return list message
     */
    String successList(List<Task> tasks);

    /**
     * Get message for displaying search results.
     *
     * @param tasks list of tasks to display
     * @return search message
     */
    String successFind(List<Task> tasks);

    /**
     * Get message for displaying added tasks.
     *
     * @param tasks list of added tasks
     * @return task added message
     */
    String successAdd(List<Task> tasks);

    /**
     * Get message for displaying removed tasks.
     *
     * @param tasks list of removed tasks
     * @return task removed message
     */
    String successDelete(List<Task> tasks);

    /**
     * Get message for displaying marked tasks.
     *
     * @param tasks list of marked tasks
     * @return task marked message
     */
    String successMark(List<Task> tasks);

    /**
     * Get message for displaying unmarked tasks.
     *
     * @param tasks list of unmarked tasks
     * @return task unmarked message
     */
    String successUnmark(List<Task> tasks);

    /**
     * Get warning message when no save file has been created.
     *
     * @return no save file message
     */
    String warningSaveNotFound();

    /**
     * Get error message when save file is of an unrecognized format.
     *
     * @return corrupted save file message
     */
    String errorSaveCorrupted();

    /**
     * Get error message when save file cannot be accessed.
     *
     * @param cause error message returned by the {@link java.io.IOException}
     * @return inaccessible save file message
     */
    String errorSaveUnavailable(String cause);

    /**
     * Get error message when the input command is recognized but not supported
     * by this {@code Marquee} implementation.
     *
     * @param command the command input
     * @return unsupported command message
     */
    String errorUnknownCommand(String command);

    /**
     * Get error message when the input command is recognized but not supported
     * by this {@code Marquee} implementation.
     *
     * @param code the command code
     * @return unsupported command message
     */
    String errorUnsupportedCommand(Code code);

    /**
     * Get error message when an input flag is not applicable to the current command.
     *
     * @param flag     the invalid flag
     * @param codeName the command code name
     * @return unsupported command message
     */
    String errorUnknownFlag(String flag, String codeName);

    /**
     * Get error message when an input flag appeared twice.
     *
     * @param flag     the invalid flag
     * @return unsupported command message
     */
    String errorDuplicateFlag(String flag);

    /**
     * Get error message when input or output streams throws an error.
     *
     * @return IO error message
     */
    String errorIoUnavailable();

    /**
     * Return a string containing a numbered list of the given items.
     * <p>
     * The format used is {@code <spaces><numbering>. <text>}.
     *
     * @param list   list of items to convert
     * @param minIndent minimum number of spaces to use for indent, counting the numbering
     * @return a numbered list of the given items
     */
    default String numberedList(List<?> list, int minIndent) {
        int extraIndent = Math.max(minIndent - 2, Integer.toString(list.size()).length());
        StringBuilder builder = IntStream.range(0, list.size())
                .mapToObj(idx -> String.format("%" + extraIndent + "d. %s\n", idx + 1, list.get(idx)))
                .collect(
                        StringBuilder::new,
                        StringBuilder::append,
                        StringBuilder::append
                );
        return builder.toString();
    }

    /**
     * Return a string containing a bullet list of the given items.
     * <p>
     * The format used is {@code <spaces>- <text>}.
     *
     * @param list   list of items to convert
     * @param indent number of spaces to use for indent, counting the bullet
     * @return a numbered list of the given items
     */
    default String bulletList(List<?> list, int indent) {
        int extraIndents = indent - 2;
        StringBuilder builder = list.stream()
                .map(item -> new StringBuilder()
                        .repeat(' ', extraIndents)
                        .append("- ").append(item).append('\n')
                )
                .collect(
                        StringBuilder::new,
                        StringBuilder::append,
                        StringBuilder::append
                );
        return builder.toString();
    }
}
