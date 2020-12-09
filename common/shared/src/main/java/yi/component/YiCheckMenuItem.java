package yi.component;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import org.jetbrains.annotations.Nullable;
import yi.common.i18n.I18n;
import yi.common.i18n.TextResource;

public class YiCheckMenuItem extends CheckMenuItem implements YiComponent, Toggle {

    private final ObjectProperty<ToggleGroup> toggleGroup = new SimpleObjectProperty<>(null);

    public YiCheckMenuItem() {
        super();
    }

    public YiCheckMenuItem(TextResource key) {
        super(key == null ? "" : key.getLocalisedText());
        setText(key);
    }

    public YiCheckMenuItem(TextResource key, Node graphic) {
        super(key == null ? "" : key.getLocalisedText(), graphic);
        setText(key);
    }

    /**
     * Sets the text for this item using a locale-agnostic resource key.
     * Text is set at runtime based on the currently set language given by
     * {@link I18n#getCurrentLanguage()}.
     *
     * @param key A text resource key, may be null to indicate no text.
     */
    public void setText(@Nullable TextResource key) {
        setText(key == null ? "" : key.getLocalisedText());
    }

    @Override
    public ToggleGroup getToggleGroup() {
        return this.toggleGroup.get();
    }

    @Override
    public void setToggleGroup(ToggleGroup toggleGroup) {
        this.toggleGroup.setValue(toggleGroup);
    }

    @Override
    public ObjectProperty<ToggleGroup> toggleGroupProperty() {
        return toggleGroup;
    }

}
