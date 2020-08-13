package yi.component.utilities;

import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

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
    public static Color getNormalized(int r255, int g255, int b255) {
        return getNormalized(r255, g255, b255, 1.0d);
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
    public static Color getNormalized(int r255, int g255, int b255, double alpha1) {
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
}
