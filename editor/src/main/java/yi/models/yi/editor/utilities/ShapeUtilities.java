package yi.models.yi.editor.utilities;

import javafx.scene.shape.Rectangle;

/**
 * A collection of utilities that manipulate 2D geometry objects.
 */
public final class ShapeUtilities {

    private ShapeUtilities() { }

    /**
     * Calculates the bounds of the component if it is centered relative to the container.
     *
     * @param container The container to center within
     * @param component The component to center
     * @return The bounds of the component after it is centered
     */
    public static Rectangle center(Rectangle container, Rectangle component) {
        double x = container.getX() + container.getWidth() / 2 - component.getWidth() / 2;
        double y = container.getY() + container.getHeight() / 2 - component.getHeight() / 2;
        return new Rectangle(x, y, component.getWidth(), component.getHeight());
    }

    /**
     * Finds the largest rectangle with the given width to height ratio that can fit inside the container.
     *
     * @param container The container to fit the rectangle within
     * @param targetWidthToHeightRatio The width to height ratio of the fitted rectangle
     * @param percentageInsets Amount of margin for the fitted rectangle on each side, expressed as a percentage of the shorter side of the container.
     *                         For example, 0.05d (5%) on a 100x200 container means a margin of 5px on all sides of the fitted rectangle.
     * @return The largest rectangle within the container that complies with the target width to height ratio
     */
    public static Rectangle centerFit(Rectangle container, double targetWidthToHeightRatio, double percentageInsets) {
        boolean containerWidthWider = container.getWidth() > container.getHeight();
        boolean fitWidthWider = targetWidthToHeightRatio > 1;

        double fitInsets;

        if (containerWidthWider) {
            fitInsets = container.getHeight() * percentageInsets;
        } else {
            fitInsets = container.getWidth() * percentageInsets;
        }

        double fitWidth, fitHeight;
        double scaleFactor;

        if (fitWidthWider) {
            // If fit by width, component size is constrained by height
            fitWidth = container.getWidth();
            fitHeight = fitWidth / targetWidthToHeightRatio;

            scaleFactor = fitHeight / container.getHeight();
        } else {
            // If fit by height, component size is constrained by width
            fitHeight = container.getHeight();
            fitWidth = fitHeight * targetWidthToHeightRatio;

            scaleFactor = fitWidth / container.getWidth();
        }

        if (scaleFactor > 1d) {
            fitWidth /= scaleFactor;
            fitHeight /= scaleFactor;
        }

        double fitX = container.getX() + container.getWidth() / 2 - fitWidth / 2;
        double fitY = container.getY() + container.getHeight() / 2 - fitHeight / 2;

        return new Rectangle(fitX + fitInsets, fitY + fitInsets, fitWidth - 2 * fitInsets, fitHeight - 2 * fitInsets);
    }

    /**
     * Trims the given bounds on all four sides by an inset. The result is a smaller rectangle
     * centered within bounds.
     *
     * @param bounds The bounds to trim
     * @param insets Amount to trim from all sides on the bounds, in pixel units
     * @return The clipped bounds
     */
    public static Rectangle clip(Rectangle bounds, double insets) {
        return clip(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), insets);
    }

    /**
     * Trims the given bounds on all four sides by an inset. The result is a smaller rectangle
     * centered within bounds.
     *
     * @param x x position of the bound rectangle
     * @param y y position of the bound rectangle
     * @param w width of the bound rectangle
     * @param h height of the bound rectangle
     * @param insets Amount to trim from all sides on the bounds, in pixel units
     * @return The clipped bounds
     */
    public static Rectangle clip(double x, double y, double w, double h, double insets) {
        return clip(x, y, w, h, insets, insets, insets, insets);
    }

    /**
     * Trims the given bounds on each side by a custom inset. The result is a smaller rectangle.
     * </p>
     * The rectangle is not guaranteed to be centered within the bounds if the amount trimmed on each side is not identical.
     *
     * @param x x position of the bound rectangle
     * @param y y position of the bound rectangle
     * @param w width of the bound rectangle
     * @param h height of the bound rectangle
     * @param insetLeft Amount to trim from left of the rectangle, in pixel units
     * @param insetTop Amount to trim from top of the rectangle, in pixel units
     * @param insetRight Amount to trim from right of the rectangle, in pixel units
     * @param insetBottom Amount to trim from bottom of the rectangle, in pixel units
     * @return The clipped bounds
     */
    public static Rectangle clip(double x, double y, double w, double h, double insetLeft, double insetTop, double insetRight, double insetBottom) {
        double newX, newY, newWidth, newHeight;

        newX = x + insetLeft;
        newY = y + insetTop;
        newWidth = w - (insetLeft + insetRight);
        newHeight = h - (insetTop + insetBottom);

        return new Rectangle(newX, newY, newWidth, newHeight);
    }

}
