package org.cs2103t.marquee.gui.ui;

import java.io.IOException;

import org.cs2103t.marquee.core.task.Task;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

/**
 * Controller for read-only task item view.
 */
public class TaskItemView extends HBox {
    private static final PseudoClass MARKED_CLASS = PseudoClass.getPseudoClass("completed");

    private final ObjectProperty<Task> task = new SimpleObjectProperty<>(this, "task");
    private final BooleanProperty mark = new SimpleBooleanProperty(this, "mark");

    @FXML
    private Label description;
    @FXML
    private Label tags;
    @FXML
    private HBox resizable;
    @FXML
    private Label start;
    @FXML
    private Label end;

    /**
     * Creates a new {@code TaskItemView}.
     * @throws IOException if an I/O error occurs
     */
    @SuppressWarnings("checkstyle:SeparatorWrap")
    public TaskItemView(ListCell<Task> parent) throws IOException {
        mark.addListener((_, oldValue, newValue) ->
                pseudoClassStateChanged(MARKED_CLASS, newValue)
        );
        mark.set(true);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TaskItemView.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        loader.load();

        prefWidthProperty().bind(parent.widthProperty());

        task.addListener((_, oldValue, newValue) -> {
            mark.unbind();
            description.textProperty().unbind();
            start.textProperty().unbind();
            end.textProperty().unbind();
            if (newValue != null) {
                this.setDisable(false);
                mark.bind(newValue.markProperty());
                description.textProperty().bind(newValue.descriptionProperty());
                start.textProperty().bind(Bindings.createStringBinding(
                        () -> MainMenu.DATETIME_STRING_CONVERTER.toString(newValue.getStart()),
                        newValue.startProperty()
                ));
                end.textProperty().bind(Bindings.createStringBinding(
                        () -> MainMenu.DATETIME_STRING_CONVERTER.toString(newValue.getEnd()),
                        newValue.endProperty()
                ));
            } else {
                this.setDisable(true);
                mark.set(false);
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
