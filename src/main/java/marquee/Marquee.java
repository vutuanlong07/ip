package marquee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import marquee.files.CsvFile;
import marquee.command.Command;
import marquee.command.DuplicateFlagException;
import marquee.command.UnknownFlagException;
import marquee.task.DeadlineTask;
import marquee.task.EventTask;
import marquee.task.Task;
import marquee.task.TodoTask;
import marquee.time.DateTimeFormatter;

/**
 * Main class for the standalone chatbot Marquee.
 */
public class Marquee {
    /**
     * Data class for all dialogues used by Marquee.
     */
    public static final class Dialogues {
        public static final String BANNER = """
            ____  ___
            |   \\/   |  _____   ____   _____   _   _   ____   ____
            | |\\  /| | / .__ /  | .-` / ._. \\ | | | | / ___) / ___)
            | | || | | | |_/ |  | |   | |_| | | |_| | | ___) | ___)
            |_| |/ |_| \\____/\\_ |_|   \\___  | \\_____\\ \\____) \\____)
                                          | |
                                          | |
                                           \\|
            """;
        public static final String GREETINGS = "Hi! I'm Marquee \\(>e<)/\nWhat will we do today? xD\n";

        public static final String SUCCESS_EXIT = "See you later :3\n";
        public static final String SUCCESS_LOAD = "Checklist loaded successfully! :D\n";
        public static final String SUCCESS_SAVE = "Checklist saved successfully :D\n";
        public static final String SUCCESS_LIST = "Current item(s) in your list:\n%s\n";
        public static final String SUCCESS_FIND = "Item(s) matching your search:\n%s\n";
        public static final String SUCCESS_ADD = "Added items(s):\n%s\nto the list (^_-☆ >c\nCurrently have %d item(s) in your checklist\n";
        public static final String SUCCESS_DELETE = "Deleted items(s):\n%s\nfrom the list (σ_σ.╒══⚟\nThere are %d item(s) left in your checklist\n";
        public static final String SUCCESS_MARK = "These item(s) were marked:\n%s\n";
        public static final String SUCCESS_UNMARK = "These item(s) were unmarked:\n%s\n";

        public static final String WARNING_LIST_EMPTY = "Your checklist is empty (‾ 3‾)\n";
        public static final String WARNING_FIND_EMPTY = "No items matched your search (‾ 3‾)\n";
        public static final String WARNING_MARK_EMPTY = "No items were marked (‾ 3‾)\n";
        public static final String WARNING_UNMARK_EMPTY = "No items were unmarked (‾ 3‾)\n";
        public static final String WARNING_DELETE_EMPTY = "No items were deleted (‾ 3‾)\n";

        public static final String ERROR_TASK_MISSING_NAME = "Task name can't be empty, duh °∀°?\n";
        public static final String ERROR_DEADLINE_MISSING_DEADLINE = "Task has no deadline? °∀°?";
        public static final String ERROR_EVENT_MISSING_START_TIME = "Event can't start without start time °∀°?\n";
        public static final String ERROR_EVENT_MISSING_END_TIME = "Event can't end without end time °∀°?\n";
        public static final String ERROR_EVENT_END_BEFORE_START = "Event ending before it starts? °∀°?\n";

        public static final String ERROR_SAVE_CORRUPTED = "..ca.che..fi.le..cor.rup.te..d.  Σ( ﾟДﾟ)!\n";
        public static final String ERROR_SAVE_UNAVAILABLE = "Somehow can't write save file?! Σ( ﾟДﾟ)!\nCause: %s\n";
        public static final String ERROR_INDEX = "%d is not a valid index! >_<\n";
        public static final String ERROR_NAN = "'%s' is not a number! (@ ~ @)\n";
        public static final String ERROR_DATETIME = "Not a valid date! (@ ~ @)\n";
        public static final String ERROR_UNKNOWN_COMMAND = "No clue what '%s' means `O ᗝ O´╬\n";
        public static final String ERROR_UNKNOWN_FLAG = "Flag /%s doesn't mean anything in %s `O ᗝ O´╬\n";
        public static final String ERROR_DUPLICATE_FLAG = "Too many /%s `O ᗝ O´╬\n";

        public static final String FATAL_ERROR_IO_UNAVAILABLE = "I can't see anything #.#\n";
    }

    private static final String CSV_SEPARATOR = ";";

    private final BufferedReader inputReader;
    private final PrintStream outputStream;
    private final Path savePath;

    private final List<Task> checklist;
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
    public boolean loadChecklist() {
        try {
            readCsv();
            outputStream.print(Dialogues.SUCCESS_LOAD);
            return true;
        } catch (IOException e) {
            outputStream.print(Dialogues.ERROR_SAVE_CORRUPTED);
            return false;
        }
    }

    /**
     * Saves the checklist into the save file.
     * If the operation fails, the original save file will not be changed.
     *
     * @return Whether the file was written successfully
     */
    public boolean saveChecklist() {
        try {
            writeCsv();
            outputStream.print(Dialogues.SUCCESS_SAVE);
            return true;
        } catch (IOException e) {
            outputStream.printf(Dialogues.ERROR_SAVE_UNAVAILABLE, e.getMessage());
            return false;
        }
    }

    /**
     * Tells the chatbot to save checklist and ends the session.
     */
    public void exit() {
        if (saveChecklist()) {
            outputStream.print(Dialogues.SUCCESS_EXIT);
            isRunning = false;
        }
    }

    /**
     * Lists the items in the checklist to the output stream.
     * If there are none, output a different message clarifying that the checklist is empty.
     */
    public void list() {
        if (checklist.isEmpty()) {
            outputStream.print(Dialogues.WARNING_LIST_EMPTY);
        } else {
            outputStream.printf(Dialogues.SUCCESS_LIST, numberedList(checklist));
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
    public void find(String description, LocalDateTime start, LocalDateTime end, Boolean isMarked) {
        List<Task> matchingItems = filterTasks(description, start, end, isMarked);
        if (matchingItems.isEmpty()) {
            outputStream.print(Dialogues.WARNING_FIND_EMPTY);
        } else {
            outputStream.printf(Dialogues.SUCCESS_FIND, numberedList(matchingItems));
        }
    }

    /**
     * Adds tasks to the checklist, then list the added tasks.
     *
     * @param tasks the tasks to be added to the checklist
     */
    public void addTasks(Task... tasks) {
        List<Task> newTasks = List.of(tasks);
        checklist.addAll(newTasks);
        outputStream.printf(Dialogues.SUCCESS_ADD, bulletList(newTasks), checklist.size());
    }

    /**
     * Deletes tasks from the checklist by index, then list the deleted tasks.
     *
     * @param indices the indices of the tasks to be deleted
     */
    public void deleteTasks(int... indices) {
        if (checklist.isEmpty()) {
            outputStream.print(Dialogues.WARNING_LIST_EMPTY);
        }
        for (int i : indices) {
            if (i < 1 || i > checklist.size()) {
                outputStream.printf(Dialogues.ERROR_INDEX, i);
                return;
            }
        }
        List<Task> removedItems = IntStream.of(indices)
                .mapToObj(i -> checklist.remove(i - 1))
                .filter(Objects::nonNull)
                .toList();
        if (removedItems.isEmpty()) {
            outputStream.print(Dialogues.WARNING_DELETE_EMPTY);
        } else {
            outputStream.printf(Dialogues.SUCCESS_DELETE, bulletList(removedItems), checklist.size());
        }
    }

    /**
     * Marks tasks from the checklist as completed by index, then list the marked tasks.
     *
     * @param indices the indices of the tasks to be marked
     */
    public void markTasks(int... indices) {
        if (checklist.isEmpty()) {
            outputStream.print(Dialogues.WARNING_LIST_EMPTY);
        }
        for (int i : indices) {
            if (i < 1 || i > checklist.size()) {
                outputStream.printf(Dialogues.ERROR_INDEX, i);
                return;
            }
        }
        List<Task> markedItems = IntStream.of(indices)
                .mapToObj(i -> checklist.get(i - 1))
                .filter(Task::mark)
                .toList();
        if (markedItems.isEmpty()) {
            outputStream.print(Dialogues.WARNING_MARK_EMPTY);
        } else {
            outputStream.printf(Dialogues.SUCCESS_MARK, bulletList(markedItems));
        }
    }

    /**
     * Unmarks (mark as incomplete) tasks from the checklist by index, then list the unmarked tasks.
     *
     * @param indices the indices of the tasks to be unmarked
     */
    public void unmarkTasks(int... indices) {
        if (checklist.isEmpty()) {
            outputStream.print(Dialogues.WARNING_LIST_EMPTY);
        }
        for (int i : indices) {
            if (i < 1 || i > checklist.size()) {
                outputStream.printf(Dialogues.ERROR_INDEX, i);
                return;
            }
        }
        List<Task> unmarkedItems = IntStream.of(indices)
                .mapToObj(i -> checklist.get(i - 1))
                .filter(Task::unmark)
                .toList();
        if (unmarkedItems.isEmpty()) {
            outputStream.print(Dialogues.WARNING_UNMARK_EMPTY);
        } else {
            outputStream.printf(Dialogues.SUCCESS_UNMARK, bulletList(unmarkedItems));
        }
    }

    /**
     * Starts the chatbot loop. Marquee will listen from the input stream
     * and print to the output stream given in the constructor.
     */
    public void run() {
        isRunning = true;
        outputStream.print(Dialogues.BANNER);
        outputStream.print(Dialogues.GREETINGS);
        while (isRunning) {
            String input;
            Command command;

            try {
                input = getInput();
            } catch (IOException e) {
                outputStream.print(Dialogues.FATAL_ERROR_IO_UNAVAILABLE);
                exit();
                continue;
            }

            try {
                command = Command.parseCommand(input);
            } catch (UnknownFlagException e) {
                outputStream.printf(Dialogues.ERROR_UNKNOWN_FLAG, e.getFlagName(), e.getCode().getCodeString());
                continue;
            } catch (DuplicateFlagException e) {
                outputStream.printf(Dialogues.ERROR_DUPLICATE_FLAG, e.getFlagName());
                continue;
            } catch (IllegalArgumentException e) {
                outputStream.printf(Dialogues.ERROR_UNKNOWN_COMMAND, input);
                continue;
            }

            try {
                switch (command.code()) {
                    case EXIT -> exit();
                    case LOAD -> loadChecklist();
                    case SAVE -> saveChecklist();
                    case LIST -> list();
                    case FIND -> find(
                            command.parameter(),
                            command.flags().containsKey("from")
                                    ? DateTimeFormatter.parseDateTime(command.flags().get("from"))
                                    : null,
                            command.flags().containsKey("to")
                                    ? DateTimeFormatter.parseDateTime(command.flags().get("to"))
                                    : null,
                            command.flags().containsKey("completed") != command.flags().containsKey("incomplete")
                                    ? command.flags().containsKey("completed")
                                    : null
                    );
                    case TODO -> {
                        if (command.parameter().isEmpty()) {
                            outputStream.print(Dialogues.ERROR_TASK_MISSING_NAME);
                        } else {
                            addTasks(new TodoTask(command.parameter()));
                        }
                    }
                    case DEADLINE -> {
                        if (command.parameter().isEmpty()) {
                            outputStream.print(Dialogues.ERROR_TASK_MISSING_NAME);
                        } else if (!command.flags().containsKey("by")) {
                            outputStream.print(Dialogues.ERROR_DEADLINE_MISSING_DEADLINE);
                        } else {
                            addTasks(new DeadlineTask(
                                    command.parameter(),
                                    DateTimeFormatter.parseDateTime(command.flags().get("by"))
                            ));
                        }
                    }
                    case EVENT -> {
                        if (command.parameter().isEmpty()) {
                            outputStream.print(Dialogues.ERROR_TASK_MISSING_NAME);
                        } else if (!command.flags().containsKey("from")) {
                            outputStream.print(Dialogues.ERROR_EVENT_MISSING_START_TIME);
                        } else if (!command.flags().containsKey("to")) {
                            outputStream.print(Dialogues.ERROR_EVENT_MISSING_END_TIME);
                        } else {
                            try {
                                addTasks(new EventTask(
                                        command.parameter(),
                                        DateTimeFormatter.parseDateTime(command.flags().get("from")),
                                        DateTimeFormatter.parseDateTime(command.flags().get("to"))
                                ));
                            } catch (IllegalArgumentException _) {
                                outputStream.print(Dialogues.ERROR_EVENT_END_BEFORE_START);
                            }
                        }
                    }
                    case DELETE -> {
                        try {
                            deleteTasks(parseIntArray(command.parameter()));
                        } catch (NumberFormatException e) {
                            outputStream.printf(Dialogues.ERROR_NAN, e.getMessage());
                        }
                    }
                    case DELETE_ALL -> deleteTasks(IntStream.rangeClosed(1, checklist.size()).toArray());
                    case DELETE_MATCHING -> deleteTasks(
                            filterTasks(
                                    command.parameter(),
                                    command.flags().containsKey("from")
                                            ? DateTimeFormatter.parseDateTime(command.flags().get("from"))
                                            : null,
                                    command.flags().containsKey("to")
                                            ? DateTimeFormatter.parseDateTime(command.flags().get("to"))
                                            : null,
                                    command.flags().containsKey("completed") != command.flags().containsKey("incomplete")
                                            ? command.flags().containsKey("completed")
                                            : null
                            ).stream()
                                    .mapToInt(item -> checklist.indexOf(item) + 1)
                                    .toArray()
                    );
                    case MARK -> {
                        try {
                            markTasks(parseIntArray(command.parameter()));
                        } catch (NumberFormatException e) {
                            outputStream.printf(Dialogues.ERROR_NAN, e.getMessage());
                        }
                    }
                    case MARK_ALL -> markTasks(IntStream.rangeClosed(1, checklist.size()).toArray());
                    case MARK_MATCHING -> markTasks(
                            filterTasks(
                                    command.parameter(),
                                    command.flags().containsKey("from")
                                            ? DateTimeFormatter.parseDateTime(command.flags().get("from"))
                                            : null,
                                    command.flags().containsKey("to")
                                            ? DateTimeFormatter.parseDateTime(command.flags().get("to"))
                                            : null,
                                    command.flags().containsKey("completed") != command.flags().containsKey("incomplete")
                                            ? command.flags().containsKey("completed")
                                            : null
                            ).stream()
                                    .mapToInt(item -> checklist.indexOf(item) + 1)
                                    .toArray()
                    );
                    case UNMARK -> {
                        try {
                            unmarkTasks(parseIntArray(command.parameter()));
                        } catch (NumberFormatException e) {
                            outputStream.printf(Dialogues.ERROR_NAN, e.getMessage());
                        }
                    }
                    case UNMARK_ALL -> unmarkTasks(IntStream.rangeClosed(1, checklist.size()).toArray());
                    case UNMARK_MATCHING -> unmarkTasks(
                            filterTasks(
                                    command.parameter(),
                                    command.flags().containsKey("from")
                                            ? DateTimeFormatter.parseDateTime(command.flags().get("from"))
                                            : null,
                                    command.flags().containsKey("to")
                                            ? DateTimeFormatter.parseDateTime(command.flags().get("to"))
                                            : null,
                                    command.flags().containsKey("completed") != command.flags().containsKey("incomplete")
                                            ? command.flags().containsKey("completed")
                                            : null
                            ).stream()
                                    .mapToInt(item -> checklist.indexOf(item) + 1)
                                    .toArray()
                    );
                }
            } catch (DateTimeParseException e) {
                outputStream.print(Dialogues.ERROR_DATETIME);
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

    private void readCsv() throws IOException {
        if (Files.isRegularFile(savePath) && Files.isReadable(savePath) && Files.isWritable(savePath)) {
            List<TodoTask> checklist = new ArrayList<>();
            String[] lines = Files.readString(savePath).split("\r\n");

            List<String> headers = Arrays.asList(lines[0].split(CSV_SEPARATOR));
            int tagIdx = headers.indexOf("tag");
            int isMarkedIdx = headers.indexOf("isMarked");
            int contentIdx =  headers.indexOf("content");
            int startIdx = headers.indexOf("start");
            int endIdx = headers.indexOf("end");
            if (tagIdx == -1 || contentIdx == -1 || isMarkedIdx == -1 || startIdx == -1 || endIdx == -1) {
                throw new IOException("Save file is corrupted");
            }

            for (int i = 1; i < lines.length; i++) {
                if (lines[i].isEmpty()) {
                    continue;
                }

                String[] fields = lines[i].split(CSV_SEPARATOR, -1);
                if (fields.length != headers.size()) {
                    throw new IOException("Save file is corrupted");
                }

                String content = fields[contentIdx];
                boolean isMarked = Boolean.parseBoolean(fields[isMarkedIdx]);
                String start = fields[startIdx];
                String end = fields[endIdx];
                try {
                    switch (TaskTag.fromLabel(fields[tagIdx])) {
                        case TODO -> checklist.add(new TodoTask(
                                content,
                                isMarked
                        ));
                        case DEADLINE -> checklist.add(new DeadlineTask(
                                content,
                                LocalDateTime.parse(end),
                                isMarked
                        ));
                        case EVENT -> checklist.add(new EventTask(content,
                                LocalDateTime.parse(start),
                                LocalDateTime.parse(end),
                                isMarked
                        ));
                        case null -> {}
                    }
                } catch (IllegalArgumentException | DateTimeParseException _) {
                    throw new IOException("Save file is corrupted");
                }
            }
            this.checklist = checklist;
        } else {
            throw new IOException("File not found or inaccessible");
        }
    }

    private void writeCsv() throws IOException {
        Path temp = null;
        try {
            temp = Files.createTempFile(savePath.getParent(), null, null);
            Files.writeString(temp, Stream.concat(
                    Stream.of(String.join(CSV_SEPARATOR, "tag", "isMarked", "content", "start", "end")),
                    checklist.stream()
                            .map(todoTask -> String.join(CSV_SEPARATOR,
                                    todoTask.getTag().getLabel(),
                                    Boolean.toString(todoTask.isMarked()),
                                    todoTask.getDescription(),
                                    todoTask instanceof EventTask
                                            ? ((EventTask) todoTask).getStart().toString()
                                            : "",
                                    todoTask instanceof DeadlineTask
                                            ? ((DeadlineTask) todoTask).deadline().toString()
                                            : todoTask instanceof EventTask
                                              ? ((EventTask) todoTask).getEnd().toString()
                                              : ""
                            ))
            ).collect(Collectors.joining("\r\n")));
            Files.move(temp, savePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            if (temp != null) {
                Files.deleteIfExists(temp);
            }
        }
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
