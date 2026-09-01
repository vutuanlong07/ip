import tasks.DeadlineItem;
import tasks.EventItem;
import tasks.TaskItem;
import tasks.TodoItem;
import time.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Marquee {
    public static final class Dialogues {
        public static final String BANNER = """
            ____  ___
            |   \\/   |  _____   ____   _____   _   _   ____   ____
            | |\\  /| | / .__ /  | .-` / ._. \\ | | | | / ___) / ___)
            | | || | | | |_/ |  | |   | |_| | | |_| | | ___) | ___)
            |_| |/ |_| \\____/\\_ |_|   \\___  | \\_____\\ \\____) \\____)
                                          | |
                                          | |
                                           \\|""";
        public static final String START_MESSAGE = "Hi! I'm Marquee \\(>e<)/\nWhat will we do today? xD\n";
        public static final String EXIT_MESSAGE = "See you later :3\n";

        public static final String SUCCESS_LIST = "Current item(s) in your list:\n%s\n";
        public static final String SUCCESS_ADD = "Added task:\n  %s\nto the list (^_-☆ >c\nCurrently have %d item(s) in your checklist\n";
        public static final String SUCCESS_MARK = "These item(s) were marked:\n%s\n";
        public static final String SUCCESS_UNMARK = "These item(s) were unmarked:\n%s\n";
        public static final String SUCCESS_DELETE = "These item(s) were deleted:\n%s\nThere are %d item(s) left in your checklist\n";

        public static final String WARNING_LIST_EMPTY = "Your checklist is empty (‾ 3‾)\n";
        public static final String WARNING_MARKED_EMPTY = "No items were marked (‾ 3‾)\n";
        public static final String WARNING_UNMARK_EMPTY = "No items were unmarked (‾ 3‾)\n";
        public static final String WARNING_DELETE_EMPTY = "No items were deleted (‾ 3‾)\n";

        public static final String ERROR_MARK_MISSING_ARGUMENT = "Nothing to mark -_-'\n";
        public static final String ERROR_UNMARK_MISSING_ARGUMENT = "Nothing to unmark -_-'\n";
        public static final String ERROR_DELETE_MISSING_ARGUMENT = "Nothing to delete -_-'\n";
        public static final String ERROR_TASK_MISSING_NAME = "Task name can't be empty, duh °∀°?\n";
        public static final String ERROR_DEADLINE_MISSING_DEADLINE = "Task has no deadline? °∀°";
        public static final String ERROR_EVENT_MISSING_START_TIME = "Event can't start without start time °∀°\n";
        public static final String ERROR_EVENT_MISSING_END_TIME = "Event can't end without end time °∀°\n";

        public static final String ERROR_SAVE_CORRUPTED = "..ca.che..fi.le..cor.rup.te..d.  Σ( ﾟДﾟ)\n";
        public static final String ERROR_SAVE_DENIED = "Somehow can't write save file?! Σ( ﾟДﾟ)\nCause: %s\n";
        public static final String ERROR_INDEX = "%d is not a valid index! >_<\n";
        public static final String ERROR_NAN = "'%s' is not a number! (@ ~ @)\n";
        public static final String ERROR_DATETIME = "Not a valid date! (@ ~ @)\n";
        public static final String ERROR_COMMAND = "No clue what %s means `O ᗝ O´╬\n";
    }

    private static final String CSV_SEPARATOR = ";";

    private static final Path checklistPath = Path.of("./checklist.csv");
    private static final BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) throws IOException {
        System.out.println(Dialogues.Banner);
        List<TaskItem> checklist = readCSV();
        System.out.println(Dialogues.Greetings);
        while (true) {
            String rawInput = getInput();
            Command command = Command.parseCommand(rawInput);
            switch (command.code()) {
                case Command.Code.EXIT -> {
                    System.out.println(Dialogues.Goodbye);
                    writeCSV(checklist);
                    return;
                }
                case Command.Code.LIST -> {
                    if (checklist.isEmpty()) {
                        System.out.println(Dialogues.DisplayListEmpty);
                    } else {
                        System.out.println(Dialogues.DisplayListSuccessful);
                        System.out.print(numberedList(checklist));
                    }
                }
                case Command.Code.TODO -> {
                    if (command.parameter().isEmpty()) {
                        System.out.println(Dialogues.TaskItemMissingName);
                    } else {
                        TodoItem toDoItem = new TodoItem(command.parameter());
                        checklist.add(toDoItem);
                        System.out.printf(Dialogues.AddTaskSuccessful + "\n", toDoItem, checklist.size());
                    }
                }
                case Command.Code.DEADLINE -> {
                    if (command.parameter().isEmpty()) {
                        System.out.println(Dialogues.TaskItemMissingName);
                    } else if (!command.flags().containsKey("by")) {
                        System.out.println(Dialogues.DeadlineMissing);
                    } else {
                        try {
                            DeadlineItem deadlineItem = new DeadlineItem(
                                    command.parameter(),
                                    DateTimeFormatter.parseDateTime(command.flags().get("by"))
                            );
                            checklist.add(deadlineItem);
                            System.out.printf(Dialogues.AddTaskSuccessful + "\n", deadlineItem, checklist.size());
                        } catch (DateTimeParseException e) {
                            System.out.println(Dialogues.InvalidDateTime);
                        }
                    }
                }
                case Command.Code.EVENT -> {
                    if (command.parameter().isEmpty()) {
                        System.out.println(Dialogues.TaskItemMissingName);
                    } else if (!command.flags().containsKey("from")) {
                        System.out.println(Dialogues.EventMissingStart);
                    } else if (!command.flags().containsKey("to")) {
                        System.out.println(Dialogues.EventMissingEnd);
                    } else {
                        EventItem eventItem = new EventItem(
                                command.parameter(),
                                DateTimeFormatter.parseDateTime(command.flags().get("from")),
                                DateTimeFormatter.parseDateTime(command.flags().get("to"))
                        );
                        checklist.add(eventItem);
                        System.out.printf(Dialogues.AddTaskSuccessful + "\n", eventItem, checklist.size());
                    }
                }
                case Command.Code.DELETE -> {
                    if (command.parameter().isEmpty()) {
                        System.out.println(Dialogues.DeleteMissingArguments);
                    } else {
                        List<TaskItem> modified = parseIndexArray(command.parameter(), checklist.size())
                                .mapToObj(checklist::remove)
                                .toList();
                        if (modified.isEmpty()) {
                            System.out.println(Dialogues.DeleteNoChange);
                        } else {
                            System.out.printf(Dialogues.DeleteSuccessful + "\n", bulletList(modified), checklist.size());
                        }
                    }
                }
                case Command.Code.MARK -> {
                    if (command.parameter().isEmpty()) {
                        System.out.println(Dialogues.MarkMissingArguments);
                    } else {
                        List<TaskItem> modified = parseIndexArray(command.parameter(), checklist.size())
                                .mapToObj(checklist::get)
                                .filter(TaskItem::mark)
                                .toList();
                        if (modified.isEmpty()) {
                            System.out.println(Dialogues.MarkNoChange);
                        } else {
                            System.out.println(Dialogues.MarkSuccessful);
                            System.out.print(bulletList(modified));
                        }
                    }
                }
                case Command.Code.UNMARK -> {
                    if (command.parameter().isEmpty()) {
                        System.out.println(Dialogues.UnmarkMissingArguments);
                    } else {
                        List<TaskItem> modified = parseIndexArray(command.parameter(), checklist.size())
                                .mapToObj(checklist::get)
                                .filter(TaskItem::unmark)
                                .toList();
                        if (modified.isEmpty()) {
                            System.out.println(Dialogues.UnmarkNoChange);
                        } else {
                            System.out.println(Dialogues.UnmarkSuccessful);
                            System.out.print(bulletList(modified));
                        }
                    }
                }
            }
        }
    }

    public static String getInput() throws IOException {
        System.out.print("\n> ");
        return inputReader.readLine();
    }

    private static List<TaskItem> readCSV(Path file) throws IOException {
        try {
            Files.createFile(file);
            Files.writeString(file, String.join(CSV_SEPARATOR, "tag", "marked", "content", "start", "end"));
            return new ArrayList<>();
        } catch (FileAlreadyExistsException _) {
            List<TaskItem> checklist = new ArrayList<>();
            String[] lines = Files.readString(file).split("\r\n");
            boolean corrupted = false;

            List<String> headers = Arrays.asList(lines[0].split(CSV_SEPARATOR));
            int tagIdx = headers.indexOf("tag");
            int markedIdx = headers.indexOf("marked");
            int contentIdx =  headers.indexOf("content");
            int startIdx = headers.indexOf("start");
            int endIdx = headers.indexOf("end");
            if (tagIdx != -1 && contentIdx != -1 && markedIdx != -1 && startIdx != -1 && endIdx != -1) {
                for (int i = 1; i < lines.length; i++) {
                    if (lines[i].isEmpty()) continue;

                    String[] fields = lines[i].split(CSV_SEPARATOR, -1);
                    if (fields.length != headers.size()) corrupted = true;
                    try {
                        String content = fields[contentIdx];
                        boolean marked = Boolean.parseBoolean(fields[markedIdx]);
                        String start = fields[startIdx];
                        String end = fields[endIdx];
                        switch (TaskItem.ItemTag.fromLabel(fields[tagIdx])) {
                            case Todo -> checklist.add(new TodoItem(
                                    content,
                                    marked
                            ));
                            case Deadline -> checklist.add(new DeadlineItem(
                                    content,
                                    LocalDateTime.parse(end),
                                    marked
                            ));
                            case Event -> checklist.add(new EventItem(content,
                                    LocalDateTime.parse(start),
                                    LocalDateTime.parse(end),
                                    marked
                            ));
                            case null -> {}
                        }
                    }
                    catch (IllegalArgumentException | IndexOutOfBoundsException _) { corrupted = true; }
                }
            } else corrupted = true;

            if (corrupted) {
                System.out.println(Dialogues.CorruptedCacheFile);
            }
            return checklist;
        }
    }

    private static void writeCSV(List<TaskItem> checklist, Path file) throws IOException {
        Files.writeString(file, Stream.concat(
            Stream.of(String.join(CSV_SEPARATOR, "tag", "marked", "content", "start", "end")),
            checklist.stream()
                    .map(taskItem -> String.join(CSV_SEPARATOR,
                            taskItem.tag().label,
                            Boolean.toString(taskItem.marked()),
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
    }

    private static IntStream parseIndexArray(String input, int capacity) {
        return Arrays.stream(input.split(" ", -1))
                .mapToInt(idxStr -> {
                    if (!idxStr.isEmpty()) try {
                        int idx = Integer.parseInt(idxStr) - 1;
                        if (idx < 0)
                            System.out.printf(Dialogues.IndexInvalid + "\n", idx + 1);
                        else if (idx >= capacity)
                            System.out.printf(Dialogues.IndexTooLarge + "\n", idx + 1, capacity);
                        else
                            return idx;
                    } catch (NumberFormatException e) {
                        System.out.printf(Dialogues.ArgumentNAN + "\n", idxStr);
                    }
                    return -1;
                })
                .filter(idx -> idx != -1);
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
