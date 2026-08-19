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
        greet();
        exit();
    }

    public static String getInput() throws IOException {
        System.out.print("\n> ");
        return inputReader.readLine();
    }

    public static void greet() {
        System.out.print("\nHi! I'm Marquee.\nWhat will we do today?\n\n");
    }

    public static void bye() {
        System.out.print("\nSee you later :D\n");
    }
}
