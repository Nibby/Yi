package yi.component.shared.component;

import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.jetbrains.annotations.Nullable;
import yi.component.shared.i18n.I18n;
import yi.component.shared.i18n.TextResource;

/**
 * Wrapper for {@link Menu} with localised text support.
 */
@SuppressWarnings("unused")
public class YiMenu extends Menu implements YiComponent {

    public YiMenu(TextResource key) {
        super(key.getLocalisedText());
        setText(key);
    }

    public YiMenu(TextResource key, Node graphic) {
        super(key.getLocalisedText(), graphic);
        setText(key);
    }

    public YiMenu(TextResource key, Node graphic, MenuItem... items) {
        super(key.getLocalisedText(), graphic, items);
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
