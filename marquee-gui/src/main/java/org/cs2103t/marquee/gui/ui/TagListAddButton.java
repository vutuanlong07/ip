package org.cs2103t.marquee.gui.ui;

import java.io.IOException;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Controller for tags list add button.
 */
public class TagListAddButton extends HBox {
    private final TagListView parent;

    @FXML
    private HBox buttonContainer;
    @FXML
    private Label hiddenCountLabel;

    /**
     * Creates a {@code TagListAddButton}.
     */
    public TagListAddButton(TagListView parent) throws IOException {
        this.parent = parent;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TagListAddButton.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        loader.load();
    }

    @FXML
    void initialize() {
        hiddenCountLabel.textProperty().bind(Bindings.concat("+", parent.hiddenCountProperty(), " more..."));
        hiddenCountLabel.visibleProperty().bind(parent.hiddenCountProperty().isNotEqualTo(0));
        hiddenCountLabel.managedProperty().bind(parent.hiddenCountProperty().isNotEqualTo(0));
        buttonContainer.visibleProperty().bind(parent.addAllowedProperty());
    }

    @FXML
    private void selectTagDialog() {

    }
}
