package org.cs2103t.marquee.gui.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.cs2103t.marquee.core.Marquee;
import org.cs2103t.marquee.core.io.FileParseException;
import org.cs2103t.marquee.core.task.Task;
import org.cs2103t.marquee.core.task.TaskTag;
import org.cs2103t.marquee.gui.MainApplication;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Controller class for the main menu.
 */
public class MainMenu extends VBox {
    private final Marquee marquee;
    private final ObjectProperty<Path> currentFile = new SimpleObjectProperty<>(this, "currentFile");

    private Stage stage;
    @FXML
    private SplitPane content;
    @FXML
    private AnchorPane taskListContainer;
    @FXML
    private AnchorPane taskEditorContainer;

    private TaskListView taskList;
    private TaskEditorView taskEditor;

    private ObjectProperty<Task> selected = new SimpleObjectProperty<>(this, "selected");
    private Task clipboard;

    /**
     * Creates a new {@code MainMenu}.
     * @throws IOException if an I/O error occurs
     */
    public MainMenu(Stage stage) throws IOException {
        this.stage = stage;
        marquee = new Marquee();
        currentFile.addListener((_, _, filepath) -> {
            if (filepath == null) {
                stage.setTitle("Marquee: Untitled");
            } else {
                stage.setTitle("Marquee: " + filepath);
            }
        });

        taskList = new TaskListView();
        taskList.itemsProperty().bind(marquee.checklistProperty());
        AnchorPane.setTopAnchor(taskList, 0.0);
        AnchorPane.setRightAnchor(taskList, 0.0);
        AnchorPane.setBottomAnchor(taskList, 0.0);
        AnchorPane.setLeftAnchor(taskList, 0.0);
        selected.bind(taskList.getSelectionModel().selectedItemProperty());

        taskEditor = new TaskEditorView();
        taskEditor.taskProperty().bind(taskList.getSelectionModel().selectedItemProperty());
        AnchorPane.setTopAnchor(taskEditor, 0.0);
        AnchorPane.setRightAnchor(taskEditor, 0.0);
        AnchorPane.setBottomAnchor(taskEditor, 0.0);
        AnchorPane.setLeftAnchor(taskEditor, 0.0);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainMenu.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        loader.load();
    }

    @FXML
    void initialize() {
        content.setDividerPosition(0, 0.7);
        taskListContainer.getChildren().setAll(taskList);
        taskEditorContainer.getChildren().setAll(taskEditor);
    }

    private boolean saveAt(Path saveLocation) {
        if (saveLocation == null) {
            return false;
        }
        try {
            marquee.save(saveLocation);
            currentFile.set(saveLocation);
            return true;
        } catch (IOException e) {
            new Alert(
                    Alert.AlertType.ERROR,
                    "Unable to save file: " + e.getMessage(),
                    ButtonType.OK
            ).showAndWait();
            return false;
        }
    }

    private boolean openAt(Path openLocation) {
        if (Files.notExists(openLocation)) {
            new Alert(
                    Alert.AlertType.WARNING,
                    "File at location "
                            + openLocation.toString()
                            + " does not exist.",
                    ButtonType.OK
            ).showAndWait();
            return false;
        }
        if (!reset()) {
            return false;
        }
        try {
            if (!marquee.load(openLocation)) {
                new Alert(
                        Alert.AlertType.WARNING,
                        "Some tasks are unrecognized and skipped",
                        ButtonType.OK
                ).showAndWait();
            }
            currentFile.set(openLocation);
            return true;
        } catch (NoSuchFileException e) {
            new Alert(
                    Alert.AlertType.ERROR,
                    "File at location " + openLocation + " does not exist.",
                    ButtonType.OK
            ).showAndWait();
            return false;
        } catch (IOException e) {
            new Alert(
                    Alert.AlertType.ERROR,
                    "Unable to open file: " + e.getMessage(),
                    ButtonType.OK
            ).showAndWait();
            return false;
        } catch (FileParseException e) {
            new Alert(
                    Alert.AlertType.ERROR,
                    "Parsing error at line " + e.getLine()
                            + " column " + e.getColumn()
                            + ": " + e.getMessage(),
                    ButtonType.OK
            ).showAndWait();
            return false;
        } catch (IllegalArgumentException e) {
            new Alert(
                    Alert.AlertType.ERROR,
                    "Save file is corrupted: " + e.getMessage(),
                    ButtonType.OK
            ).showAndWait();
            return false;
        }
    }

    @FXML
    boolean reset() {
        ButtonType response = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Save "
                        + (currentFile.get() == null
                        ? "Untitled.csv"
                        : currentFile.get().getFileName().toString())
                        + " before closing?",
                ButtonType.YES,
                ButtonType.NO,
                ButtonType.CANCEL
        ).showAndWait().orElse(ButtonType.CANCEL);
        if (response == ButtonType.YES) {
            if (!save()) {
                return false;
            }
        } else if (response == ButtonType.CANCEL) {
            return false;
        }

        marquee.deleteAllTasks(false);
        TaskTag.getDictionary().clear();
        currentFile.set(null);
        return true;
    }

    @FXML
    boolean save() {
        if (currentFile.get() == null) {
            return saveAs();
        } else {
            return saveAt(currentFile.get());
        }
    }

    @FXML
    boolean saveAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save as...");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV files", "*.csv")
        );
        fileChooser.setInitialFileName("Untitled.csv");
        fileChooser.setInitialDirectory(MainApplication.getLocalStoragePath().toFile());
        File filepath = fileChooser.showSaveDialog(getScene().getWindow());
        if (filepath == null) {
            return false;
        } else {
            return saveAt(filepath.toPath());
        }
    }

    @FXML
    boolean open() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open...");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV files", "*.csv")
        );
        fileChooser.setInitialDirectory(MainApplication.getLocalStoragePath().toFile());
        File filepath = fileChooser.showOpenDialog(getScene().getWindow());
        if (filepath == null) {
            return false;
        }
        return openAt(filepath.toPath());
    }

    @FXML
    void newTask() {
        marquee.addTasks(new Task(
                "New Task",
                false,
                null,
                null,
                TaskTag.createOrGet("Abiau"),
                TaskTag.createOrGet("aeystrhfg"),
                TaskTag.createOrGet("AAvcccb V")));
    }

    @FXML
    void copySelected() {
        if (selected.get() != null) {
            clipboard = new Task(selected.get());
        }
    }

    @FXML
    void deleteSelected() {
        if (selected.get() != null) {
            marquee.checklistProperty().remove(selected.get());
        }
    }

    @FXML
    void cutSelected() {
        copySelected();
        deleteSelected();
    }

    @FXML
    void paste() {
        if (clipboard != null) {
            if (selected.get() != null) {
                marquee.checklistProperty().add(new Task(clipboard));
            }
        }
    }

    @FXML
    void refresh() {
        taskList.refresh();
    }
}
