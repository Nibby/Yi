package yi.editor.components;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;

import java.util.Arrays;

/**
 * A toolbar that provides view controls.
 */
public final class ControlToolBar extends ToolBar {

    private final ComboBox<ComponentLayout> layoutOptions;

    public ControlToolBar() {
        layoutOptions = new ComboBox<>();
        Arrays.stream(ComponentLayout.values()).forEach(value -> layoutOptions.getItems().add(value));
        getItems().add(layoutOptions);
    }

}
