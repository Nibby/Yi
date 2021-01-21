package yi.component.shared.utilities;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.Optional;

/**
 * Collection of methods to work with images used for component icons.
 */
public final class IconUtilities {

    private IconUtilities() {

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
    public static Optional<ImageView> loadIcon(String resourceFile, Class<?> resourceClass) {
        return loadIcon(resourceFile, resourceClass, -1);
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
    public static Optional<ImageView> loadIcon(String resourceFile, Class<?> resourceClass, int fitSize) {
        return loadIcon(resourceFile, resourceClass, fitSize, fitSize);
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
    public static Optional<ImageView> loadIcon(String resourceFile, Class<?> resourceClass,
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
     * Re-colors the image, replacing all RGB data on the original image with
     * the supplied version. The opacity of each pixel is preserved.
     *
     * @param originalIcon Image to re-color
     * @param r Red element, 0 - 255
     * @param g Green element, 0 - 255
     * @param b Blue element, 0 - 255
     * @return Re-colored image
     */
    public static Image flatColor(Image originalIcon, int r, int g, int b) {
        int iconWidth = (int) originalIcon.getWidth();
        int iconHeight = (int) originalIcon.getHeight();
        var pixelReader = originalIcon.getPixelReader();
        var tintedImage = new WritableImage(iconWidth, iconHeight);
        var pixelWriter = tintedImage.getPixelWriter();

        for (int x = 0; x < iconWidth; ++x) {
            for (int y = 0; y < iconHeight; ++y) {
                Color original = pixelReader.getColor(x, y);
                Color flatColor = new Color((double) r / 255d, (double) g / 255d, (double) b / 255d, original.getOpacity());
                pixelWriter.setColor(x, y, flatColor);
            }
        }
        return tintedImage;
    }

}
