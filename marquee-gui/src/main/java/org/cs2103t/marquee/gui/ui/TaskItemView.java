package org.cs2103t.marquee.gui.ui;

import java.io.IOException;

import org.cs2103t.marquee.core.task.Task;
import org.cs2103t.marquee.core.time.DateTimeFormatter;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;
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
    private AnchorPane tagsContainer;
    @FXML
    private HBox resizable;
    @FXML
    private Label start;
    @FXML
    private Label end;

    private final TagListView tags;

    /**
     * Creates a new {@code TaskItemView}.
     * @throws IOException if an I/O error occurs
     */
    public TaskItemView(ListCell<Task> parent) throws IOException {
        tags = new TagListView();
        tags.setAddAllowed(false);
        AnchorPane.setTopAnchor(tags, 0.0);
        AnchorPane.setRightAnchor(tags, 0.0);
        AnchorPane.setBottomAnchor(tags, 0.0);
        AnchorPane.setLeftAnchor(tags, 0.0);

        mark.addListener((_, _, isMarked) ->
                pseudoClassStateChanged(MARKED_CLASS, isMarked)
        );
        task.addListener((_, oldValue, newValue) -> {
            if (oldValue != null) {
                mark.unbind();
                description.textProperty().unbind();
                tags.itemsProperty().unbind();
                start.textProperty().unbind();
                end.textProperty().unbind();
            }
            if (newValue != null) {
                setDisable(false);
                mark.bind(newValue.markProperty());
                description.textProperty().bind(newValue.descriptionProperty());
                tags.itemsProperty().bind(newValue.tagsProperty());
                start.textProperty().bind(newValue.startProperty().map(DateTimeFormatter::formatDateTime));
                end.textProperty().bind(newValue.endProperty().map(DateTimeFormatter::formatDateTime));
            } else {
                setDisable(true);
                mark.set(false);
                description.setText("");
                tags.setItems(null);
                start.setText("");
                end.setText("");
            }
        });
        prefWidthProperty().bind(parent.widthProperty());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TaskItemView.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        loader.load();

        mark.set(true);
    }

    @FXML
    void initialize() {
        tagsContainer.getChildren().setAll(tags);
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
