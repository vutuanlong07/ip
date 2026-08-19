import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Marquee {
    private static final BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) throws IOException {
        String banner = "____  ___\n" +
                "|   \\/   |  _____   ____   _____   _   _   ____   ____\n" +
                "| |\\  /| | /  _  /  | .-` /  _  \\ | | | | / ___) / ___)\n" +
                "| | || | | | |_| |  | |   | |_| | | |_| | | ___) | ___)\n" +
                "|_| |/ |_| \\____/\\_ |_|   \\___  | \\_____\\ \\____) \\____)\n" +
                "                              | |\n" +
                "                              | |\n" +
                "                               \\|";
        System.out.println(banner);
        System.out.println("Hi! I'm Marquee.\nWhat will we do today?");
        while (true) {
            String rawInput = getInput();
            String[] input = rawInput.split(" ", -1);
            switch (input[0]) {
                case "bye":
                    System.out.println("See you later :D");
                    return;
                default:
                    System.out.printf("%s\n", rawInput);
                    break;
            }
        }
    }

    public static String getInput() throws IOException {
        System.out.print("\n> ");
        return inputReader.readLine();
    }
}
