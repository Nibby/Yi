package yi.component;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import org.jetbrains.annotations.Nullable;
import yi.common.i18n.I18n;
import yi.common.i18n.TextResource;

/**
 * Wrapper for {@link MenuItem} with localised text support.
 */
@SuppressWarnings("unused")
public class YiMenuItem extends MenuItem implements YiComponent {

    public YiMenuItem(TextResource key) {
        super(key.getLocalisedText());
        setText(key);
    }

    public YiMenuItem(TextResource key, Node graphic) {
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [Text: '" + getText() + "']";
    }
}
