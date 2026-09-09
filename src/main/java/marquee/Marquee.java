package marquee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import marquee.base.command.Command;
import marquee.base.command.CommandFormatter;
import marquee.base.command.DuplicateFlagException;
import marquee.base.command.UnknownFlagException;
import marquee.base.io.CsvTable;
import marquee.base.task.Task;
import marquee.base.task.TaskTag;
import marquee.base.time.DateTimeFormatter;
import marquee.command.BaseCodes;
import marquee.task.DeadlineTask;
import marquee.task.EventTask;
import marquee.task.TodoTask;

/**
 * Main class for the standalone chatbot Marquee.
 */
public class Marquee {
    private final BufferedReader inputReader;
    private final PrintStream outputStream;
    private final Path savePath;

    private boolean isRunning = false;
    private final List<Task> checklist = new ArrayList<>();
    private List<Task> tempList = checklist;
    private Dialogues dialogues;
    private CommandFormatter commandFormatter;

    /**
     * Instantiates an instance of Marquee and attempts to load its checklist from {@code savePath}.
     * <p>
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

        useDialogues(new Dialogues());
        useCommandFormatter(new CommandFormatter("/", "\\"));
        loadChecklist();
    }

    /**
     * Tells {@code Marquee} to use the given dialogue set to communicate with the user.
     * <p>
     * To use a custom dialogue set, override the {@link Dialogues} class
     * then pass an instance to this method.
     *
     * @param dialogues the dialogue set to use
     */
    protected final void useDialogues(Dialogues dialogues) {
        this.dialogues = dialogues;
    }

    /**
     * Tells {@code Marquee} to use the given {@code CommandFormatter} to parse commands.
     * <p>
     * To use a custom command formatter, override the {@link CommandFormatter} class
     * then pass an instance to this method.
     *
     * @param commandFormatter the {@code CommandFormatter} to use
     */
    protected final void useCommandFormatter(CommandFormatter commandFormatter) {
        this.commandFormatter = commandFormatter;
    }

    /**
     * Formats the list of {@code Task} into a {@code CsvTable} for storage.
     * <p>
     *
     * @param list the list of {@link Task} to format
     * @return a new {@link CsvTable} containing the formatted tasks
     * @implSpec Override this to account for new {@link Task} subclasses
     */
    protected CsvTable listToCsv(List<Task> list) {
        Set<String> columnNames = list.stream()
                .flatMap(task -> task.toValueMap().keySet().stream())
                .collect(Collectors.toSet());
        List<String> columnNamesWithTag = Stream.concat(Stream.of(Task.TAG_COLUMN), columnNames.stream()).toList();
        CsvTable csv = new CsvTable(columnNamesWithTag, ";");
        list.forEach(task -> csv.add(csv.createPartialRecord(
                Stream.concat(
                        Stream.of(Map.entry(Task.TAG_COLUMN, task.getTaskTag().getLabel())),
                        task.toValueMap().entrySet().stream()
                ).collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ))
        )));
        return csv;
    }

    /**
     * Extracts {@code Task} items from {@code CsvTable} into the checklist.
     * <p>
     *
     * @param csv the {@link CsvTable} to read from
     * @return a new list containing the parsed {@link Task}
     * @throws IllegalArgumentException if an unsupported or unknown task tag is found
     * @implSpec Override this to account for new {@link Task} subclasses
     */
    protected List<Task> csvToList(CsvTable csv)
            throws IllegalArgumentException {
        List<Task> list = new ArrayList<>();
        csv.getValues().forEach(record -> {
            TaskTag<?> tag = TaskTag.fromLabel(record.getField(Task.TAG_COLUMN));
            try {
                list.add(Task.reconstructTask(tag, record.getAllFields()));
            } catch (NoSuchMethodException | InvocationTargetException
                    | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        return list;
    }

    /**
     * Attempts to load the checklist from the save file.
     * <p>
     * If the operation fails, no change is made to the checklist.
     *
     * @return Whether the file was read successfully
     */
    public final boolean loadChecklist() {
        try {
            List<Task> newChecklist = csvToList(CsvTable.readFile(savePath, ";"));
            checklist.clear();
            checklist.addAll(newChecklist);
            outputStream.print(this.dialogues.successLoad());
            return true;
        } catch (NoSuchFileException _) {
            outputStream.print(this.dialogues.warningSaveFileNotFound());
        } catch (IOException | ParseException | IllegalArgumentException e) {
            outputStream.print(this.dialogues.errorSaveCorrupted());
            outputStream.print(e.getMessage());
        }
        return false;
    }

    /**
     * Saves the checklist into the save file.
     * <p>
     * If the operation fails, the original save file will not be changed.
     *
     * @return Whether the file was written successfully
     */
    public final boolean saveChecklist() {
        try {
            CsvTable.writeFile(savePath, listToCsv(checklist));
            outputStream.print(this.dialogues.successSave());
            return true;
        } catch (IOException e) {
            outputStream.print(this.dialogues.errorSaveUnavailable(e.getMessage()));
            return false;
        }
    }

    /**
     * Tells the chatbot to save checklist and ends the session.
     */
    public final void exit() {
        if (saveChecklist()) {
            outputStream.print(this.dialogues.successExit());
            isRunning = false;
        }
    }

    /**
     * Lists the items in the checklist to the output stream.
     * <p>
     * If there are none, output a different message clarifying that the checklist is empty.
     */
    public final void list() {
        if (checklist.isEmpty()) {
            outputStream.print(this.dialogues.warningListEmpty());
        } else {
            outputStream.print(this.dialogues.successList(checklist));
        }
        tempList = checklist;
    }

    /**
     * Searches for items in the checklist satisfying the search conditions, then list those tasks.
     * <p>
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
            outputStream.print(this.dialogues.warningFindEmpty());
        } else {
            outputStream.print(this.dialogues.successFind(matchingItems));
        }
        tempList = matchingItems;
    }

    /**
     * Adds tasks to the checklist, then list the added tasks.
     *
     * @param tasks the tasks to be added to the checklist
     */
    public final void addTasks(Task... tasks) {
        List<Task> newTasks = List.of(tasks);
        checklist.addAll(newTasks);
        outputStream.print(this.dialogues.successAdd(newTasks, checklist.size()));
        tempList = newTasks;
    }

    /**
     * Deletes tasks from the checklist by index, then list the deleted tasks.
     *
     * @param indices the indices of the tasks to be deleted
     */
    public final void deleteTasks(int... indices) {
        if (checklist.isEmpty()) {
            outputStream.print(this.dialogues.warningListEmpty());
        }
        for (int i : indices) {
            if (i < 1 || i > checklist.size()) {
                outputStream.print(this.dialogues.errorIndex(i));
                return;
            }
        }
        List<Task> removedItems = IntStream.of(indices)
                .mapToObj(i -> checklist.remove(i - 1))
                .filter(Objects::nonNull)
                .toList();
        if (removedItems.isEmpty()) {
            outputStream.print(this.dialogues.warningDeleteEmpty());
        } else {
            outputStream.print(this.dialogues.successDelete(removedItems, checklist.size()));
        }
        tempList = removedItems;
    }

    /**
     * Marks tasks from the checklist as completed by index, then list the marked tasks.
     *
     * @param indices the indices of the tasks to be marked
     */
    public final void markTasks(int... indices) {
        if (checklist.isEmpty()) {
            outputStream.print(this.dialogues.warningListEmpty());
        }
        for (int i : indices) {
            if (i < 1 || i > checklist.size()) {
                outputStream.print(this.dialogues.errorIndex(i));
                return;
            }
        }
        List<Task> markedItems = IntStream.of(indices)
                .mapToObj(i -> checklist.get(i - 1))
                .filter(Task::mark)
                .toList();
        if (markedItems.isEmpty()) {
            outputStream.print(this.dialogues.warningMarkEmpty());
        } else {
            outputStream.print(this.dialogues.successMark(markedItems));
        }
        tempList = markedItems;
    }

    /**
     * Unmarks (mark as incomplete) tasks from the checklist by index, then list the unmarked tasks.
     *
     * @param indices the indices of the tasks to be unmarked
     */
    public final void unmarkTasks(int... indices) {
        if (checklist.isEmpty()) {
            outputStream.print(this.dialogues.warningListEmpty());
        }
        for (int i : indices) {
            if (i < 1 || i > checklist.size()) {
                outputStream.print(this.dialogues.errorIndex(i));
                return;
            }
        }
        List<Task> unmarkedItems = IntStream.of(indices)
                .mapToObj(i -> checklist.get(i - 1))
                .filter(Task::unmark)
                .toList();
        if (unmarkedItems.isEmpty()) {
            outputStream.print(this.dialogues.warningUnmarkEmpty());
        } else {
            outputStream.print(this.dialogues.successUnmark(unmarkedItems));
        }
        tempList = unmarkedItems;
    }

    /**
     * Starts the chatbot loop.
     * <p>
     * Marquee will listen from the input stream
     * and print to the output stream given in the constructor.
     */
    public void run() {
        isRunning = true;
        outputStream.print(this.dialogues.banner());
        outputStream.print(this.dialogues.greetings());
        while (isRunning) {
            String input;
            Command command;

            try {
                input = getInput();
            } catch (IOException e) {
                outputStream.print(this.dialogues.fatalErrorIoUnavailable());
                exit();
                continue;
            }

            try {
                command = commandFormatter.parseCommand(input);
            } catch (UnknownFlagException e) {
                if (e.getFlagName() == null) {
                    outputStream.print(this.dialogues.errorUnusedArgument(e.getCode().getName()));
                } else {
                    outputStream.print(this.dialogues.errorUnknownFlag(e.getFlagName(), e.getCode().getName()));
                }
                continue;
            } catch (DuplicateFlagException e) {
                outputStream.print(this.dialogues.errorDuplicateFlag(e.getFlagName()));
                continue;
            } catch (IllegalArgumentException e) {
                outputStream.print(this.dialogues.errorUnknownCommand(input));
                continue;
            }

            try {
                if (BaseCodes.EXIT.equals(command.getCode())) {
                    exit();
                } else if (BaseCodes.LOAD.equals(command.getCode())) {
                    loadChecklist();
                } else if (BaseCodes.SAVE.equals(command.getCode())) {
                    saveChecklist();
                } else if (BaseCodes.LIST.equals(command.getCode())) {
                    list();
                } else if (BaseCodes.FIND.equals(command.getCode())) {
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
                } else if (BaseCodes.TODO.equals(command.getCode())) {
                    if (!command.hasArgument()) {
                        outputStream.print(this.dialogues.errorTaskNameMissing());
                    } else {
                        addTasks(new TodoTask(command.getArgument()));
                    }
                } else if (BaseCodes.DEADLINE.equals(command.getCode())) {
                    if (!command.hasArgument()) {
                        outputStream.print(this.dialogues.errorTaskNameMissing());
                    } else if (!command.hasFlag("by")) {
                        outputStream.print(this.dialogues.errorDeadlineMissing());
                    } else {
                        addTasks(new DeadlineTask(
                                command.getArgument(),
                                DateTimeFormatter.parseDateTime(command.getFlag("by"))
                        ));
                    }
                } else if (BaseCodes.EVENT.equals(command.getCode())) {
                    if (!command.hasArgument()) {
                        outputStream.print(this.dialogues.errorTaskNameMissing());
                    } else if (!command.hasFlag("from")) {
                        outputStream.print(this.dialogues.errorStartTimeMissing());
                    } else if (!command.hasFlag("to")) {
                        outputStream.print(this.dialogues.errorEndTimeMissing());
                    } else {
                        try {
                            addTasks(new EventTask(
                                    command.getArgument(),
                                    DateTimeFormatter.parseDateTime(command.getFlag("from")),
                                    DateTimeFormatter.parseDateTime(command.getFlag("to"))
                            ));
                        } catch (IllegalArgumentException _) {
                            outputStream.print(this.dialogues.errorEventEndBeforeStart());
                        }
                    }
                } else if (BaseCodes.DELETE.equals(command.getCode())) {
                    deleteTasks(parseIntArray(command.getArgument()));
                } else if (BaseCodes.DELETE_ALL.equals(command.getCode())) {
                    deleteTasks(IntStream.rangeClosed(1, checklist.size()).toArray());
                } else if (BaseCodes.DELETE_MATCHING.equals(command.getCode())) {
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
                } else if (BaseCodes.MARK.equals(command.getCode())) {
                    markTasks(parseIntArray(command.getArgument()));
                } else if (BaseCodes.MARK_ALL.equals(command.getCode())) {
                    markTasks(IntStream.rangeClosed(1, checklist.size()).toArray());
                } else if (BaseCodes.MARK_MATCHING.equals(command.getCode())) {
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
                } else if (BaseCodes.UNMARK.equals(command.getCode())) {
                    unmarkTasks(parseIntArray(command.getArgument()));
                } else if (BaseCodes.UNMARK_ALL.equals(command.getCode())) {
                    unmarkTasks(IntStream.rangeClosed(1, checklist.size()).toArray());
                } else if (BaseCodes.UNMARK_MATCHING.equals(command.getCode())) {
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
                    outputStream.print(this.dialogues.errorUnsupportedCommand());
                }
            } catch (DateTimeParseException e) {
                outputStream.print(this.dialogues.errorDatetime(e.getParsedString()));
            } catch (NumberFormatException e) {
                outputStream.print(this.dialogues.errorNan(e.getMessage()));
            }
        }
    }

    public static void main(String[] args) throws ClassNotFoundException {
        // loads data classes
        Class.forName("marquee.command.BaseCodes");
        Class.forName("marquee.task.BaseTags");

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
                        : item -> !(item.getStart() == null || item.getStart().isBefore(start))
                )
                .filter(end == null
                        ? _ -> true
                        : item -> !(item.getEnd() == null || item.getEnd().isAfter(end))
                )
                .filter(search == null || search.isEmpty()
                        ? _ -> true
                        : item -> item.getDescription().contains(search))
                .toList();
    }
}
