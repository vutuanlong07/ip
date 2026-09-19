package org.cs2103t.marquee.gui.ui;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.cs2103t.marquee.core.Marquee;
import org.cs2103t.marquee.core.task.Task;
import org.cs2103t.marquee.core.time.DateTimeFormatter;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Controller class for the main menu.
 */
public class MainMenu extends VBox {
    public static final StringConverter<LocalDateTime> DATETIME_STRING_CONVERTER =
            new StringConverter<LocalDateTime>() {
                @Override
                public String toString(LocalDateTime object) {
                    return object == null ? "" : DateTimeFormatter.formatDateTime(object);
                }

                @Override
                public LocalDateTime fromString(String string) throws DateTimeParseException {
                    return string.isEmpty() ? null : DateTimeFormatter.parseDateTime(string);
                }
            };

    private Marquee marquee;

    @FXML
    private Pane taskListContainer;
    @FXML
    private Pane taskEditorContainer;

    private TaskListView taskList;
    private TaskEditorView taskEditor;

    /**
     * Creates a new {@code MainMenu}.
     * @throws IOException if an I/O error occurs
     */
    public MainMenu() throws IOException {
        marquee = new Marquee();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainMenu.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        loader.load();
    }

    @FXML
    void initialize() throws IOException {
        taskList = new TaskListView();
        taskList.itemsProperty().bind(marquee.checklistProperty());
        taskListContainer.getChildren().setAll(taskList);

        taskEditor = new TaskEditorView();
        taskEditor.taskProperty().bind(taskList.getSelectionModel().selectedItemProperty());
        taskEditorContainer.getChildren().setAll(taskEditor);
    }

    @FXML
    void newTask(ActionEvent event) {
        marquee.addTasks(new Task());
    }

    @FXML
    void newTag(ActionEvent event) {

    }

    @FXML
    void copySelected(ActionEvent event) {

    }

    @FXML
    void cutSelected(ActionEvent event) {

    }

    @FXML
    void deleteSelected(ActionEvent event) {

    }

    @FXML
    void deselectAll(ActionEvent event) {

    }

    @FXML
    void openExisting(ActionEvent event) {

    }

    @FXML
    void openNew(ActionEvent event) {

    }

    @FXML
    void paste(ActionEvent event) {

    }

    @FXML
    void saveCurrent(ActionEvent event) {

    }

    @FXML
    void saveNew(ActionEvent event) {

    }

    @FXML
    void selectAll(ActionEvent event) {

    }

    @FXML
    void showHelp(ActionEvent event) {

    }

}
