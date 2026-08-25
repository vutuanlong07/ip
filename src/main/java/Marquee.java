import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.IntStream;

public class Marquee {
    private static final String flagDelimiter = "\\/";
    private static final BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) throws IOException {
        List<TaskItem> checklist = new ArrayList<>();

        System.out.println(Dialogues.Banner);
        System.out.println(Dialogues.Greetings);
        while (true) {
            String rawInput = getInput();
            Command command = Command.fromInput(rawInput);
            switch (command.code()) {
                case "bye" -> {
                    System.out.println(Dialogues.Goodbye);
                    return;
                }
                case "list" -> {
                    if (checklist.isEmpty()) {
                        System.out.println(Dialogues.DisplayListEmpty);
                    } else {
                        System.out.println(Dialogues.DisplayListSuccessful);
                        System.out.print(checklistToString(checklist));
                    }
                }
                case "todo" -> {
                    if (command.parameter().isEmpty()) {
                        System.out.println(Dialogues.TaskItemMissingName);
                    } else {
                        TodoItem toDoItem = new TodoItem(command.parameter());
                        checklist.add(toDoItem);
                        System.out.printf(Dialogues.AddTaskSuccessful + "\n", toDoItem, checklist.size());
                    }
                }
                case "deadline" -> {
                    if (command.parameter().isEmpty()) {
                        System.out.println(Dialogues.TaskItemMissingName);
                    } else if (!command.flags().containsKey("by")) {
                        System.out.println(Dialogues.DeadlineMissing);
                    } else {
                        DeadlineItem deadlineItem = new DeadlineItem(command.parameter(), command.flags().get("by"));
                        checklist.add(deadlineItem);
                        System.out.printf(Dialogues.AddTaskSuccessful + "\n", deadlineItem, checklist.size());
                    }
                }
                case "event" -> {
                    if (command.parameter().isEmpty()) {
                        System.out.println(Dialogues.TaskItemMissingName);
                    } else if (!command.flags().containsKey("from")) {
                        System.out.println(Dialogues.EventMissingStart);
                    } else if (!command.flags().containsKey("to")) {
                        System.out.println(Dialogues.EventMissingEnd);
                    } else {
                        EventItem eventItem = new EventItem(command.parameter(), command.flags().get("from"), command.flags().get("to"));
                        checklist.add(eventItem);
                        System.out.printf(Dialogues.AddTaskSuccessful + "\n", eventItem, checklist.size());
                    }
                }
                case "delete" -> {
                    if (command.parameter().isEmpty()) {
                        System.out.println(Dialogues.DeleteMissingArguments);
                    } else {
                        List<TaskItem> modified = parseRawIndexArray(command.parameter(), checklist.size())
                                .mapToObj(checklist::remove)
                                .toList();
                        if (modified.isEmpty()) {
                            System.out.println(Dialogues.DeleteNoChange);
                        } else {
                            System.out.printf(Dialogues.DeleteSuccessful + "\n", checklistToStringNoIndex(modified), checklist.size());
                        }
                    }
                }
                case "mark" -> {
                    if (command.parameter().isEmpty()) {
                        System.out.println(Dialogues.MarkMissingArguments);
                    } else {
                        List<TaskItem> modified = parseRawIndexArray(command.parameter(), checklist.size())
                                .mapToObj(checklist::get)
                                .filter(TaskItem::mark)
                                .toList();
                        if (modified.isEmpty()) {
                            System.out.println(Dialogues.MarkNoChange);
                        } else {
                            System.out.println(Dialogues.MarkSuccessful);
                            System.out.print(checklistToStringNoIndex(modified));
                        }
                    }
                }
                case "unmark" -> {
                    if (command.parameter().isEmpty()) {
                        System.out.println(Dialogues.UnmarkMissingArguments);
                    } else {
                        List<TaskItem> modified = parseRawIndexArray(command.parameter(), checklist.size())
                                .mapToObj(checklist::get)
                                .filter(TaskItem::unmark)
                                .toList();
                        if (modified.isEmpty()) {
                            System.out.println(Dialogues.UnmarkNoChange);
                        } else {
                            System.out.println(Dialogues.UnmarkSuccessful);
                            System.out.print(checklistToStringNoIndex(modified));
                        }
                    }
                }
                default -> {
                    if (!command.code().isEmpty())
                        System.out.printf(Dialogues.UnknownCommand + "\n", command);
                }
            }
        }
    }

    public static String getInput() throws IOException {
        System.out.print("\n> ");
        return inputReader.readLine();
    }

    public static IntStream parseRawIndexArray(String indexArray, int capacity) {
        List<Integer> indices = new ArrayList<>();
        return Arrays.stream(indexArray.split(" ", -1))
                .mapToInt(idxStr -> {
                    if (!idxStr.isEmpty()) try {
                        int idx = Integer.parseInt(idxStr) - 1;
                        if (capacity == 0)
                            System.out.println(Dialogues.IndexListEmpty);
                        else if (idx < 0)
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

    public static String checklistToString(List<TaskItem> list) {
        StringBuilder builder = IntStream.range(0, list.size())
                .mapToObj(idx -> String.format("%d. %s\n", idx + 1, list.get(idx)))
                .collect(
                        StringBuilder::new,
                        StringBuilder::append,
                        StringBuilder::append
                );
        return builder.toString();
    }

    public static String checklistToStringNoIndex(List<TaskItem> list) {
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
