package marquee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import marquee.base.command.Command;
import marquee.base.command.DuplicateFlagException;
import marquee.base.command.UnknownFlagException;
import marquee.base.io.CsvTable;
import marquee.base.task.Task;
import marquee.base.time.DateTimeFormatter;
import marquee.command.BaseCodes;
import marquee.task.DeadlineTask;
import marquee.task.EventTask;
import marquee.task.TodoTask;

/**
 * Main class for the standalone chatbot Marquee.
 */
public class Marquee {
    /**
     * Data class for all dialogues used by Marquee.
     */
    public static class Dialogues {
        public String BANNER() {
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
        public String GREETINGS() {
            return "Hi! I'm Marquee \\(>e<)/\nWhat will we do today? xD\n";
        }

        public String SUCCESS_EXIT() {
            return "See you later :3\n";
        }
        public String SUCCESS_LOAD() {
            return "Checklist loaded successfully! :D\n";
        }
        public String SUCCESS_SAVE() {
            return "Checklist saved successfully :D\n";
        }
        public String SUCCESS_LIST(String list) {
            return "Current item(s) in your list:\n" + list + "\n";
        }
        public String SUCCESS_FIND(String list) {
            return "Item(s) matching your search:\n%s\n";
        }
        public String SUCCESS_ADD(String list, int size) {
            return "Added items(s):\n%s\nto the list (^_-☆ >c\nCurrently have " + size + " item(s) in your checklist\n";
        }
        public String SUCCESS_DELETE(String list, int size) {
            return "Deleted items(s):\n%s\nfrom the list (σ_σ.╒══⚟\nThere are " + size + " item(s) left in your checklist\n";
        }
        public String SUCCESS_MARK(String list) {
            return "These item(s) were marked:\n%s\n";
        }
        public String SUCCESS_UNMARK(String list) {
            return "These item(s) were unmarked:\n%s\n";
        }

        public String WARNING_LIST_EMPTY() {
            return "Your checklist is empty (‾ 3‾)\n";
        }
        public String WARNING_FIND_EMPTY() {
            return "No items matched your search (‾ 3‾)\n";
        }
        public String WARNING_MARK_EMPTY() {
            return "No items were marked (‾ 3‾)\n";
        }
        public String WARNING_UNMARK_EMPTY() {
            return "No items were unmarked (‾ 3‾)\n";
        }
        public String WARNING_DELETE_EMPTY() {
            return "No items were deleted (‾ 3‾)\n";
        }
        public String WARNING_SAVE_FILE_NOT_FOUND() {
            return "No save file created yet (‾ 3‾)\n";
        }

        public String ERROR_TASK_MISSING_NAME() {
            return "Task name can't be empty, duh °∀°?\n";
        }
        public String ERROR_DEADLINE_MISSING_DEADLINE() {
            return "Task has no deadline? °∀°?";
        }
        public String ERROR_EVENT_MISSING_START_TIME() {
            return "Event can't start without start time °∀°?\n";
        }
        public String ERROR_EVENT_MISSING_END_TIME() {
            return "Event can't end without end time °∀°?\n";
        }
        public String ERROR_EVENT_END_BEFORE_START() {
            return "Event ending before it starts? °∀°?\n";
        }

        public String ERROR_SAVE_CORRUPTED() {
            return "..ca.che..fi.le..cor.rup.te..d.  Σ( ﾟДﾟ)!\n";
        }
        public String ERROR_SAVE_UNAVAILABLE(String cause) {
            return "Somehow can't write save file?! Σ( ﾟДﾟ)!\nCause: " + cause + "\n";
        }
        public String ERROR_INDEX(int index) {
            return index + " is not a valid index! >_<\n";
        }
        public String ERROR_NAN(String numStr) {
            return "'" + numStr + "' is not a number! (@ ~ @)\n";
        }
        public String ERROR_DATETIME() {
            return "Not a valid date! (@ ~ @)\n";
        }

        public String ERROR_UNSUPPORTED_COMMAND() {
            return "Sorry, Marquee doesn't know how to execute this command \uD83D\uDE4F(╯⌒╰.)\n";
        }
        public String ERROR_UNKNOWN_COMMAND(String command) {
            return "No clue what '" + command + "' means `O ᗝ O´╬\n";
        }
        public String ERROR_UNKNOWN_FLAG(String flag, String code) {
            return "Flag /" + flag + " doesn't mean anything in " + code + " `O ᗝ O´╬\n";
        }
        public String ERROR_UNUSED_ARGUMENT(String code) {
            return "Command" + code + " doesn't take any argument `O ᗝ O´╬\n";
        }
        public String ERROR_DUPLICATE_FLAG(String flag) {
            return "Too many /" + flag + " `O ᗝ O´╬\n";
        }

        public String FATAL_ERROR_IO_UNAVAILABLE() {
            return "I can't see anything #.#\n";
        }
    }

    private final BufferedReader inputReader;
    private final PrintStream outputStream;
    private final Path savePath;
    private final Dialogues dialogues;

    private List<Task> checklist;
    private boolean isRunning;

    /**
     * Instantiates an instance of Marquee and attempts to load its checklist from {@code savePath}.
     * If loading fails, starts with an empty checklist.
     *
     * @param inputStream  the input stream Marquee will read commands from
     * @param outputStream the output stream Marquee will direct outputs from its methods to
     * @param savePath     the path to the CSV file Marquee will save its checklist to
     */
    public Marquee(InputStream inputStream, OutputStream outputStream, Path savePath) {
        this.dialogues = new Dialogues();
        this.inputReader = new BufferedReader(new InputStreamReader(inputStream));
        this.outputStream = new PrintStream(outputStream, true, StandardCharsets.UTF_8);
        this.savePath = savePath;
        this.checklist = new ArrayList<>();
        loadChecklist();
    }

    /**
     * Attempts to load the checklist from the save file.
     * If the operation fails, no change is made to the checklist.
     *
     * @return Whether the file was read successfully
     */
    public final boolean loadChecklist() {
        try {
            CsvTable.readFile(savePath, ";");
            outputStream.print(this.dialogues.SUCCESS_LOAD());
            return true;
        } catch (NoSuchFileException _) {
            outputStream.print(this.dialogues.WARNING_SAVE_FILE_NOT_FOUND());
        } catch (IOException | ParseException | IllegalArgumentException _) {
            outputStream.print(this.dialogues.ERROR_SAVE_CORRUPTED());
        }
        return false;
    }

    /**
     * Saves the checklist into the save file.
     * If the operation fails, the original save file will not be changed.
     *
     * @return Whether the file was written successfully
     */
    public final boolean saveChecklist() {
        try {
            CsvTable csv = new CsvTable(List.of("task"), ";");
            CsvTable.writeFile(savePath, csv);
            outputStream.print(this.dialogues.SUCCESS_SAVE());
            return true;
        } catch (IOException e) {
            outputStream.print(this.dialogues.ERROR_SAVE_UNAVAILABLE(e.getMessage()));
            return false;
        }
    }

    /**
     * Tells the chatbot to save checklist and ends the session.
     */
    public final void exit() {
        if (saveChecklist()) {
            outputStream.print(this.dialogues.SUCCESS_EXIT());
            isRunning = false;
        }
    }

    /**
     * Lists the items in the checklist to the output stream.
     * If there are none, output a different message clarifying that the checklist is empty.
     */
    public final void list() {
        if (checklist.isEmpty()) {
            outputStream.print(this.dialogues.WARNING_LIST_EMPTY());
        } else {
            outputStream.print(this.dialogues.SUCCESS_LIST(numberedList(checklist)));
        }
    }

    /**
     * Searches for items in the checklist satisfying the search conditions, then list those tasks.
     * If description is empty or {@code null}, and all other parameters are {@code null}, no result is returned.
     *
     * @param description match items containing this substring in its description
     * @param start       match items starting after this time
     * @param end         match items ending before this time
     * @param isMarked    match items with this mark status
     */
    public final void find(String description, LocalDateTime start, LocalDateTime end, Boolean isMarked) {
        List<Task> matchingItems = filterTasks(description, start, end, isMarked);
        if (matchingItems.isEmpty()) {
            outputStream.print(this.dialogues.WARNING_FIND_EMPTY());
        } else {
            outputStream.print(this.dialogues.SUCCESS_FIND(numberedList(matchingItems)));
        }
    }

    /**
     * Adds tasks to the checklist, then list the added tasks.
     *
     * @param tasks the tasks to be added to the checklist
     */
    public final void addTasks(Task... tasks) {
        List<Task> newTasks = List.of(tasks);
        checklist.addAll(newTasks);
        outputStream.print(this.dialogues.SUCCESS_ADD(bulletList(newTasks), checklist.size()));
    }

    /**
     * Deletes tasks from the checklist by index, then list the deleted tasks.
     *
     * @param indices the indices of the tasks to be deleted
     */
    public final void deleteTasks(int... indices) {
        if (checklist.isEmpty()) {
            outputStream.print(this.dialogues.WARNING_LIST_EMPTY());
        }
        for (int i : indices) {
            if (i < 1 || i > checklist.size()) {
                outputStream.print(this.dialogues.ERROR_INDEX(i));
                return;
            }
        }
        List<Task> removedItems = IntStream.of(indices)
                .mapToObj(i -> checklist.remove(i - 1))
                .filter(Objects::nonNull)
                .toList();
        if (removedItems.isEmpty()) {
            outputStream.print(this.dialogues.WARNING_DELETE_EMPTY());
        } else {
            outputStream.print(this.dialogues.SUCCESS_DELETE(bulletList(removedItems), checklist.size()));
        }
    }

    /**
     * Marks tasks from the checklist as completed by index, then list the marked tasks.
     *
     * @param indices the indices of the tasks to be marked
     */
    public final void markTasks(int... indices) {
        if (checklist.isEmpty()) {
            outputStream.print(this.dialogues.WARNING_LIST_EMPTY());
        }
        for (int i : indices) {
            if (i < 1 || i > checklist.size()) {
                outputStream.print(this.dialogues.ERROR_INDEX(i));
                return;
            }
        }
        List<Task> markedItems = IntStream.of(indices)
                .mapToObj(i -> checklist.get(i - 1))
                .filter(Task::mark)
                .toList();
        if (markedItems.isEmpty()) {
            outputStream.print(this.dialogues.WARNING_MARK_EMPTY());
        } else {
            outputStream.print(this.dialogues.SUCCESS_MARK(bulletList(markedItems)));
        }
    }

    /**
     * Unmarks (mark as incomplete) tasks from the checklist by index, then list the unmarked tasks.
     *
     * @param indices the indices of the tasks to be unmarked
     */
    public final void unmarkTasks(int... indices) {
        if (checklist.isEmpty()) {
            outputStream.print(this.dialogues.WARNING_LIST_EMPTY());
        }
        for (int i : indices) {
            if (i < 1 || i > checklist.size()) {
                outputStream.print(this.dialogues.ERROR_INDEX(i));
                return;
            }
        }
        List<Task> unmarkedItems = IntStream.of(indices)
                .mapToObj(i -> checklist.get(i - 1))
                .filter(Task::unmark)
                .toList();
        if (unmarkedItems.isEmpty()) {
            outputStream.print(this.dialogues.WARNING_UNMARK_EMPTY());
        } else {
            outputStream.print(this.dialogues.SUCCESS_UNMARK(bulletList(unmarkedItems)));
        }
    }

    /**
     * Processes the command
     */

    /**
     * Starts the chatbot loop. Marquee will listen from the input stream
     * and print to the output stream given in the constructor.
     */
    public final void run() {
        isRunning = true;
        outputStream.print(this.dialogues.BANNER());
        outputStream.print(this.dialogues.GREETINGS());
        while (isRunning) {
            String input;
            Command command;

            try {
                input = getInput();
            } catch (IOException e) {
                outputStream.print(this.dialogues.FATAL_ERROR_IO_UNAVAILABLE());
                exit();
                continue;
            }

            try {
                command = Command.parseCommand(input);
            } catch (UnknownFlagException e) {
                if (e.getFlagName() == null) {
                    outputStream.print(this.dialogues.ERROR_UNUSED_ARGUMENT(e.getCode().name()));
                } else {
                    outputStream.print(this.dialogues.ERROR_UNKNOWN_FLAG(e.getFlagName(), e.getCode().name()));
                }
                continue;
            } catch (DuplicateFlagException e) {
                outputStream.print(this.dialogues.ERROR_DUPLICATE_FLAG(e.getFlagName()));
                continue;
            } catch (IllegalArgumentException e) {
                outputStream.print(this.dialogues.ERROR_UNKNOWN_COMMAND(input));
                continue;
            }

            try {
                if (command.getCode().equals(BaseCodes.EXIT)) {
                    exit();
                } else if (command.getCode().equals(BaseCodes.LOAD)) {
                    loadChecklist();
                } else if (command.getCode().equals(BaseCodes.SAVE)) {
                    saveChecklist();
                } else if (command.getCode().equals(BaseCodes.LIST)) {
                    list();
                } else if (command.getCode().equals(BaseCodes.FIND)) {
                    find(
                            command.getArgument(),
                            command.hasFlag("from")
                                    ? DateTimeFormatter.parseDateTime(command.getFlag("from"))
                                    : null,
                            command.hasFlag("to")
                                    ? DateTimeFormatter.parseDateTime(command.getFlag("to"))
                                    : null,
                            command.hasFlag("completed") != command.hasFlag("incomplete")
                                    ? command.hasFlag("completed")
                                    : null
                    );
                } else if (command.getCode().equals(BaseCodes.TODO)) {
                    if (!command.hasArgument()) {
                        outputStream.print(this.dialogues.ERROR_TASK_MISSING_NAME());
                    } else {
                        addTasks(new TodoTask(command.getArgument()));
                    }
                } else if (command.getCode().equals(BaseCodes.DEADLINE)) {
                    if (!command.hasArgument()) {
                        outputStream.print(this.dialogues.ERROR_TASK_MISSING_NAME());
                    } else if (!command.hasFlag("by")) {
                        outputStream.print(this.dialogues.ERROR_DEADLINE_MISSING_DEADLINE());
                    } else {
                        addTasks(new DeadlineTask(
                                command.getArgument(),
                                DateTimeFormatter.parseDateTime(command.getFlag("by"))
                        ));
                    }
                } else if (command.getCode().equals(BaseCodes.EVENT)) {
                    if (!command.hasArgument()) {
                        outputStream.print(this.dialogues.ERROR_TASK_MISSING_NAME());
                    } else if (!command.hasFlag("from")) {
                        outputStream.print(this.dialogues.ERROR_EVENT_MISSING_START_TIME());
                    } else if (!command.hasFlag("to")) {
                        outputStream.print(this.dialogues.ERROR_EVENT_MISSING_END_TIME());
                    } else {
                        try {
                            addTasks(new EventTask(
                                    command.getArgument(),
                                    DateTimeFormatter.parseDateTime(command.getFlag("from")),
                                    DateTimeFormatter.parseDateTime(command.getFlag("to"))
                            ));
                        } catch (IllegalArgumentException _) {
                            outputStream.print(this.dialogues.ERROR_EVENT_END_BEFORE_START());
                        }
                    }
                } else if (command.getCode().equals(BaseCodes.DELETE)) {
                    deleteTasks(parseIntArray(command.getArgument()));
                } else if (command.getCode().equals(BaseCodes.DELETE_ALL)) {
                    deleteTasks(IntStream.rangeClosed(1, checklist.size()).toArray());
                } else if (command.getCode().equals(BaseCodes.DELETE_MATCHING)) {
                    deleteTasks(
                            filterTasks(
                                    command.getArgument(),
                                    command.hasFlag("from")
                                            ? DateTimeFormatter.parseDateTime(command.getFlag("from"))
                                            : null,
                                    command.hasFlag("to")
                                            ? DateTimeFormatter.parseDateTime(command.getFlag("to"))
                                            : null,
                                    command.hasFlag("completed") != command.hasFlag("incomplete")
                                            ? command.hasFlag("completed")
                                            : null
                            ).stream()
                                    .mapToInt(item -> checklist.indexOf(item) + 1)
                                    .toArray()
                    );
                } else if (command.getCode().equals(BaseCodes.MARK)) {
                    markTasks(parseIntArray(command.getArgument()));
                } else if (command.getCode().equals(BaseCodes.MARK_ALL)) {
                    markTasks(IntStream.rangeClosed(1, checklist.size()).toArray());
                } else if (command.getCode().equals(BaseCodes.MARK_MATCHING)) {
                    markTasks(
                            filterTasks(
                                    command.getArgument(),
                                    command.hasFlag("from")
                                            ? DateTimeFormatter.parseDateTime(command.getFlag("from"))
                                            : null,
                                    command.hasFlag("to")
                                            ? DateTimeFormatter.parseDateTime(command.getFlag("to"))
                                            : null,
                                    command.hasFlag("completed") != command.hasFlag("incomplete")
                                            ? command.hasFlag("completed")
                                            : null
                            ).stream()
                                    .mapToInt(item -> checklist.indexOf(item) + 1)
                                    .toArray()
                    );
                } else if (command.getCode().equals(BaseCodes.UNMARK)) {
                    unmarkTasks(parseIntArray(command.getArgument()));
                } else if (command.getCode().equals(BaseCodes.UNMARK_ALL)) {
                    unmarkTasks(IntStream.rangeClosed(1, checklist.size()).toArray());
                } else if (command.getCode().equals(BaseCodes.UNMARK_MATCHING)) {
                    unmarkTasks(
                            filterTasks(
                                    command.getArgument(),
                                    command.hasFlag("from")
                                            ? DateTimeFormatter.parseDateTime(command.getFlag("from"))
                                            : null,
                                    command.hasFlag("to")
                                            ? DateTimeFormatter.parseDateTime(command.getFlag("to"))
                                            : null,
                                    command.hasFlag("completed") != command.hasFlag("incomplete")
                                            ? command.hasFlag("completed")
                                            : null
                            ).stream()
                                    .mapToInt(item -> checklist.indexOf(item) + 1)
                                    .toArray()
                    );
                } else {
                    outputStream.print(this.dialogues.ERROR_UNSUPPORTED_COMMAND());
                }
            } catch (DateTimeParseException e) {
                outputStream.print(this.dialogues.ERROR_DATETIME());
            } catch (NumberFormatException e) {
                outputStream.print(this.dialogues.ERROR_NAN(e.getMessage()));
            }
        }
    }

    public static void main(String[] args) {
        Marquee chatbot = new Marquee(System.in, System.out, Path.of("./checklist.csv"));
        chatbot.run();
    }

    private String getInput() throws IOException {
        outputStream.print("\n> ");
        return inputReader.readLine();
    }

    private static int[] parseIntArray(String input) throws NumberFormatException {
        return Arrays.stream(input.split("\\s+", -1))
                .mapToInt(str -> {
                    try {
                        return Integer.parseInt(str);
                    } catch (NumberFormatException _) {
                        throw new NumberFormatException(str);
                    }
                })
                .toArray();
    }

    private List<Task> filterTasks(String search, LocalDateTime start, LocalDateTime end, Boolean isMarked) {
        return (search == null || search.isEmpty()) && start == null && end == null && isMarked == null
                ? List.of()
                : checklist.stream()
                .filter(isMarked == null
                        ? _ -> true
                        : item -> item.isMarked() == isMarked
                )
                .filter(start == null
                        ? _ -> true
                        : item -> !item.getStart().isBefore(start)
                )
                .filter(end == null
                        ? _ -> true
                        : item -> !item.getEnd().isAfter(end)
                )
                .filter(search == null || search.isEmpty()
                        ? _ -> true
                        : item -> item.getDescription().contains(search))
                .toList();
        }

    private static String numberedList(List<Task> list) {
        StringBuilder builder = IntStream.range(0, list.size())
                .mapToObj(idx -> String.format("%d. %s\n", idx + 1, list.get(idx)))
                .collect(
                        StringBuilder::new,
                        StringBuilder::append,
                        StringBuilder::append
                );
        return builder.toString();
    }

    private static String bulletList(List<Task> list) {
        StringBuilder builder = list.stream()
                .map(item -> String.format("  %s\n", item))
                .collect(
                        StringBuilder::new,
                        StringBuilder::append,
                        StringBuilder::append
                );
        return builder.toString();
    }
}
