package yi.common.utilities;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import yi.component.KeyModifier;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class GuiUtilities {

    private GuiUtilities() {

    }

    /**
     * The main action key is {@code command} aka meta on macOS, otherwise {@code ctrl} on other
     * operating systems.
     *
     * @return true if the native action key has been pressed.
     */
    public static boolean isActionKeyDown(KeyEvent e) {
        return SystemUtilities.isMac() ? e.isMetaDown() : e.isControlDown();
    }

    /**
     * Instantiates a {@link Color} object using 0-255 for red, green and blue values rather than 0-1.0.
     * <p>
     * Values exceeding these boundaries will be clipped.
     *
     * @param r255 Red value, 0 - 255 inclusive
     * @param g255 Green value, 0 - 255 inclusive
     * @param b255 Blue value, 0 - 255 inclusive
     * @return A color of the defined RGB value
     */
    public static Color getColor(int r255, int g255, int b255) {
        return getColor(r255, g255, b255, 1.0d);
    }

    /**
     * Instantiates a {@link Color} object using 0-255 for red, green and blue values rather than 0-1.0.
     * However, the alpha value is still between 0 - 1.0 inclusive.
     * <p>
     * Values exceeding these boundaries will be clipped.
     *
     * @param r255 Red value, 0 - 255 inclusive
     * @param g255 Green value, 0 - 255 inclusive
     * @param b255 Blue value, 0 - 255 inclusive
     * @return A color of the defined RGB value
     */
    public static Color getColor(int r255, int g255, int b255, double alpha1) {
        double r = r255 / 255d;
        double g = g255 / 255d;
        double b = b255 / 255d;

        if (r > 1.0d) r = 1.0d;
        if (r < 0d) r = 0d;

        if (g > 1.0d) g = 1.0d;
        if (g < 0d) g = 0d;

        if (b > 1.0d) b = 1.0d;
        if (b < 0d) b = 0d;

        if (alpha1 > 1.0d) alpha1 = 1.0d;
        if (alpha1 < 0d) alpha1 = 0d;

        return new Color(r, g, b, alpha1);
    }

    /**
     * Helper method to create Fx {@link KeyCombination} in a readable way.
     *
     * @param keyCode Main key combination keycode
     * @param modifiers Modifier states, the presence of which indicates the modifier key
     *                  must be held down. Otherwise the key must be up.
     * @return The requested key combination.
     *
     * @see KeyModifier Available modifier keys
     */
    public static KeyCombination getKeyCombination(KeyCode keyCode, KeyModifier ... modifiers) {
        Set<KeyModifier> modifierSet = Arrays.stream(modifiers).collect(Collectors.toSet());
        boolean shortcutDown = modifierSet.contains(KeyModifier.SHORTCUT);
        boolean ctrlDown = modifierSet.contains(KeyModifier.CTRL);
        boolean altDown = modifierSet.contains(KeyModifier.ALT);
        boolean shiftDown = modifierSet.contains(KeyModifier.SHIFT);
        boolean metaDown = modifierSet.contains(KeyModifier.META);

        return new KeyCodeCombination(keyCode,
                getModifierValue(shiftDown),
                getModifierValue(ctrlDown),
                getModifierValue(altDown),
                getModifierValue(metaDown),
                getModifierValue(shortcutDown));
    }

    private static KeyCombination.ModifierValue getModifierValue(boolean modifierDown) {
        return modifierDown ? KeyCombination.ModifierValue.DOWN : KeyCombination.ModifierValue.UP;
    }


}
