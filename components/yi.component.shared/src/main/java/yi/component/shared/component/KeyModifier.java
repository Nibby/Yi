package yi.component.shared.component;

import javafx.scene.input.KeyCode;
import yi.component.shared.utilities.GuiUtilities;

import java.util.Optional;

/**
 * List of available key combination modifiers for {@link GuiUtilities#getKeyCombination(KeyCode, KeyModifier...)}.
 * <p/>
 * Note use in meta/control modifier key between macOS and non-macOS platforms. In
 * situations where the meta/control modifier should vary between platform, use the
 * {@link #SHORTCUT} value instead.
 */
public enum KeyModifier {
    SHORTCUT("shortcut"),
    CTRL("ctrl"),
    META("meta"),
    SHIFT("shift"),
    ALT("alt");

    private final String serializedValue;

    KeyModifier(String value) {
        this.serializedValue = value;
    }

    public String getSerializedValue() {
        return serializedValue;
    }

    public static Optional<KeyModifier> fromSerializedValue(String serializedValue) {
        for (KeyModifier value : KeyModifier.values()) {
            if (value.getSerializedValue().equals(serializedValue)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}