package marquee;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

import marquee.base.command.Code;
import marquee.base.task.Task;

/**
 * Data class for all dialogues used by Marquee.
 */
public class Dialogues {
    /**
     * Get the chatbot banner.
     *
     * @return the chatbot banner
     */
    public String banner() {
        return """
                ____  ___
                |   \\/   |  _____   ____   _____   _   _   ____   ____
                | |\\  /| | / .__ /  | .-` / ._. \\ | | | | / ___) / ___)
                | | || | | | |_/ |  | |   | |_| | | |_| | | ___) | ___)
                |_| |/ |_| \\____/\\_ |_|   \\___  | \\_____\\ \\____) \\____)
                                              | |
                                              | |
                                               \\|
                """;
    }

    /**
     * Gets the message that tells the save directory.
     *
     * @param savePath the save directory
     * @return save directory info message
     */
    public String infoSaveFilePath(Path savePath) {
        return "Save file location: " + savePath.toString() + "\n";
    }

    /**
     * Get greetings message.
     *
     * @return greetings message
     */
    public String greetings() {
        return "Hi! I'm Marquee \\(>e<)/\nWhat will we do today? xD\n";
    }

    /**
     * Get session exit message.
     *
     * @return exit message
     */
    public String successExit() {
        return "See you later :3\n";
    }

    /**
     * Get success message when loading save file.
     *
     * @return loading success message
     */
    public String successLoad() {
        return "Checklist loaded successfully! :D\n";
    }

    /**
     * Get success message when saving save file.
     *
     * @return saving success message
     */
    public String successSave() {
        return "Checklist saved successfully :D\n";
    }

    /**
     * Get message for displaying the current checklist.
     *
     * @param tasks list of tasks to display
     * @return list message
     */
    public String successList(List<Task> tasks) {
        return "Current item(s) in your list:\n" + numberedList(tasks, 4) + "\n";
    }

    /**
     * Get message for displaying search results.
     *
     * @param tasks list of tasks to display
     * @return search message
     */
    public String successFind(List<Task> tasks) {
        return "Item(s) matching your search:\n" + numberedList(tasks, 4) + "\n";
    }

    /**
     * Get message for displaying added tasks.
     *
     * @param tasks list of added tasks
     * @param size  the new size of the checklist
     * @return task added message
     */
    public String successAdd(List<Task> tasks, int size) {
        return "Added items(s):\n" + numberedList(tasks, 4)
                + "\nto the list (^_-☆ >c\nCurrently have "
                + size + " item(s) in your checklist\n";
    }

    /**
     * Get message for displaying removed tasks.
     *
     * @param tasks list of removed tasks
     * @param size the new size of the checklist
     * @return task removed message
     */
    public String successDelete(List<Task> tasks, int size) {
        return "Deleted items(s):\n" + numberedList(tasks, 4)
                + "\nfrom the list (σ_σ.╒══⚟\nThere are "
                + size + " item(s) left in your checklist\n";
    }

    /**
     * Get message for displaying marked tasks.
     *
     * @param tasks list of marked tasks
     * @return task marked message
     */
    public String successMark(List<Task> tasks) {
        return "These item(s) were marked:\n" + numberedList(tasks, 4) + "\n";
    }

    /**
     * Get message for displaying unmarked tasks.
     *
     * @param tasks list of unmarked tasks
     * @return task unmarked message
     */
    public String successUnmark(List<Task> tasks) {
        return "These item(s) were unmarked:\n" + numberedList(tasks, 4) + "\n";
    }

    /**
     * Get warning message when current checklist is empty.
     *
     * @return empty list message
     */
    public String warningListEmpty() {
        return "Your checklist is empty (‾ 3‾)\n";
    }

    /**
     * Get warning message when search result is empty.
     *
     * @return empty search message
     */
    public String warningFindEmpty() {
        return "No items matched your search (‾ 3‾)\n";
    }

    /**
     * Get warning message when no tasks were marked.
     *
     * @return no mark target message
     */
    public String warningMarkEmpty() {
        return "No items were marked (‾ 3‾)\n";
    }

    /**
     * Get warning message when no tasks were unmarked.
     *
     * @return no unmark target message
     */
    public String warningUnmarkEmpty() {
        return "No items were unmarked (‾ 3‾)\n";
    }

    /**
     * Get warning message when no tasks were deleted.
     *
     * @return no delete target message
     */
    public String warningDeleteEmpty() {
        return "No items were deleted (‾ 3‾)\n";
    }

    /**
     * Get warning message when no save file has been created.
     *
     * @return no save file message
     */
    public String warningSaveFileNotFound() {
        return "No save file created yet (‾ 3‾)\n";
    }

    /**
     * Get error message when no task name was given.
     *
     * @return no task name message
     */
    public String errorTaskNameMissing() {
        return "Task name can't be empty, duh °∀°?\n";
    }

    /**
     * Get error message when no deadline was given.
     *
     * @return no deadline message
     */
    public String errorDeadlineMissing() {
        return "Task has no deadline? °∀°?";
    }

    /**
     * Get error message when no starting time was given.
     *
     * @return no starting time message
     */
    public String errorStartTimeMissing() {
        return "Event can't start without start time °∀°?\n";
    }

    /**
     * Get error message when no ending time was given.
     *
     * @return no ending time message
     */
    public String errorEndTimeMissing() {
        return "Event can't end without end time °∀°?\n";
    }

    /**
     * Get error message when ending time is before starting time.
     *
     * @return event ended before starting message
     */
    public String errorEventEndBeforeStart() {
        return "Event ending before it starts? °∀°?\n";
    }

    /**
     * Get error message when save file is of an unrecognized format.
     *
     * @return corrupted save file message
     */
    public String errorSaveCorrupted() {
        return "..ca.che..fi.le..cor.rup.te..d.  Σ( ﾟДﾟ)!\n";
    }

    /**
     * Get error message when save file cannot be accessed.
     *
     * @param cause error message returned by the {@link java.io.IOException}
     * @return inaccessible save file message
     */
    public String errorSaveUnavailable(String cause) {
        return "Somehow can't write save file?! Σ( ﾟДﾟ)!\nCause: " + cause + "\n";
    }

    /**
     * Get error message when an index input is out of range.
     *
     * @param string the input string
     * @return invalid index message
     */
    public String errorIndex(int string) {
        return string + " is not a valid index! (@ ~ @)\n";
    }

    /**
     * Get error message when an index input is not a number.
     *
     * @param string the input string
     * @return invalid index message
     */
    public String errorNan(String string) {
        return "'" + string + "' is not a number! (@ ~ @)\n";
    }

    /**
     * Get error message when a date-time input is not a valid date-time
     * according to {@code DateTimeFormatter}.
     *
     * @param string the input string
     * @return invalid date-time message
     * @see marquee.base.time.DateTimeFormatter
     */
    public String errorDatetime(String string) {
        return "'" + string + "' is not a valid date! (@ ~ @)\n";
    }

    /**
     * Get error message when the input command is recognized but not supported
     * by this {@code Marquee} implementation.
     *
     * @param command the command input
     * @return unsupported command message
     */
    public String errorUnknownCommand(String command) {
        return "No clue what '" + command + "' means `O ᗝ O´╬\n";
    }

    /**
     * Get error message when the input command is recognized but not supported
     * by this {@code Marquee} implementation.
     *
     * @param code the command code
     * @return unsupported command message
     */
    public String errorUnsupportedCommand(Code code) {
        return "Sorry, Marquee doesn't know how to execute " + code.getName() + " \uD83D\uDE4F(╯⌒╰.)\n";
    }

    /**
     * Get error message when an input flag is not applicable to the current command.
     *
     * @param flag     the invalid flag
     * @param codeName the command code name
     * @return unsupported command message
     */
    public String errorUnknownFlag(String flag, String codeName) {
        return "Flag /" + flag + " doesn't mean anything in " + codeName + " `O ᗝ O´╬\n";
    }

    /**
     * Get error message when the current command doesn't take an argument.
     *
     * @param codeName the command code name
     * @return unsupported command message
     */
    public String errorUnusedArgument(String codeName) {
        return "Command" + codeName + " doesn't take any argument `O ᗝ O´╬\n";
    }

    /**
     * Get error message when an input flag appeared twice.
     *
     * @param flag     the invalid flag
     * @return unsupported command message
     */
    public String errorDuplicateFlag(String flag) {
        return "Too many /" + flag + " `O ᗝ O´╬\n";
    }

    /**
     * Get error message when input or output streams throws an error.
     *
     * @return IO error message
     */
    public String fatalErrorIoUnavailable() {
        return "I can't see anything #.#\n";
    }

    /**
     * Return a string containing a numbered list of the given items.
     * <p>
     * The format used is {@code <spaces><numbering>. <text>}.
     *
     * @param list   list of items to convert
     * @param minIndent minimum number of spaces to use for indent, counting the numbering
     * @return a numbered list of the given items
     */
    public static String numberedList(List<?> list, int minIndent) {
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
    private static String bulletList(List<?> list, int indent) {
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
