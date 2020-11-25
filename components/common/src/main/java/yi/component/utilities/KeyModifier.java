package yi.component.utilities;

import javafx.scene.input.KeyCode;

/**
 * List of available key combination modifiers for {@link GuiUtilities#getKeyCombination(KeyCode, KeyModifier...)}.
 * <p/>
 * Note use in meta/control modifier key between macOS and non-macOS platforms. In
 * situations where the meta/control modifier should vary between platform, use the
 * {@link #SHORTCUT} value instead.
 */
public enum KeyModifier {
    SHORTCUT,
    CTRL,
    META,
    SHIFT,
    ALT
}