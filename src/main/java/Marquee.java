import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        String banner = "____  ___\n" +
                "|   \\/   |  _____   ____   _____   _   _   ____   ____\n" +
                "| |\\  /| | / .__ /  | .-` / ._. \\ | | | | / ___) / ___)\n" +
                "| | || | | | |_/ |  | |   | |_| | | |_| | | ___) | ___)\n" +
                "|_| |/ |_| \\____/\\_ |_|   \\___  | \\_____\\ \\____) \\____)\n" +
                "                              | |\n" +
                "                              | |\n" +
                "                               \\|";
        List<TaskItem> checklist = new ArrayList<>();

        System.out.println(banner);
        System.out.println("Hi! I'm Marquee \\(>e<)/\nWhat will we do today? xD");
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
                    System.out.println("See you later :3");
                    return;
                case "list":
                    if (checklist.isEmpty()) {
                        System.out.println("No items yet -.- .·(ᶻzZ)");
                    } else {
                        System.out.println("Current items in your list:");
                        System.out.print(checklistToString(checklist));
                    }
                    break;
                case "todo":
                    ToDoItem toDoItem = new ToDoItem(argument);
                    checklist.add(toDoItem);
                    System.out.println("Added task:");
                    System.out.println("  " + toDoItem);
                    System.out.println("to the list (^_-☆ >c");
                    System.out.println("Currently have " + checklist.size() + " items in your checklist");
                    break;
                case "deadline":
                    if (!flags.containsKey("by")) {
                        System.out.println("Missing deadline for task -_-\"");
                    } else {
                        DeadlineItem deadlineItem = new DeadlineItem(argument, flags.get("by"));
                        checklist.add(deadlineItem);
                        System.out.println("Added task:");
                        System.out.println("  " + deadlineItem);
                        System.out.println("to the list (^_-☆ >c");
                        System.out.println("Currently have " + checklist.size() + " items in your checklist");
                    }
                    break;
                case "event":
                    if (!flags.containsKey("from")) {
                        System.out.println("Missing start time for event -_-\"");
                    } else if (!flags.containsKey("to")) {
                        System.out.println("Missing end time for event -_-\"");
                    } else {
                        EventItem eventItem = new EventItem(argument, flags.get("from"), flags.get("to"));
                        checklist.add(eventItem);
                        System.out.println("Added event:");
                        System.out.println("  " + eventItem);
                        System.out.println("to the list (^_-☆ >c");
                        System.out.println("Currently have " + checklist.size() + " items in your checklist");
                    }
                    break;
                case "mark":
                    if (argument.isEmpty()) {
                        System.out.println("Indicate an index to mark -_-\"");
                    } else {
                        List<TaskItem> modified = new ArrayList<>();
                        for (String idxStr : argument.split(" ", -1)) {
                            if (idxStr.isEmpty()) continue;
                            try {
                                int idx = Integer.parseInt(idxStr) - 1;
                                try {
                                    if (checklist.get(idx).mark()) {
                                        modified.add(checklist.get(idx));
                                    }
                                } catch (IndexOutOfBoundsException e) {
                                    if (idx < 0)
                                        System.out.printf("%d is not a valid index >_<\n", idx);
                                    else
                                        System.out.printf("%d is too large! Your checklist only has %d items >_<\n", idx, checklist.size());
                                }
                            } catch (NumberFormatException e) {
                                System.out.printf("'%s' is not a number :O\n", idxStr);
                            }
                        }

                        if (modified.isEmpty()) {
                            System.out.println("No items were modified (= ~ =)");
                        } else {
                            System.out.println("These items were marked:");
                            System.out.print(checklistToStringNoIndex(modified));
                        }
                    }
                    break;
                case "unmark":
                    if (argument.isEmpty()) {
                        System.out.println("Indicate an index to unmark -_-\"");
                    } else {
                        List<TaskItem> modified = new ArrayList<>();
                        for (String idxStr : argument.split(" ", -1)) {
                            if (idxStr.isEmpty()) continue;
                            try {
                                int idx = Integer.parseInt(idxStr) - 1;
                                try {
                                    if (checklist.get(idx).unmark()) {
                                        modified.add(checklist.get(idx));
                                    }
                                } catch (IndexOutOfBoundsException e) {
                                    if (idx < 0)
                                        System.out.printf("%d is not a valid index >_<\n", idx);
                                    else
                                        System.out.printf("%d is too large! Your checklist only has %d items >_<\n", idx, checklist.size());
                                }
                            } catch (NumberFormatException e) {
                                System.out.printf("'%s' is not a number :O\n", idxStr);
                            }
                        }

                        if (modified.isEmpty()) {
                            System.out.println("No items were modified (= ~ =)");
                        } else {
                            System.out.println("These items were unmarked \uD83D\uDC4D:");
                            System.out.print(checklistToStringNoIndex(modified));
                        }
                    }
                    break;
                default:
                    if (!command.isEmpty())
                        System.out.printf("unknown command: %s\n", command);
                    break;
            }
        }
    }

    public static String getInput() throws IOException {
        System.out.print("\n> ");
        return inputReader.readLine();
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
