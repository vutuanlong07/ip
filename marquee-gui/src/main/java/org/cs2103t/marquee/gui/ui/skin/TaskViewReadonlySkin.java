package org.cs2103t.marquee.gui.ui.skin;

import org.cs2103t.marquee.gui.ui.control.TaskView;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.FlowPane;

/**
 * Read-only skin for task items.
 */
public class TaskViewReadonlySkin extends SkinBase<TaskView> {
    public TaskViewReadonlySkin(TaskView control) {
        super(control);
    }

    @FXML
    private Label description;

    @FXML
    private Label endTime;

    @FXML
    private CheckBox selected;

    @FXML
    private Label startTime;

    @FXML
    private FlowPane tags;
}
