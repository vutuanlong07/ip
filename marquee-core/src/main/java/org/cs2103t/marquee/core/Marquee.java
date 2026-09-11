package org.cs2103t.marquee.core;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.cs2103t.marquee.core.io.CsvTable;
import org.cs2103t.marquee.core.task.Task;

/**
 * Main class for the standalone chatbot Marquee.
 */
public class Marquee {
    private final Path savePath;
    private final List<Task> checklist = new ArrayList<>();
    private List<Task> lastResult = checklist;

    /**
     * Instantiates an instance of Marquee and attempts to load its checklist from {@code savePath}.
     * <p>
     * If loading fails, starts with an empty checklist.
     *
     * @param savePath the path to the CSV file Marquee will save its checklist to
     */
    public Marquee(Path savePath) {
        this.savePath = savePath;
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
        CsvTable csv = new CsvTable(columnNames);
        list.forEach(task -> csv.add(csv.createRecord(task.toValueMap())));
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
        csv.getValues().forEach(record -> list.add(Task.fromValueMap(record.getAllFields())));
        return list;
    }

    /**
     * Attempts to load the checklist from the save file.
     * <p>
     * If the operation fails, no change is made to the checklist.
     */
    public final void load() throws NoSuchFileException, IOException, ParseException, IllegalArgumentException {
        List<Task> newChecklist = csvToList(CsvTable.readFile(savePath, ";"));
        checklist.clear();
        checklist.addAll(newChecklist);
    }

    /**
     * Saves the checklist into the save file.
     * <p>
     * If the operation fails, the original save file will not be changed.
     */
    public final void save() throws IOException {
        CsvTable.writeFile(savePath, listToCsv(checklist));
    }

    /**
     * Returns the tasks in the checklist.
     * <p>
     * If there are none, output a different message clarifying that the checklist is empty.
     *
     * @return an unmodifiable view of the current checklist
     */
    public final List<Task> list() {
        List<Task> tempList = Collections.unmodifiableList(checklist);
        return lastResult = tempList;
    }

    /**
     * Searches for tasks in the checklist satisfying the search conditions.
     * <p>
     * If description is empty or {@code null}, and all other parameters are {@code null}, no result is returned.
     *
     * @param description match tasks containing this substring in its description
     * @param start match tasks starting after this time
     * @param end match tasks ending before this time
     * @param isMarked match tasks with this mark status
     * @return the tasks matching the filters
     */
    public final List<Task> find(String description, LocalDateTime start, LocalDateTime end, Boolean isMarked) {
        List<Task> matchingItems = (description == null || description.isEmpty())
                && start == null && end == null && isMarked == null
                ? List.of()
                : checklist.stream()
                .filter(isMarked == null
                        ? _ -> true
                        : task -> task.isMarked() == isMarked
                )
                .filter(start == null
                        ? _ -> true
                        : task -> task.startsAfter(start)
                )
                .filter(end == null
                        ? _ -> true
                        : task -> task.endsBefore(end)
                )
                .filter(description == null || description.isEmpty()
                        ? _ -> true
                        : task -> task.getDescription().contains(description)
                )
                .toList();
        return lastResult = matchingItems;
    }

    /**
     * Adds tasks to the checklist, then returns the added tasks.
     *
     * @param tasks the tasks to be added to the checklist
     * @return the added tasks
     */
    public final List<Task> addTasks(Task... tasks) {
        List<Task> newTasks = List.of(tasks);
        checklist.addAll(newTasks);
        return lastResult = newTasks;
    }

    /**
     * Deletes tasks from the last search/list result by index, then returns the modified tasks.
     * <p>
     * This operation is atomic - if an exception is thrown, no task will be modified.
     *
     * @param indices the indices of the tasks to be deleted
     * @return the deleted tasks
     * @throws IndexOutOfBoundsException if an index is out of the last task list's bounds
     */
    public final List<Task> deleteTasks(int... indices) throws IndexOutOfBoundsException {
        return lastResult = IntStream.of(indices)
                .peek(i -> {
                    if (i < 0 || i >= lastResult.size()) {
                        throw new IndexOutOfBoundsException(i);
                    }
                })
                .mapToObj(lastResult::get)
                .peek(checklist::remove)
                .toList();
    }

    /**
     * Deleted all tasks from the last search/list result, then returns the modified tasks.
     *
     * @return the deleted tasks
     */
    public final List<Task> deleteAllTasks() {
        return lastResult = lastResult.stream()
                .peek(checklist::remove)
                .toList();
    }

    /**
     * Marks tasks from the last search/list result by index, then returns the modified tasks.
     * <p>
     * This operation is atomic - if an exception is thrown, no task will be modified.
     *
     * @param indices the indices of the tasks to be marked
     * @return the marked tasks
     * @throws IndexOutOfBoundsException if an index is out of the last task list's bounds
     */
    public final List<Task> markTasks(int... indices) throws IndexOutOfBoundsException {
        return lastResult = IntStream.of(indices)
                .peek(i -> {
                    if (i < 0 || i >= lastResult.size()) {
                        throw new IndexOutOfBoundsException(i);
                    }
                })
                .mapToObj(lastResult::get)
                .peek(Task::mark)
                .toList();
    }

    /**
     * Marks all tasks from the last search/list result, then returns the modified tasks.
     *
     * @return the marked tasks
     */
    public final List<Task> markAllTasks() {
        return lastResult = lastResult.stream()
                .peek(Task::mark)
                .toList();
    }

    /**
     * Unmarks tasks from the last search/list result by index, then returns the modified tasks.
     * <p>
     * This operation is atomic - if an exception is thrown, no task will be modified.
     *
     * @param indices the indices of the tasks to be unmarked
     * @return the unmarked tasks
     * @throws IndexOutOfBoundsException if an index is out of the last task list's bounds
     */
    public final List<Task> unmarkTasks(int... indices) throws IndexOutOfBoundsException {
        return lastResult = IntStream.of(indices)
                .peek(i -> {
                    if (i < 0 || i >= lastResult.size()) {
                        throw new IndexOutOfBoundsException(i);
                    }
                })
                .mapToObj(lastResult::get)
                .peek(Task::unmark)
                .toList();
    }

    /**
     * Unmarks all tasks from the last search/list result, then returns the modified tasks.
     *
     * @return the unmarked tasks
     */
    public final List<Task> unmarkAllTasks() {
        return lastResult = lastResult.stream()
                .peek(Task::unmark)
                .toList();
    }
}
