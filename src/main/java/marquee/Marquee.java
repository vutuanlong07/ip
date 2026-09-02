package marquee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import marquee.tasks.DeadlineItem;
import marquee.tasks.EventItem;
import marquee.tasks.TaskItem;
import marquee.tasks.TodoItem;
import marquee.time.DateTimeFormatter;

/**
 * Main class for the standalone chatbot Marquee.
 *
 * @author Vu Tuan Long
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
        public static final String SUCCESS_LOAD = "Checklist loaded successfully \\(>e<)/\nCurrent checklist:\n%s\n";
        public static final String SUCCESS_SAVE = "Checklist saved successfully \\(>e<)/\nItem(s) written to file:\n%s\n";
        public static final String SUCCESS_LIST = "Current item(s) in your list:\n%s\n";
        public static final String SUCCESS_SEARCH = "Item(s) matching your search:\n%s\n";
        public static final String SUCCESS_ADD = "Added items(s):\n%s\nto the list (^_-☆ >c\nCurrently have %d item(s) in your checklist\n";
        public static final String SUCCESS_DELETE = "Deleted items(s):\n%s\nfrom the list (σ_σ.╒══⚟\nThere are %d item(s) left in your checklist\n";
        public static final String SUCCESS_MARK = "These item(s) were marked:\n%s\n";
        public static final String SUCCESS_UNMARK = "These item(s) were unmarked:\n%s\n";

        public static final String WARNING_LIST_EMPTY = "Your checklist is empty (‾ 3‾)\n";
        public static final String WARNING_SEARCH_EMPTY = "No items matched your search (‾ 3‾)\n";
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

        public static final String FATAL_ERROR_IO_UNAVAILABLE = "I can't see anything #.#\n";
    }

    private static final String CSV_SEPARATOR = ";";

    private final BufferedReader inputReader;
    private final Path savePath;

    private List<TaskItem> checklist;
    private boolean isRunning;

    /**
     * Instantiates an instance of Marquee and attempts to load its checklist from the given filepath.
     * If loading fails, starts with an empty checklist.
     *
     * @param inputStream the input stream Marquee will read commands from
     * @param savePath    the path to the CSV file Marquee will save its checklist to
     */
    public Marquee(InputStream inputStream, Path savePath) {
        this.inputReader = new BufferedReader(new InputStreamReader(inputStream));
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
            readCSV();
            System.out.printf(Dialogues.SUCCESS_LOAD, numberedList(checklist));
            return true;
        } catch (IOException e) {
            System.out.print(Dialogues.ERROR_SAVE_CORRUPTED);
            return false;
        }
    }

    /**
     * Saves the checklist into the save file.
     * If the operation fails,
     *
     * @return Whether the file was written successfully
     */
    public boolean saveChecklist() {
        try {
            writeCSV();
            System.out.printf(Dialogues.SUCCESS_SAVE, numberedList(checklist));
            return true;
        } catch (IOException e) {
            System.out.printf(Dialogues.ERROR_SAVE_UNAVAILABLE, e.getMessage());
            return false;
        }
    }


    public void exit() {
        if (saveChecklist()) {
            System.out.print(Dialogues.SUCCESS_EXIT);
            isRunning = false;
        }
    }

    public void list() {
        if (checklist.isEmpty()) {
            System.out.print(Dialogues.WARNING_LIST_EMPTY);
        } else {
            System.out.printf(Dialogues.SUCCESS_LIST, numberedList(checklist));
        }
    }

    public void search(String search, LocalDateTime start, LocalDateTime end, Boolean isMarked) {
        List<TaskItem> matchingItems = filterTasks(search, start, end, isMarked);
        if (matchingItems.isEmpty()) {
            System.out.print(Dialogues.WARNING_SEARCH_EMPTY);
        } else {
            System.out.printf(Dialogues.SUCCESS_SEARCH, numberedList(matchingItems));
        }
    }

    public void addTasks(TaskItem... tasks) {
        List<TaskItem> newTasks = List.of(tasks);
        checklist.addAll(newTasks);
        System.out.printf(Dialogues.SUCCESS_ADD, bulletList(newTasks), checklist.size());
    }

    public void deleteTasks(int... indices) {
        if (checklist.isEmpty()) {
            System.out.print(Dialogues.WARNING_LIST_EMPTY);
        }
        for (int i : indices) {
            if (i < 1 || i > checklist.size()) {
                System.out.printf(Dialogues.ERROR_INDEX, i);
                return;
            }
        }
        List<TaskItem> removedItems = IntStream.of(indices)
                .mapToObj(i -> checklist.remove(i - 1))
                .filter(Objects::nonNull)
                .toList();
        if (removedItems.isEmpty()) {
            System.out.print(Dialogues.WARNING_DELETE_EMPTY);
        } else {
            System.out.printf(Dialogues.SUCCESS_DELETE, bulletList(removedItems), checklist.size());
        }
    }

    public void markTasks(int... indices) {
        if (checklist.isEmpty()) {
            System.out.print(Dialogues.WARNING_LIST_EMPTY);
        }
        for (int i : indices) {
            if (i < 1 || i > checklist.size()) {
                System.out.printf(Dialogues.ERROR_INDEX, i);
                return;
            }
        }
        List<TaskItem> markedItems = IntStream.of(indices)
                .mapToObj(i -> checklist.get(i - 1))
                .filter(TaskItem::mark)
                .toList();
        if (markedItems.isEmpty()) {
            System.out.print(Dialogues.WARNING_MARK_EMPTY);
        } else {
            System.out.printf(Dialogues.SUCCESS_MARK, bulletList(markedItems));
        }
    }

    public void unmarkTasks(int... indices) {
        if (checklist.isEmpty()) {
            System.out.print(Dialogues.WARNING_LIST_EMPTY);
        }
        for (int i : indices) {
            if (i < 1 || i > checklist.size()) {
                System.out.printf(Dialogues.ERROR_INDEX, i);
                return;
            }
        }
        List<TaskItem> unmarkedItems = IntStream.of(indices)
                .mapToObj(i -> checklist.get(i - 1))
                .filter(TaskItem::unmark)
                .toList();
        if (unmarkedItems.isEmpty()) {
            System.out.print(Dialogues.WARNING_UNMARK_EMPTY);
        } else {
            System.out.printf(Dialogues.SUCCESS_UNMARK, bulletList(unmarkedItems));
        }
    }

    public void run() {
        isRunning = true;
        System.out.print(Dialogues.BANNER);
        System.out.print(Dialogues.GREETINGS);
        while (isRunning) {
            String input;
            Command command;

            try {
                input = getInput();
            } catch (IOException e) {
                System.out.print(Dialogues.FATAL_ERROR_IO_UNAVAILABLE);
                exit();
                continue;
            }

            try {
                command = Command.parseCommand(input);
            } catch (IllegalArgumentException e) {
                System.out.printf(Dialogues.ERROR_UNKNOWN_COMMAND, input);
                continue;
            }

            try {
                switch (command.code()) {
                    case EXIT -> exit();
                    case LOAD -> loadChecklist();
                    case SAVE -> saveChecklist();
                    case LIST -> list();
                    case SEARCH -> search(
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
                            System.out.print(Dialogues.ERROR_TASK_MISSING_NAME);
                        } else {
                            addTasks(new TodoItem(command.parameter()));
                        }
                    }
                    case DEADLINE -> {
                        if (command.parameter().isEmpty()) {
                            System.out.print(Dialogues.ERROR_TASK_MISSING_NAME);
                        } else if (!command.flags().containsKey("by")) {
                            System.out.print(Dialogues.ERROR_DEADLINE_MISSING_DEADLINE);
                        } else {
                            addTasks(new DeadlineItem(
                                    command.parameter(),
                                    DateTimeFormatter.parseDateTime(command.flags().get("by"))
                            ));
                        }
                    }
                    case EVENT -> {
                        if (command.parameter().isEmpty()) {
                            System.out.print(Dialogues.ERROR_TASK_MISSING_NAME);
                        } else if (!command.flags().containsKey("from")) {
                            System.out.print(Dialogues.ERROR_EVENT_MISSING_START_TIME);
                        } else if (!command.flags().containsKey("to")) {
                            System.out.print(Dialogues.ERROR_EVENT_MISSING_END_TIME);
                        } else {
                            try {
                                addTasks(new EventItem(
                                        command.parameter(),
                                        DateTimeFormatter.parseDateTime(command.flags().get("from")),
                                        DateTimeFormatter.parseDateTime(command.flags().get("to"))
                                ));
                            } catch (IllegalArgumentException _) {
                                System.out.print(Dialogues.ERROR_EVENT_END_BEFORE_START);
                            }
                        }
                    }
                    case DELETE -> {
                        try {
                            deleteTasks(parseIntArray(command.parameter()));
                        } catch (NumberFormatException e) {
                            System.out.printf(Dialogues.ERROR_NAN, e.getMessage());
                        }
                    }
                    case DELETE_ALL -> {
                        deleteTasks(IntStream.rangeClosed(1, checklist.size()).toArray());
                    }
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
                            System.out.printf(Dialogues.ERROR_NAN, e.getMessage());
                        }
                    }
                    case MARK_ALL -> {
                        markTasks(IntStream.rangeClosed(1, checklist.size()).toArray());
                    }
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
                            System.out.printf(Dialogues.ERROR_NAN, e.getMessage());
                        }
                    }
                    case UNMARK_ALL -> {
                        unmarkTasks(IntStream.rangeClosed(1, checklist.size()).toArray());
                    }
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
                System.out.print(Dialogues.ERROR_DATETIME);
            }
        }
    }

    public static void main(String[] args) {
        Marquee chatbot = new Marquee(System.in, Path.of("./checklist.csv"));
        chatbot.run();
    }

    private String getInput() throws IOException {
        System.out.print("\n> ");
        return inputReader.readLine();
    }

    private void readCSV() throws IOException {
        if (Files.isRegularFile(savePath) && Files.isReadable(savePath) && Files.isWritable(savePath)) {
            List<TaskItem> checklist = new ArrayList<>();
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
                if (lines[i].isEmpty()) continue;

                String[] fields = lines[i].split(CSV_SEPARATOR, -1);
                if (fields.length != headers.size()) {
                    throw new IOException("Save file is corrupted");
                }

                String content = fields[contentIdx];
                boolean isMarked = Boolean.parseBoolean(fields[isMarkedIdx]);
                String start = fields[startIdx];
                String end = fields[endIdx];
                try {
                    switch (TaskItem.ItemTag.fromLabel(fields[tagIdx])) {
                        case Todo -> checklist.add(new TodoItem(
                                content,
                                isMarked
                        ));
                        case Deadline -> checklist.add(new DeadlineItem(
                                content,
                                LocalDateTime.parse(end),
                                isMarked
                        ));
                        case Event -> checklist.add(new EventItem(content,
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

    private void writeCSV() throws IOException {
        Path temp = null;
        try {
            temp = Files.createTempFile(savePath.getParent(), null, null);
            Files.writeString(temp, Stream.concat(
                    Stream.of(String.join(CSV_SEPARATOR, "tag", "isMarked", "content", "start", "end")),
                    checklist.stream()
                            .map(taskItem -> String.join(CSV_SEPARATOR,
                                    taskItem.tag().label,
                                    Boolean.toString(taskItem.isMarked()),
                                    taskItem.content(),
                                    taskItem instanceof EventItem
                                            ? ((EventItem) taskItem).start().toString()
                                            : "",
                                    taskItem instanceof DeadlineItem
                                            ? ((DeadlineItem) taskItem).deadline().toString()
                                            : taskItem instanceof EventItem
                                              ? ((EventItem) taskItem).end().toString()
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

    private List<TaskItem> filterTasks(String search, LocalDateTime start, LocalDateTime end, Boolean isMarked) {
        return (search == null || search.isEmpty()) && start == null && end == null && isMarked == null
                ? List.of()
                : checklist.stream()
                .filter(isMarked == null
                        ? _ -> true
                        : item -> item.isMarked() == isMarked
                )
                .filter(start == null
                        ? _ -> true
                        : item -> item.isAfter(start)
                )
                .filter(end == null
                        ? _ -> true
                        : item -> item.isBefore(end)
                )
                .filter(search == null || search.isEmpty()
                        ? _ -> true
                        : item -> item.content().contains(search))
                .toList();
        }

    private static String numberedList(List<TaskItem> list) {
        StringBuilder builder = IntStream.range(0, list.size())
                .mapToObj(idx -> String.format("%d. %s\n", idx + 1, list.get(idx)))
                .collect(
                        StringBuilder::new,
                        StringBuilder::append,
                        StringBuilder::append
                );
        return builder.toString();
    }

    private static String bulletList(List<TaskItem> list) {
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
