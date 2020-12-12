package yi.common.component;

import javafx.scene.Node;
import javafx.scene.control.RadioMenuItem;
import org.jetbrains.annotations.Nullable;
import yi.common.i18n.I18n;
import yi.common.i18n.TextResource;

public class YiRadioMenuItem extends RadioMenuItem implements YiComponent {

    public YiRadioMenuItem() {
        super();
    }

    public YiRadioMenuItem(TextResource key) {
        super(key == null ? "" : key.getLocalisedText());
        setText(key);
    }

    public YiRadioMenuItem(TextResource key, Node graphic) {
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
}
