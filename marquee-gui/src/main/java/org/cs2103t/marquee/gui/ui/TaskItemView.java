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
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/**
 * Controller for read-only task item view.
 */
public class TaskItemView extends HBox {
    private static final PseudoClass MARKED_CLASS = PseudoClass.getPseudoClass("completed");

    private final ObjectProperty<Task> task = new SimpleObjectProperty<>(this, "task");
    private final ChangeListener<Boolean> markListener = (_, oldValue, newValue) ->
            pseudoClassStateChanged(MARKED_CLASS, newValue);

    private ListCell<Task> parent;
    @FXML
    private Label description;
    @FXML
    private StackPane descriptionContainer;
    @FXML
    private TagListView tags;
    @FXML
    private HBox resizable;
    @FXML
    private Label start;
    @FXML
    private Label end;
    @FXML
    private CheckBox selected;

    /**
     * Creates a new {@code TaskItemView}.
     * @throws IOException if an I/O error occurs
     */
    public TaskItemView(ListCell<Task> parent) throws IOException {
        this.parent = parent;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TaskItemView.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        loader.load();

        maxWidthProperty().bind(this.parent.widthProperty());
        descriptionContainer.prefWidthProperty().bind(resizable.widthProperty().divide(2));

        task.addListener((_, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.markProperty().removeListener(markListener);
                description.textProperty().unbindBidirectional(oldValue.descriptionProperty());
                start.textProperty().unbindBidirectional(oldValue.startProperty());
                end.textProperty().unbindBidirectional(oldValue.endProperty());
            }
            if (newValue != null) {
                this.setDisable(false);

                newValue.markProperty().addListener(markListener);
                pseudoClassStateChanged(MARKED_CLASS, newValue.isMarked());
                description.textProperty().bindBidirectional(newValue.descriptionProperty());
                start.textProperty().bindBidirectional(newValue.startProperty(), MainMenu.DATETIME_STRING_CONVERTER);
                end.textProperty().bindBidirectional(newValue.endProperty(), MainMenu.DATETIME_STRING_CONVERTER);
            } else {
                this.setDisable(true);

                pseudoClassStateChanged(MARKED_CLASS, true);
                description.setText("");
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
