import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Marquee {
    private static final BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

    private static final class MemoryItem {
        private boolean marked;
        private String content;

        private MemoryItem(String content) {
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
        List<String> memory = new ArrayList<>();

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
                    if (memory.isEmpty()) {
                        System.out.println("No items yet :(");
                    } else {
                        StringBuilder builder = IntStream.range(0, memory.size())
                                .mapToObj(idx -> String.format("%d. %s\n", idx + 1, memory.get(idx)))
                                .collect(
                                        StringBuilder::new,
                                        StringBuilder::append,
                                        StringBuilder::append
                                );
                        builder.deleteCharAt(builder.length() - 1);
                        System.out.println(builder.toString());
                    }
                    break;
                default:
                    memory.add(rawInput);
                    System.out.printf("added: %s\n", rawInput);
                    break;
            }
        }
    }

    public static String getInput() throws IOException {
        System.out.print("\n> ");
        return inputReader.readLine();
    }
}
