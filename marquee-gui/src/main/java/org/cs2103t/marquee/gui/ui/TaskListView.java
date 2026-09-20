package org.cs2103t.marquee.gui.ui;

import java.io.IOException;
import java.util.ArrayList;

import org.cs2103t.marquee.core.task.Task;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

/**
 * Controller for a task list view.
 */
public class TaskListView extends ListView<Task> {

    /**
     * Controller for a task list cell.
     */
    public static class TaskListCell extends ListCell<Task> {
        private TaskItemView taskView;

        /**
         * Creates a new {@code TaskListCell}.
         */
        public TaskListCell() {
            setPadding(new Insets(0));
            setMaxWidth(Double.MAX_VALUE);
            try {
                taskView = new TaskItemView(this);
            } catch (IOException e) {
                taskView = null;
            }
        }

        @Override
        protected void updateItem(Task item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                if (taskView == null) {
                    setText(item.toString());
                } else {
                    taskView.setTask(item);
                    setGraphic(taskView);
                }
            }
        }
    }

    private final ListProperty<Task> taskList =
            new SimpleListProperty<>(this, "taskList", FXCollections.observableList(new ArrayList<>()));

    /**
     * Creates a new {@code TaskListView}.
     */
    public TaskListView() {
        setFixedCellSize(75);
        setMaxWidth(Double.MAX_VALUE);
        setCellFactory(_ -> new TaskListCell());
    }
}
