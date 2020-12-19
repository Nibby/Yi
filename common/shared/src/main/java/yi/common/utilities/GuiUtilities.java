package yi.common.utilities;

import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;
import yi.common.component.KeyModifier;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class GuiUtilities {

    private GuiUtilities() {

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
        double r = trimToRange(r255 / 255d);
        double g = trimToRange(g255 / 255d);
        double b = trimToRange(b255 / 255d);
        alpha1 = trimToRange(alpha1);

        return new Color(r, g, b, alpha1);
    }

    private static double trimToRange(double value) {
        if (value < 0.0) {
            value = 0.0;
        }

        if (value > 1.0) {
            value = 1.0;
        }
        
        return value;
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

    public static Optional<ImageView> getIcon(String resourceFile, Class<?> aClass) {
        return getIcon(resourceFile, aClass, -1);
    }

    public static Optional<ImageView> getIcon(String resourceFile, Class<?> aClass, int fitSize) {
        return getIcon(resourceFile, aClass, fitSize, fitSize);
    }

    public static Optional<ImageView> getIcon(String resourceFile, Class<?> aClass,
                                              int fitWidth, int fitHeight) {
        try {
            var resourceStream = aClass.getResourceAsStream(resourceFile);
            var iconImage = new Image(resourceStream);
            var icon = new ImageView(iconImage);

            if (fitWidth > 0) {
                icon.setFitWidth(fitWidth);
            }
            if (fitHeight > 0) {
                icon.setFitHeight(fitHeight);
            }

            return Optional.of(icon);
        } catch (NullPointerException e) {
            return Optional.empty();
        }
    }

    public static Bounds getTextBoundsLocal(@NotNull Font font, String text) {
        var metricsTest = new Text(text);
        metricsTest.setFont(font);
        return metricsTest.getBoundsInLocal();
    }
}
