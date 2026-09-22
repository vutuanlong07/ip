package org.cs2103t.marquee.gui.ui;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.cs2103t.marquee.core.task.TaskTag;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.layout.FlowPane;

/**
 * Controller for tags view.
 */
public class TagListView extends FlowPane {
    private final SetProperty<TaskTag> items =
            new SimpleSetProperty<>(this, "items", FXCollections.observableSet());

    private final HashMap<TaskTag, TagItemView> cellsByTag = new HashMap<>();
    private final List<TagItemView> pile = new LinkedList<>();

    private final BooleanProperty isAddAllowed = new SimpleBooleanProperty(this, "allowAdd", true);
    private final BooleanProperty isBroken = new SimpleBooleanProperty(this, "isBroken", false);
    private final IntegerProperty hiddenCount = new SimpleIntegerProperty(this, "hiddenCount", 0);

    private final ReadOnlyIntegerProperty hiddenCountReadOnly =
            ReadOnlyIntegerWrapper.readOnlyIntegerProperty(hiddenCount);

    private final TagListAddButton addButton;

    /**
     * Creates a {@code TagListView}.
     */
    public TagListView() throws IOException {
        addButton = new TagListAddButton(this);
        getChildren().add(addButton);

        items.addListener(this::fixCellCount);
        needsLayoutProperty().addListener((_, _, _) -> Platform.runLater(this::layoutCells));

        setHgap(10);
        setVgap(10);
    }

    public ObservableSet<TaskTag> getItems() {
        return items.get();
    }
    public void setItems(ObservableSet<TaskTag> newItems) {
        items.set(newItems);
    }
    public SetProperty<TaskTag> itemsProperty() {
        return items;
    }

    public boolean isAddAllowed() {
        return isAddAllowed.get();
    }
    public void setAddAllowed(boolean addAllowed) {
        isAddAllowed.set(addAllowed);
    }
    public BooleanProperty addAllowedProperty() {
        return isAddAllowed;
    }

    public int getHiddenCount() {
        return hiddenCount.get();
    }
    public ReadOnlyIntegerProperty hiddenCountProperty() {
        return hiddenCountReadOnly;
    }

    private TagItemView addCell() {
        TagItemView newCell;
        try {
            newCell = pile.removeLast();
        } catch (NoSuchElementException _) {
            try {
                newCell = new TagItemView(this);
            } catch (IOException e) {
                e.printStackTrace();
                isBroken.set(true);
                return null;
            }
        }
        getChildren().add(newCell);
        return newCell;
    }

    private boolean stashCell(TagItemView item) {
        if (getChildren().remove(item)) {
            pile.add(item);
            return true;
        } else {
            return false;
        }
    }

    private void reconstructCells() {
        pile.clear();
        getChildren().clear();
        cellsByTag.clear();

        if (items.get() != null) {
            for (TaskTag item : items.get()) {
                TagItemView cell = addCell();
                if (cell != null) {
                    cell.setTag(item);
                    cellsByTag.put(item, cell);
                } else {
                    isBroken.set(true);
                }
            }
        }
    }

    private void fixCellCount(SetChangeListener.Change<? extends TaskTag> c) {
        if (isBroken.get()) {
            reconstructCells();
        } else if (c.wasAdded()) {
            TagItemView cell = addCell();
            if (cell != null) {
                cell.setTag(c.getElementAdded());
                cellsByTag.put(c.getElementAdded(), cell);
            } else {
                isBroken.set(true);
            }
        } else if (c.wasRemoved()) {
            stashCell(cellsByTag.remove(c.getElementRemoved()));
        }
        resetCellLayout();
    }

    private void resetCellLayout() {
        getChildren().remove(addButton);
        getChildren().add(addButton);
        cellsByTag.forEach((_, cell) -> cell.setOverflow(false));
        layoutCells();
    }

    private void layoutCells() {
        int i = getChildren().indexOf(addButton);
        int oldI = i;
        if (getChildren().get(i).getBoundsInParent().getMaxY() < getLayoutBounds().getMaxY()) {
            while (i < cellsByTag.size()) {
                i++;
                if (getChildren().get(i).getBoundsInParent().getMaxY() < getLayoutBounds().getMaxY()) {
                    ((TagItemView) getChildren().get(i)).setOverflow(false);
                } else {
                    i--;
                    break;
                }
            }
        } else {
            while (i > 0) {
                i--;
                ((TagItemView) getChildren().get(i)).setOverflow(true);
                if (getChildren().get(i).getBoundsInParent().getMaxY() < getLayoutBounds().getMaxY()) {
                    break;
                }
            }
        }
        if (oldI != i) {
            getChildren().remove(addButton);
            getChildren().add(i, addButton);
        }
        hiddenCount.set((int) cellsByTag.values().stream().filter(cell -> !cell.isOverflow()).count());
    }
}
