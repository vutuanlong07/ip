package org.cs2103t.marquee.gui.ui;

import java.io.IOException;
import java.time.LocalDateTime;

import org.cs2103t.marquee.core.task.Task;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;

/**
 * Controller for editable task item view.
 */
public class TaskEditorView extends GridPane {
    private ObjectProperty<Task> task = new SimpleObjectProperty<>(this, "task");

    private TextFormatter<String> descriptionFormatter = new TextFormatter<>(TextFormatter.IDENTITY_STRING_CONVERTER);
    private TextFormatter<LocalDateTime> startFormatter = new TextFormatter<>(MainMenu.DATETIME_STRING_CONVERTER);
    private TextFormatter<LocalDateTime> endFormatter = new TextFormatter<>(MainMenu.DATETIME_STRING_CONVERTER);

    @FXML
    private CheckBox mark;
    @FXML
    private TextArea description;
    @FXML
    private TagsListView tags;
    @FXML
    private TextField start;
    @FXML
    private TextField end;

    /**
     * Creates a new {@code TaskEditorView}.
     * @throws IOException if an I/O error occurs
     */
    public TaskEditorView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TaskEditorView.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        loader.load();
        setOnMousePressed(event -> requestFocus());

        description.setTextFormatter(descriptionFormatter);
        start.setTextFormatter(startFormatter);
        end.setTextFormatter(endFormatter);

        task.addListener((_, oldValue, newValue) -> {
            if (oldValue != null) {
                mark.selectedProperty().unbindBidirectional(oldValue.markProperty());
                descriptionFormatter.valueProperty().unbindBidirectional(oldValue.descriptionProperty());
                startFormatter.valueProperty().unbindBidirectional(oldValue.startProperty());
                endFormatter.valueProperty().unbindBidirectional(oldValue.endProperty());
            }
            if (newValue != null) {
                mark.setDisable(false);
                description.setDisable(false);
                start.setDisable(false);
                end.setDisable(false);

                mark.selectedProperty().bindBidirectional(newValue.markProperty());
                descriptionFormatter.valueProperty().bindBidirectional(newValue.descriptionProperty());
                startFormatter.valueProperty().bindBidirectional(newValue.startProperty());
                endFormatter.valueProperty().bindBidirectional(newValue.endProperty());
            } else {
                mark.setDisable(true);
                description.setDisable(true);
                start.setDisable(true);
                end.setDisable(true);

                mark.setSelected(false);
                description.setText("");
                start.setText("");
                end.setText("");
            }
        });
    }

    private void errorDialogue(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Task getTask() {
        return task.get();
    }

    public void setTask(Task newTask) {
        task.set(newTask);
    }

    public ObjectProperty<Task> taskProperty() {
        return task;
    }
}
