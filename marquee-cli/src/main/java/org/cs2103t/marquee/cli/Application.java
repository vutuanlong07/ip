package org.cs2103t.marquee.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.cs2103t.marquee.cli.command.Codes;
import org.cs2103t.marquee.cli.command.Command;
import org.cs2103t.marquee.cli.command.CommandFormatter;
import org.cs2103t.marquee.cli.task.DeadlineTask;
import org.cs2103t.marquee.cli.task.EventTask;
import org.cs2103t.marquee.cli.task.TodoTask;
import org.cs2103t.marquee.core.DuplicateKeyException;
import org.cs2103t.marquee.core.Marquee;
import org.cs2103t.marquee.core.io.FileParseException;
import org.cs2103t.marquee.core.task.Task;
import org.cs2103t.marquee.core.time.DateTimeFormatter;

/**
 * Entry point for the CLI application.
 */
public class Application {
    private int[] parseIntArray(String input) throws NumberFormatException {
        return Arrays.stream(input.split("\\s+", -1))
                .mapToInt(str -> {
                    try {
                        return Integer.parseInt(str) - 1;
                    } catch (NumberFormatException _) {
                        throw new NumberFormatException(str);
                    }
                })
                .toArray();
    }

    private Path getLocalStoragePath() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            return Paths.get(System.getenv("LOCALAPPDATA"));
        } else if (os.contains("mac")) {
            return Paths.get(System.getProperty("user.home")).resolve("Library");
        } else {
            String dataHome = System.getenv("XDG_DATA_HOME");
            String userHome = System.getProperty("user.home");
            return Paths.get(dataHome != null && !dataHome.isEmpty() ? dataHome : userHome);
        }
    }

    void main() throws ClassNotFoundException {
        // loads data classes
        Class.forName("org.cs2103t.marquee.cli.command.Codes");
        Class.forName("org.cs2103t.marquee.cli.task.TaskTags");

        Path saveFile = getLocalStoragePath().resolve("Marquee", "checklist.csv");
        Dialogues dialogues = new Dialogues();
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        CommandFormatter commandFormatter = new CommandFormatter("/", "/");
        Marquee marquee = new Marquee();
        Map<String, Class<?>> classMap = Stream.of(TodoTask.class, EventTask.class, DeadlineTask.class)
                .collect(Collectors.toUnmodifiableMap(Class::getSimpleName, c -> c));
        marquee.setClassNameEncoding(Class::getSimpleName, classMap::get);

        dialogues.banner();
        dialogues.infoSaveFilePath(saveFile);
        try {
            if (marquee.load(saveFile)) {
                dialogues.successLoad();
            } else {
                dialogues.warningSaveIncomplete();
            }
        } catch (NoSuchFileException _) {
            dialogues.warningSaveNotFound();
        } catch (IOException e) {
            dialogues.errorSaveUnavailable(e.getMessage());
        } catch (FileParseException e) {
            dialogues.errorSaveWrongFormat();
        } catch (IllegalArgumentException e) {
            dialogues.errorSaveCorrupted();
        }
        dialogues.greetings();
        boolean isRunning = true;
        while (isRunning) {
            String input;
            Command command;

            try {
                dialogues.prompt();
                input = inputReader.readLine();
            } catch (IOException e) {
                dialogues.errorIoUnavailable();
                try {
                    marquee.save(saveFile);
                    dialogues.successSave();
                } catch (IOException f) {
                    dialogues.errorSaveUnavailable(f.getMessage());
                }
                isRunning = false;
                dialogues.successExit();
                continue;
            }

            try {
                command = commandFormatter.parseCommand(input);
            } catch (NoSuchElementException e) {
                dialogues.errorUnknownFlag(e.getMessage());
                continue;
            } catch (DuplicateKeyException e) {
                dialogues.errorDuplicateFlag(e.getKey());
                continue;
            } catch (IllegalArgumentException e) {
                dialogues.errorUnknownCommand(input);
                continue;
            }

            if (Codes.EXIT.equals(command.getCode())) {
                try {
                    marquee.save(saveFile);
                    dialogues.successSave();
                } catch (IOException e) {
                    dialogues.errorSaveUnavailable(e.getMessage());
                }
                isRunning = false;
                dialogues.successExit();
            } else if (Codes.LOAD.equals(command.getCode())) {
                try {
                    marquee.load(saveFile);
                    dialogues.successLoad();
                } catch (NoSuchFileException _) {
                    dialogues.warningSaveNotFound();
                } catch (IOException e) {
                    dialogues.errorSaveUnavailable(e.getMessage());
                } catch (FileParseException e) {
                    dialogues.errorSaveWrongFormat();
                } catch (IllegalArgumentException e) {
                    dialogues.errorSaveCorrupted();
                }
            } else if (Codes.SAVE.equals(command.getCode())) {
                try {
                    marquee.save(saveFile);
                    dialogues.successSave();
                } catch (IOException e) {
                    dialogues.errorSaveUnavailable(e.getMessage());
                }
            } else if (Codes.LIST.equals(command.getCode())) {
                dialogues.successList(marquee.list());
            } else if (Codes.TODO.equals(command.getCode())) {
                if (!command.hasArgument()) {
                    dialogues.errorTaskNameMissing();
                } else {
                    Task newTask = new TodoTask(command.getArgument());
                    marquee.addTasks(newTask);
                    dialogues.successAdd(List.of(newTask));
                }
            } else if (Codes.DEADLINE.equals(command.getCode())) {
                if (!command.hasArgument()) {
                    dialogues.errorTaskNameMissing();
                } else if (!command.hasFlag("by")) {
                    dialogues.errorDeadlineMissing();
                } else {
                    try {
                        Task newTask = new DeadlineTask(
                                command.getArgument(),
                                DateTimeFormatter.parseDateTime(command.getFlag("by"))
                        );
                        marquee.addTasks(newTask);
                        dialogues.successAdd(List.of(newTask));
                    } catch (DateTimeParseException e) {
                        dialogues.errorDatetime(e.getParsedString());
                    }
                }
            } else if (Codes.EVENT.equals(command.getCode())) {
                if (!command.hasArgument()) {
                    dialogues.errorTaskNameMissing();
                } else if (!command.hasFlag("from")) {
                    dialogues.errorStartTimeMissing();
                } else if (!command.hasFlag("to")) {
                    dialogues.errorEndTimeMissing();
                } else {
                    try {
                        Task newTask = new EventTask(
                                command.getArgument(),
                                DateTimeFormatter.parseDateTime(command.getFlag("from")),
                                DateTimeFormatter.parseDateTime(command.getFlag("to"))
                        );
                        marquee.addTasks(newTask);
                        dialogues.successAdd(List.of(newTask));
                    } catch (DateTimeParseException e) {
                        dialogues.errorDatetime(e.getParsedString());
                    } catch (IllegalArgumentException _) {
                        dialogues.errorEventEndBeforeStart();
                    }
                }
            } else if (Codes.DELETE_ALL.equals(command.getCode())) {
                dialogues.successDelete(marquee.deleteAllTasks(command.hasFlag("chain")));
            } else if (Codes.MARK_ALL.equals(command.getCode())) {
                dialogues.successMark(marquee.markAllTasks(command.hasFlag("chain")));
            } else if (Codes.UNMARK_ALL.equals(command.getCode())) {
                dialogues.successUnmark(marquee.unmarkAllTasks(command.hasFlag("chain")));
            } else if (Codes.DELETE.equals(command.getCode())) {
                try {
                    dialogues.successDelete(marquee.deleteTasks(
                            command.hasFlag("chain"),
                            parseIntArray(command.getArgument())
                    ));
                } catch (NumberFormatException e) {
                    dialogues.errorNan(e.getMessage());
                } catch (IndexOutOfBoundsException e) {
                    dialogues.errorIndex(e.getMessage());
                }
            } else if (Codes.MARK.equals(command.getCode())) {
                try {
                    dialogues.successMark(marquee.markTasks(
                            command.hasFlag("chain"),
                            parseIntArray(command.getArgument())
                    ));
                } catch (NumberFormatException e) {
                    dialogues.errorNan(e.getMessage());
                } catch (IndexOutOfBoundsException e) {
                    dialogues.errorIndex(e.getMessage());
                }
            } else if (Codes.UNMARK.equals(command.getCode())) {
                try {
                    dialogues.successUnmark(marquee.unmarkTasks(
                            command.hasFlag("chain"),
                            parseIntArray(command.getArgument())
                    ));
                } catch (NumberFormatException e) {
                    dialogues.errorNan(e.getMessage());
                } catch (IndexOutOfBoundsException e) {
                    dialogues.errorIndex(e.getMessage());
                }
            } else if (Codes.FIND.equals(command.getCode())) {
                try {
                    if (command.hasFlag("chain")) {
                        dialogues.successFind(marquee.find(
                                command.getArgument(),
                                command.hasFlag("from")
                                        ? DateTimeFormatter.parseDateTime(command.getFlag("from"))
                                        : null,
                                command.hasFlag("to")
                                        ? DateTimeFormatter.parseDateTime(command.getFlag("to"))
                                        : null,
                                command.hasFlag("completed") != command.hasFlag("incomplete")
                                        ? command.hasFlag("completed")
                                        : null,
                                null,
                                false
                        ));
                    }
                } catch (DateTimeParseException e) {
                    dialogues.errorDatetime(e.getParsedString());
                }
            } else if (Codes.DELETE_MATCHING.equals(command.getCode())) {
                try {
                    if (command.hasFlag("chain")) {
                        marquee.find(
                                command.getArgument(),
                                command.hasFlag("from")
                                        ? DateTimeFormatter.parseDateTime(command.getFlag("from"))
                                        : null,
                                command.hasFlag("to")
                                        ? DateTimeFormatter.parseDateTime(command.getFlag("to"))
                                        : null,
                                command.hasFlag("completed") != command.hasFlag("incomplete")
                                        ? command.hasFlag("completed")
                                        : null,
                                null,
                                false
                        );
                    }
                    dialogues.successDelete(marquee.deleteAllTasks(command.hasFlag("chain")));
                } catch (DateTimeParseException e) {
                    dialogues.errorDatetime(e.getParsedString());
                }
            } else if (Codes.MARK_MATCHING.equals(command.getCode())) {
                try {
                    if (command.hasFlag("chain")) {
                        marquee.find(
                                command.getArgument(),
                                command.hasFlag("from")
                                        ? DateTimeFormatter.parseDateTime(command.getFlag("from"))
                                        : null,
                                command.hasFlag("to")
                                        ? DateTimeFormatter.parseDateTime(command.getFlag("to"))
                                        : null,
                                command.hasFlag("completed") != command.hasFlag("incomplete")
                                        ? command.hasFlag("completed")
                                        : null,
                                null,
                                false
                        );
                    }
                    dialogues.successMark(marquee.markAllTasks(command.hasFlag("chain")));
                } catch (DateTimeParseException e) {
                    dialogues.errorDatetime(e.getParsedString());
                }
            } else if (Codes.UNMARK_MATCHING.equals(command.getCode())) {
                try {
                    if (command.hasFlag("chain")) {
                        marquee.find(
                                command.getArgument(),
                                command.hasFlag("from")
                                        ? DateTimeFormatter.parseDateTime(command.getFlag("from"))
                                        : null,
                                command.hasFlag("to")
                                        ? DateTimeFormatter.parseDateTime(command.getFlag("to"))
                                        : null,
                                command.hasFlag("completed") != command.hasFlag("incomplete")
                                        ? command.hasFlag("completed")
                                        : null,
                                null,
                                false
                        );
                    }
                    dialogues.successUnmark(marquee.unmarkAllTasks(command.hasFlag("chain")));
                } catch (DateTimeParseException e) {
                    dialogues.errorDatetime(e.getParsedString());
                }
            } else {
                dialogues.errorUnsupportedCommand(command.getCode());
            }
        }
    }
}
