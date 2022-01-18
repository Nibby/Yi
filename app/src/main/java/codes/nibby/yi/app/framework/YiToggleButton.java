package codes.nibby.yi.app.framework;

import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import org.jetbrains.annotations.Nullable;
import codes.nibby.yi.app.i18n.I18n;
import codes.nibby.yi.app.i18n.TextResource;

public class YiToggleButton extends ToggleButton implements YiComponent {

    public YiToggleButton() {
        super();
    }

    public YiToggleButton(TextResource textResource) {
        super(textResource.getLocalisedText());
    }

    public YiToggleButton(TextResource textResource, Node graphic) {
        super(textResource.getLocalisedText(), graphic);
    }

    /**
     * Sets the text for this button using a locale-agnostic resource key.
     * Text is set at runtime based on the currently set language given by
     * {@link I18n#getCurrentLanguage()}.
     *
     * @param key A text resource key, may be null to indicate no text.
     */
    public void setText(@Nullable TextResource key) {
        setText(key == null ? "" : key.getLocalisedText());
    }

    /**
     * Sets the tooltip for this component using a locale agnostic resource key.
     * Text is set at runtime based on the currently set language given by
     * {@link I18n#getCurrentLanguage()}.
     *
     * @param key A text resource key, may be null to indicate no text.
     */
    public void setTooltip(@Nullable TextResource key) {
        var tooltip = key == null ? new Tooltip() : new Tooltip(key.getLocalisedText());
        setTooltip(tooltip);
    }
}
