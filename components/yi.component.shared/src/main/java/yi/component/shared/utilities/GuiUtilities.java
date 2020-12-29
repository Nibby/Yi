package yi.component.shared.utilities;

import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;
import yi.component.shared.component.KeyModifier;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class GuiUtilities {

    private GuiUtilities() {

    }

    /**
     * Instantiates a {@link Color} object using 0-255 for red, green and blue values
     * rather than 0-1.0.
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
     * Instantiates a {@link Color} object using 0-255 for red, green and blue values
     * rather than 0-1.0.
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

    /**
     * Loads an internal icon resource from the module the resource class belongs to. If
     * the caller class is outside of {@code yi.component.shared} module, the resource
     * package must be opened first using the following code in the module's
     * {@code module-info.java}:
     * <pre>
     *     opens some.resource.package;
     * </pre>
     *
     * @param resourceFile Resource file path, notation should be the same as the parameter
     *                     used in {@link Class#getResourceAsStream(String)}.
     * @param resourceClass Caller class used to load the icon.
     * @return The loaded icon if it exists and is loaded successfully.
     */
    public static Optional<ImageView> getIcon(String resourceFile, Class<?> resourceClass) {
        return getIcon(resourceFile, resourceClass, -1);
    }

    /**
     * Loads an internal icon resource from the module the resource class belongs to. If
     * the caller class is outside of {@code yi.component.shared} module, the resource
     * package must be opened first using the following code in the module's
     * {@code module-info.java}:
     * <pre>
     *     opens some.resource.package;
     * </pre>
     * If the icon is loaded successfully, a fit size will be set to indicate its preferred
     * dimensions when displayed on a {@link javafx.scene.Node}. This is useful for loading
     * high resolution icons for display at a lower dimension.
     *
     * @param resourceFile Resource file path, notation should be the same as the parameter
     *                     used in {@link Class#getResourceAsStream(String)}.
     * @param resourceClass Caller class used to load the icon.
     * @param fitSize Fit size for the icon, it will be used to specify both the fit width
     *                and height.
     * @return The loaded icon if it exists and is loaded successfully.
     */
    public static Optional<ImageView> getIcon(String resourceFile, Class<?> resourceClass, int fitSize) {
        return getIcon(resourceFile, resourceClass, fitSize, fitSize);
    }

    /**
     * Loads an internal icon resource from the module the resource class belongs to. If
     * the caller class is outside of {@code yi.component.shared} module, the resource
     * package must be opened first using the following code in the module's
     * {@code module-info.java}:
     * <pre>
     *     opens some.resource.package;
     * </pre>
     * If the icon is loaded successfully, a fit size will be set to indicate its preferred
     * dimensions when displayed on a {@link javafx.scene.Node}. This is useful for loading
     * high resolution icons for display at a lower dimension.
     *
     * @param resourceFile Resource file path, notation should be the same as the parameter
     *                     used in {@link Class#getResourceAsStream(String)}.
     * @param resourceClass Caller class used to load the icon.
     * @param fitWidth Fit width size for this icon.
     * @param fitHeight Fit height size for this icon.
     * @return The loaded icon if it exists and is loaded successfully.
     */
    public static Optional<ImageView> getIcon(String resourceFile, Class<?> resourceClass,
                                              int fitWidth, int fitHeight) {

        InputStream resourceStream;
        resourceStream = resourceClass.getResourceAsStream(resourceFile);
        if (resourceStream == null) {
            return Optional.empty();
        }

        var iconImage = new Image(resourceStream);
        var icon = new ImageView(iconImage);

        if (fitWidth > 0) {
            icon.setFitWidth(fitWidth);
        }
        if (fitHeight > 0) {
            icon.setFitHeight(fitHeight);
        }

        return Optional.of(icon);
    }

    /**
     * Returns the calculated size for a body of text using the given font.
     *
     * @param font Font used to display the text.
     * @param text Text data to be displayed.
     * @return Calculated bounds for the text.
     */
    public static Bounds getTextBoundsLocal(@NotNull Font font, String text) {
        var metricsTest = new Text(text);
        metricsTest.setFont(font);
        return metricsTest.getBoundsInLocal();
    }

    /**
     * Creates an empty panel whose width is automatically calculated based on the
     * container width. This can be used to center elements like so:<br />
     * <pre>
     *     toolBar.getItems().add(createDynamicSpacer());
     *     // Add toolbar items
     *     toolBar.getItems().add(createDynamicSpacer());
     * </pre>
     * Alternatively, using just one dynamic spacer at either side will push all
     * items to the other side of the container.
     *
     * @return Spacer component to be added to a container.
     */
    public static Pane createDynamicSpacer() {
        var dynamicSpacer = new Pane();
        HBox.setHgrow(dynamicSpacer, Priority.SOMETIMES);

        return dynamicSpacer;
    }

    /**
     * Creates an empty panel of specified width to pad out components in a container.
     *
     * @param width Spacer width in pixels.
     * @return Spacer component to be added to a container.
     */
    public static Pane createStaticSpacer(int width) {
        var spacer = new Pane();
        spacer.setPrefWidth(width);

        return spacer;
    }
}
