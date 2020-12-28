package yi.component.shared.component;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import org.jetbrains.annotations.Nullable;
import yi.component.shared.i18n.I18n;
import yi.component.shared.i18n.TextResource;

/**
 * Wrapper for {@link Button} with localised text support.
 */
@SuppressWarnings("unused")
public class YiButton extends Button implements YiComponent {

    public YiButton() {
        super();
    }

    public YiButton(TextResource key) {
        super(key.getLocalisedText());
        setText(key);
    }

    public YiButton(TextResource key, Node graphic) {
        super(key.getLocalisedText(), graphic);
        setText(key);
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

    @Override
    public Node getComponent() {
        return this;
    }
}
