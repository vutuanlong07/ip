import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Marquee {
    private static final String flagDelimiter = "\\/";
    private static final BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
    private static final Pattern commandPattern = Pattern.compile("^\\s*(\\w+)");
    private static final Pattern contentPattern = Pattern.compile("\\s*(\\S.*?|)\\s*(?=<DELIM>|\\z)".replace("<DELIM>", flagDelimiter));
    private static final Pattern flagPattern = Pattern.compile("<DELIM>(\\w+)".replace("<DELIM>", flagDelimiter));

    public static void main(String[] args) throws IOException {
        List<TaskItem> checklist = new ArrayList<>();

        System.out.println(Dialogues.Banner);
        System.out.println(Dialogues.Greetings);
        while (true) {
            String rawInput = getInput();
            Matcher commandMatcher = commandPattern.matcher(rawInput);
            Matcher contentMatcher = contentPattern.matcher(rawInput);
            Matcher flagMatcher = flagPattern.matcher(rawInput);

            String command = "";
            String argument = "";
            Map<String, String> flags = new HashMap<>();
            if (commandMatcher.find()) {
                command = commandMatcher.group(1);

                if (contentMatcher.find(commandMatcher.end())) {
                    argument = contentMatcher.group(1);

                    while (flagMatcher.find(contentMatcher.end())) {
                        if (contentMatcher.find(flagMatcher.end())) {
                            flags.put(flagMatcher.group(1), contentMatcher.group(1));
                        }
                    }
                }
            }

            switch (command) {
                case "bye":
                    System.out.println(Dialogues.Goodbye);
                    return;
                case "list":
                    if (checklist.isEmpty()) {
                        System.out.println(Dialogues.DisplayListEmpty);
                    } else {
                        System.out.println(Dialogues.DisplayListSuccessful);
                        System.out.print(checklistToString(checklist));
                    }
                    break;
                case "todo":
                    if (argument.isEmpty()) {
                        System.out.println(Dialogues.TaskItemMissingName);
                    } else {
                        ToDoItem toDoItem = new ToDoItem(argument);
                        checklist.add(toDoItem);
                        System.out.printf(Dialogues.AddTaskSuccessful + "\n", toDoItem, checklist.size());
                    }
                    break;
                case "deadline":
                    if (argument.isEmpty()) {
                        System.out.println(Dialogues.TaskItemMissingName);
                    } else if (!flags.containsKey("by")) {
                        System.out.println(Dialogues.DeadlineMissing);
                    } else {
                        DeadlineItem deadlineItem = new DeadlineItem(argument, flags.get("by"));
                        checklist.add(deadlineItem);
                        System.out.printf(Dialogues.AddTaskSuccessful + "\n", deadlineItem, checklist.size());
                    }
                    break;
                case "event":
                    if (argument.isEmpty()) {
                        System.out.println(Dialogues.TaskItemMissingName);
                    } else if (!flags.containsKey("from")) {
                        System.out.println(Dialogues.EventMissingStart);
                    } else if (!flags.containsKey("to")) {
                        System.out.println(Dialogues.EventMissingEnd);
                    } else {
                        EventItem eventItem = new EventItem(argument, flags.get("from"), flags.get("to"));
                        checklist.add(eventItem);
                        System.out.printf(Dialogues.AddTaskSuccessful + "\n", eventItem, checklist.size());
                    }
                    break;
                case "delete":
                    if (argument.isEmpty()) {
                        System.out.println(Dialogues.DeleteMissingArguments);
                    } else {
                        List<TaskItem> modified = parseRawIndexArray(argument, checklist.size())
                                .mapToObj(checklist::remove)
                                .toList();
                        if (modified.isEmpty()) {
                            System.out.println(Dialogues.DeleteNoChange);
                        } else {
                            System.out.printf(Dialogues.DeleteSuccessful + "\n", checklistToStringNoIndex(modified), checklist.size());
                        }
                    }
                    break;
                case "mark":
                    if (argument.isEmpty()) {
                        System.out.println(Dialogues.MarkMissingArguments);
                    } else {
                        List<TaskItem> modified = parseRawIndexArray(argument, checklist.size())
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
                    break;
                case "unmark":
                    if (argument.isEmpty()) {
                        System.out.println(Dialogues.UnmarkMissingArguments);
                    } else {
                        List<TaskItem> modified = parseRawIndexArray(argument, checklist.size())
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
                    break;
                default:
                    if (!command.isEmpty())
                        System.out.printf(Dialogues.UnknownCommand + "\n", command);
                    break;
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
