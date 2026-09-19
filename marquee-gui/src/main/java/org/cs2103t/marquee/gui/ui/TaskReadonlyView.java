package org.cs2103t.marquee.gui.ui;

import java.io.IOException;

import org.cs2103t.marquee.core.task.Task;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/**
 * Controller for read-only task item view.
 */
public class TaskReadonlyView extends HBox {
    private static final PseudoClass MARKED_CLASS = PseudoClass.getPseudoClass("completed");

    private ObjectProperty<Task> task = new SimpleObjectProperty<>(this, "task");
    private ChangeListener<Boolean> markListener = (_, oldValue, newValue) ->
            pseudoClassStateChanged(MARKED_CLASS, newValue);

    @FXML
    private Label description;
    @FXML
    private TagsView tags;
    @FXML
    private Label start;
    @FXML
    private Label end;
    @FXML
    private CheckBox selected;

    /**
     * Creates a new {@code TaskReadonlyView}.
     * @throws IOException if an I/O error occurs
     */
    public TaskReadonlyView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TaskReadonlyView.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        loader.load();

        description.maxWidthProperty().bind(((StackPane) description.getParent()).widthProperty());

        task.addListener((_, oldValue, newValue) -> {
            if (oldValue != null) {
                description.textProperty().unbindBidirectional(oldValue.descriptionProperty());
                oldValue.markProperty().removeListener(markListener);
                start.textProperty().unbindBidirectional(oldValue.startProperty());
                end.textProperty().unbindBidirectional(oldValue.endProperty());
            }
            if (newValue != null) {
                this.setDisable(false);

                description.textProperty().bindBidirectional(newValue.descriptionProperty());
                newValue.markProperty().addListener(markListener);
                start.textProperty().bindBidirectional(newValue.startProperty(), MainMenu.DATETIME_STRING_CONVERTER);
                end.textProperty().bindBidirectional(newValue.endProperty(), MainMenu.DATETIME_STRING_CONVERTER);
            } else {
                this.setDisable(true);

                description.setText("");
                pseudoClassStateChanged(MARKED_CLASS, false);
                start.setText("");
                end.setText("");
            }
        });
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
