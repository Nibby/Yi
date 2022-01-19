package codes.nibby.yi.app.utilities;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Collection of methods to work with images used for component icons.
 */
public final class IconUtilities {

    private IconUtilities() {

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
    public static Image flatColorSwap(Image originalIcon, int r, int g, int b) {
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
