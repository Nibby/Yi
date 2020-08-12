package yi.editor.components;

import javafx.geometry.Orientation;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import yi.component.SimpleListenerManager;
import yi.editor.settings.Settings;
import yi.editor.utilities.IconUtilities;

import java.util.function.Consumer;

/**
 * A toolbar that provides view controls.
 */
public final class ControlToolBar extends ToolBar {

    private final SimpleListenerManager<ContentLayout> layoutOptionsSimpleListenerManager = new SimpleListenerManager<>();
    private final ToggleButton toggleReviewMode;

    @SuppressWarnings("UnusedLabel")
    public ControlToolBar() {
        layoutOptions: {
            toggleReviewMode = new ToggleButton();
            IconUtilities.getIcon("/icons/editMode32.png").ifPresent(toggleReviewMode::setGraphic);

            var currentValue = Settings.general.getCurrentLayout() == ContentLayout.REVIEW;
            toggleReviewMode.setSelected(currentValue);

            getItems().add(toggleReviewMode);

            toggleReviewMode.selectedProperty().addListener((observer, wasSelected, isSelected) -> {
                var newLayout = isSelected ? ContentLayout.REVIEW : ContentLayout.COMPACT;
                layoutOptionsSimpleListenerManager.fireValueChangeEvent(newLayout);
                Settings.general.setCurrentLayout(newLayout);
                Settings.general.save();
            });
        }
    }

    public void addLayoutOptionsValueListener(Consumer<ContentLayout> listener) {
        layoutOptionsSimpleListenerManager.addListener(listener);
    }

    public void removeLayoutOptionsValueListener(Consumer<ContentLayout> listener) {
        layoutOptionsSimpleListenerManager.removeListener(listener);
    }

}
