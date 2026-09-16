package org.cs2103t.marquee.gui.ui.control;

import org.cs2103t.marquee.core.task.Task;

import javafx.scene.control.Control;

/**
 * Control for {@link Task}
 */
public class TaskView extends Control {
    private final Task task;

    public TaskView(Task task) {
        this.task = task;
    }
}
