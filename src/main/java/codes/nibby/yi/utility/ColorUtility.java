package codes.nibby.qipan.utility;

import javafx.scene.paint.Color;

/**
 * Some handy tools for working with colors.
 *
 * @author Kevin Yang
 * Created on 23 August 2019
 */
public class ColorUtility {


    public static Color parseRGB_255(String rgb) {
        return parseRGB_255(rgb.split(","));
    }

    public static Color parseRGB_255(String[] rgb) {
        String[] rgba = { rgb[0], rgb[1], rgb[2], "255" };
        return parseRGBA_255(rgba);
    }

    public static Color parseRGBA_255(String rgba) {
        return parseRGBA_255(rgba.split(","));
    }

    private static Color parseRGBA_255(String[] rgba) {
        double r = Double.parseDouble(rgba[0]) / 255d;
        double g = Double.parseDouble(rgba[1]) / 255d;
        double b = Double.parseDouble(rgba[2]) / 255d;
        double a = Double.parseDouble(rgba[3]) / 255d;

        if (r > 1.0d) r = 1.0d;     if (r < 0.0d) r = 0.0d;
        if (g > 1.0d) g = 1.0d;     if (g < 0.0d) g = 0.0d;
        if (b > 1.0d) b = 1.0d;     if (b < 0.0d) b = 0.0d;
        if (a > 1.0d) a = 1.0d;     if (a < 0.0d) a = 0.0d;

        return new Color(r, g, b, a);
    }

}
