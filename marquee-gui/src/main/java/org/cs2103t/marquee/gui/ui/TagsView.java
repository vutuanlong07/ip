package org.cs2103t.marquee.gui.ui;

import java.io.IOException;

import org.cs2103t.marquee.core.task.TaskTag;

import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

/**
 * Controller for an inline tag chip display.
 */
public class TagsView extends FlowPane {
    private SetProperty<TaskTag> tags = new SimpleSetProperty<>(this, "tags", FXCollections.observableSet());

    @FXML
    private TextField newTagLabel;
    @FXML
    private ListView<TaskTag> taskListView;

    /**
     * Creates a new {@code TagsView}.
     * @throws IOException if an I/O error occurs
     */
    public TagsView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TagsView.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        loader.load();
    }

    @FXML
    void newTag() {
        tags.add(TaskTag.createOrGet(newTagLabel.textProperty().get()));
    }
}
