import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Marquee {
    private static final BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

    private static final class TaskItem {
        private boolean marked;
        private String content;

        private TaskItem(String content) {
            this.marked = false;
            this.content = content;
        }

        public boolean marked() {
            return marked;
        }

        public String content() {
            return content;
        }

        public boolean mark() {
            return this.marked != (this.marked = true);
        }

        public boolean unmark() {
            return this.marked != (this.marked = false);
        }

        @Override
        public String toString() {
            return (marked? "[x] " : "[ ] ") + content;
        }
    }

    public static void main(String[] args) throws IOException {
        String banner = "____  ___\n" +
                "|   \\/   |  _____   ____   _____   _   _   ____   ____\n" +
                "| |\\  /| | /  _  /  | .-` /  _  \\ | | | | / ___) / ___)\n" +
                "| | || | | | |_| |  | |   | |_| | | |_| | | ___) | ___)\n" +
                "|_| |/ |_| \\____/\\_ |_|   \\___  | \\_____\\ \\____) \\____)\n" +
                "                              | |\n" +
                "                              | |\n" +
                "                               \\|";
        List<TaskItem> checklist = new ArrayList<>();

        System.out.println(banner);
        System.out.println("Hi! I'm Marquee.\nWhat will we do today?");
        while (true) {
            String rawInput = getInput();
            String[] input = rawInput.split(" ", -1);
            switch (input[0]) {
                case "bye":
                    System.out.println("See you later :D");
                    return;
                case "list":
                    if (checklist.isEmpty()) {
                        System.out.println("No items yet :(");
                    } else {
                        System.out.println("Current items in your list:");
                        System.out.print(checklistToString(checklist));
                    }
                    break;
                case "mark":
                    if (input.length < 2) {
                        System.out.println("Indicate an index to mark -_-\"");
                    } else {
                        List<TaskItem> modified = new ArrayList<>();
                        for (int i = 1; i < input.length; i++) {
                            try {
                                int idx = Integer.parseInt(input[i]) - 1;
                                if (checklist.get(idx).mark()) {
                                    modified.add(checklist.get(idx));
                                }
                            } catch (NumberFormatException e) {
                                System.out.printf("'%s' is not a number :O\n", input[i]);
                            } catch (IndexOutOfBoundsException e) {
                                int idx = Integer.parseInt(input[i]) - 1;
                                if (idx < 0)
                                    System.out.printf("%s is not a valid index :O\n", input[i]);
                                else
                                    System.out.printf("%s is too large! Your checklist only has %d items >_<\n", input[i], checklist.size());
                            }
                        }

                        if (modified.isEmpty()) {
                            System.out.println("No items were modified -.- .(zzz)");
                        } else {
                            System.out.println("These items were marked:");
                            System.out.print(checklistToString(modified));
                        }
                    }
                    break;
                default:
                    checklist.add(new TaskItem(rawInput));
                    System.out.printf("added: %s\n", rawInput);
                    break;
            }
        }
    }

    public static String getInput() throws IOException {
        System.out.print("\n> ");
        return inputReader.readLine();
    }

    private static String checklistToString(List<TaskItem> list) {
        StringBuilder builder = IntStream.range(0, list.size())
                .mapToObj(idx -> String.format("%d. %s\n", idx + 1, list.get(idx)))
                .collect(
                        StringBuilder::new,
                        StringBuilder::append,
                        StringBuilder::append
                );
        return builder.toString();
    }
}
