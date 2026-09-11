package org.cs2103t.marquee.cli;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

import org.cs2103t.marquee.cli.command.Code;
import org.cs2103t.marquee.core.task.Task;

/**
 * Data class for all dialogues used by Marquee.
 */
public class Dialogues {
    /**
     * Prints the chatbot banner
     */
    public void banner() {
        System.out.print("""
                ____  ___
                |   \\/   |  _____   ____   _____   _   _   ____   ____
                | |\\  /| | / .__ /  | .-` / ._. \\ | | | | / ___) / ___)
                | | || | | | |_/ |  | |   | |_| | | |_| | | ___) | ___)
                |_| |/ |_| \\____/\\_ |_|   \\___  | \\_____\\ \\____) \\____)
                                              | |
                                              | |
                                               \\|
                """);
    }

    /**
     * Prints the user input prompt.
     */
    public void prompt() {
        System.out.print("\n> ");
    }

    /**
     * Prints the message that tells the save directory.
     *
     * @param savePath the save directory
     */
    public void infoSaveFilePath(Path savePath) {
        System.out.print("Save file location: " + savePath.toString() + "\n");
    }

    /**
     * Prints greetings message.
     */
    public void greetings() {
        System.out.print("Hi! I'm Marquee \\(>e<)/\nWhat will we do today? xD\n");
    }

    /**
     * Prints session exit message.
     */
    public void successExit() {
        System.out.print("See you later :3\n");
    }

    /**
     * Prints success message when loading save file.
     */
    public void successLoad() {
        System.out.print("Checklist loaded successfully! :D\n");
    }

    /**
     * Prints success message when saving save file.
     */
    public void successSave() {
        System.out.print("Checklist saved successfully :D\n");
    }

    /**
     * Prints message for displaying the current checklist.
     *
     * @param tasks list of tasks to display
     */
    public void successList(List<Task> tasks) {
        System.out.print(tasks.isEmpty()
                ? "Current item(s) in your list:\n" + numberedList(tasks, 4) + "\n"
                : "Your checklist is empty (‾ 3‾)\n"
        );
    }

    /**
     * Prints message for displaying search results.
     *
     * @param tasks list of tasks to display
     */
    public void successFind(List<Task> tasks) {
        System.out.print(tasks.isEmpty()
                ? "Item(s) matching your search:\n" + numberedList(tasks, 4) + "\n"
                : "No items matched your search (‾ 3‾)\n"
        );
    }

    /**
     * Prints message for displaying added tasks.
     *
     * @param tasks list of added tasks
     */
    public void successAdd(List<Task> tasks) {
        System.out.print(tasks.isEmpty()
                ? "Added items(s):\n" + numberedList(tasks, 4) + "\nto the list (^_-☆ >c\n"
                : "No items were added (‾ 3‾)\n"
        );
    }

    /**
     * Prints message for displaying removed tasks.
     *
     * @param tasks list of removed tasks
     */
    public void successDelete(List<Task> tasks) {
        System.out.print(tasks.isEmpty()
                ? "Deleted items(s):\n" + numberedList(tasks, 4) + "\nfrom the list (σ_σ.╒══⚟\n"
                : "No items were deleted (‾ 3‾)\n"
        );
    }

    /**
     * Prints message for displaying marked tasks.
     *
     * @param tasks list of marked tasks
     */
    public void successMark(List<Task> tasks) {
        System.out.print(tasks.isEmpty()
                ? "These item(s) were marked:\n" + numberedList(tasks, 4) + "\n"
                : "No items were marked (‾ 3‾)\n"
        );
    }

    /**
     * Prints message for displaying unmarked tasks.
     *
     * @param tasks list of unmarked tasks
     */
    public void successUnmark(List<Task> tasks) {
        System.out.print(tasks.isEmpty()
                ? "These item(s) were unmarked:\n" + numberedList(tasks, 4) + "\n"
                : "No items were unmarked (‾ 3‾)\n"
        );
    }

    /**
     * Prints warning message when no save file has been created.
     */
    public void warningSaveNotFound() {
        System.out.print("No save file created yet (‾ 3‾)\n");
    }

    /**
     * Prints error message when no task name was given.
     */
    public void errorTaskNameMissing() {
        System.out.print("Task name can't be empty, duh °∀°?\n");
    }

    /**
     * Prints error message when no deadline was given.
     */
    public void errorDeadlineMissing() {
        System.out.print("Task has no deadline? °∀°?");
    }

    /**
     * Prints error message when no starting time was given.
     */
    public void errorStartTimeMissing() {
        System.out.print("Event can't start without start time °∀°?\n");
    }

    /**
     * Prints error message when no ending time was given.
     */
    public void errorEndTimeMissing() {
        System.out.print("Event can't end without end time °∀°?\n");
    }

    /**
     * Prints error message when ending time is before starting time.
     */
    public void errorEventEndBeforeStart() {
        System.out.print("Event ending before it starts? °∀°?\n");
    }

    /**
     * Prints error message when save file is of an unrecognized format.
     */
    public void errorSaveCorrupted() {
        System.out.print("..ca.che..fi.le..cor.rup.te..d.  Σ( ﾟДﾟ)!\n");
    }

    /**
     * Prints error message when save file cannot be accessed.
     *
     * @param cause error message returned by the {@link java.io.IOException}
     */
    public void errorSaveUnavailable(String cause) {
        System.out.print("Somehow can't write save file?! Σ( ﾟДﾟ)!\nCause: " + cause + "\n");
    }

    /**
     * Prints error message when an index input is out of range.
     *
     * @param string the input string
     */
    public void errorIndex(String string) {
        System.out.print(string + " is not a valid index! (@ ~ @)\n");
    }

    /**
     * Prints error message when an index input is not a number.
     *
     * @param string the input string
     */
    public void errorNan(String string) {
        System.out.print("'" + string + "' is not a number! (@ ~ @)\n");
    }

    /**
     * Prints error message when a date-time input is not a valid date-time.
     *
     * @param string the input string
     */
    public void errorDatetime(String string) {
        System.out.print("'" + string + "' is not a valid date! (@ ~ @)\n");
    }

    /**
     * Prints error message when the input command is not recognized.
     *
     * @param command the command input
     */
    public void errorUnknownCommand(String command) {
        System.out.print("No clue what '" + command + "' means `O ᗝ O´╬\n");
    }

    /**
     * Prints error message when the input command is recognized but not supported
     * by this {@code Marquee} implementation.
     *
     * @param code the command code
     */
    public void errorUnsupportedCommand(Code code) {
        System.out.print("Sorry, Marquee doesn't know how to execute " + code.getName() + " \uD83D\uDE4F(╯⌒╰.)\n");
    }

    /**
     * Prints error message when an input flag is not applicable to the current command.
     *
     * @param flag the invalid flag
     */
    public void errorUnknownFlag(String flag) {
        System.out.print(flag.isEmpty()
                ? "This command doesn't take any argument `O ᗝ O´╬\n"
                : "Flag /" + flag + " cannot be used here `O ᗝ O´╬\n"
        );
    }

    /**
     * Prints error message when an input flag appeared more than once.
     *
     * @param flag the invalid flag
     */
    public void errorDuplicateFlag(String flag) {
        System.out.print("Too many /" + flag + " `O ᗝ O´╬\n");
    }

    /**
     * Prints error message when input or output streams throws an error.
     */
    public void errorIoUnavailable() {
        System.out.print("I can't see anything #.#\n");
    }

    /**
     * Return a string containing a numbered list of the given items.
     * <p>
     * The format used is {@code <spaces><numbering>. <text>}.
     *
     * @param list list of items to convert
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
     * @param list list of items to convert
     * @param indent number of spaces to use for indent, counting the bullet
     * @return a numbered list of the given items
     */
    public static String bulletList(List<?> list, int indent) {
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
