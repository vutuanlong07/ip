import marquee.Marquee;

void main() throws ClassNotFoundException {
    // loads data classes
    Class.forName("marquee.command.BaseCodes");
    Class.forName("marquee.task.BaseTags");

    // get app directory
    Path userhome;
    String os = System.getProperty("os.name").toLowerCase();
    if (os.contains("windows")) {
        userhome = Paths.get(System.getenv("LOCALAPPDATA"));
    } else if (os.contains("mac")) {
        userhome = Paths.get(System.getProperty("user.home")).resolve("Library");
    } else {
        String dataHome = System.getenv("XDG_DATA_HOME");
        String userHome = System.getProperty("user.home");
        userhome = Paths.get(dataHome != null && !dataHome.isEmpty() ? dataHome : userHome);
    }
    Path saveDirectory = userhome.resolve("Marquee");

    Marquee chatbot = new Marquee(System.in, System.out, saveDirectory.resolve("checklist.csv"));
    chatbot.run();
}
