package codes.nibby.yi.editor.gui.board;

import codes.nibby.yi.editor.utilities.ComparisonUtilities;
import javafx.scene.shape.Rectangle;

/**
 * Manages the size parameters for drawing the game board. Properties in this class are usually dynamically calculated depending on
 * the dimension of the game board canvas. For that reason, many properties use a hard-coded percentage rather than some constant value.
 *
 * The percentage is usually taken from the shorter side of a boundary (i.e. {@code Math.min(a, b) * percentage}.
 */
final class GameBoardSize {

    private double canvasWidth;
    private double canvasHeight;

    // Expressed as a percentage of total canvas width/height
    private final double percentageThicknessOfBoardBorder = 0.02d;
    private Rectangle boardBorderBounds;
    private Rectangle boardBounds;

    // These are percentages of the shorter side of board dimensions rather than total size
    private final double percentageShadowBlurRadius = 0.015d;
    private final double percentageShadowOffset = 0.012d;
    private final double percentagePaddingForCoordinateLabels = 0.02d;
    private final double percentagePaddingBetweenLabelsAndGrid = 0.02d;
    private Rectangle coordinateLabelBounds;
    private Rectangle gridBounds;
    private final double percentageGridLineThickness = 0.00125d;

    // These are percentages of gridBounds.
    // TODO: Properly implement stone rendering and size calculation
    private double percentageStoneDiameter;
    private final double percentageStoneGap = 0.008d; // Space between two adjacent stones
    private double stoneSize;

    /**
     * Recalculates the board sizing using standard margins.
     *
     * @param componentWidth Width of the board canvas
     * @param componentHeight Height of the board canvas
     * @param gridWidth Number of board intersections horizontally
     * @param gridHeight Number of board intersections vertically
     */
    void recalculate(double componentWidth, double componentHeight, int gridWidth, int gridHeight) {
        recalculate(componentWidth, componentHeight, gridWidth, gridHeight, 0.02d);
    }

    /**
     * Recalculates the board sizing using a custom margin. The margin is the space between the edges of the canvas to the start of the
     * game board.
     *
     * @param componentWidth Width of the board canvas
     * @param componentHeight Height of the board canvas
     * @param gridWidth Number of board intersections horizontally
     * @param gridHeight Number of board intersections vertically
     * @param percentageMarginFromEdge Percentage of canvas component size dedicated to the margin
     */
    void recalculate(double componentWidth, double componentHeight, int gridWidth, int gridHeight, double percentageMarginFromEdge) {
        this.canvasWidth = componentWidth;
        this.canvasHeight = componentHeight;

        double gridWidthToHeightRatio = (double) gridWidth / (double) gridHeight;

        double lowestSize = Math.min(this.canvasWidth, canvasHeight);
        double marginSize = lowestSize * percentageMarginFromEdge;

        Rectangle stage = new Rectangle(0, 0, this.canvasWidth, canvasHeight);

        // Fit the largest bounds of the same aspect ratio as (gridWidth x gridHeight) to ensure grids can be drawn as perfect squares
        // Board content size
        Rectangle boardContainerBounds = centerFit(stage, gridWidthToHeightRatio, percentageMarginFromEdge);
        boardBorderBounds = center(boardContainerBounds, clip(0, 0, lowestSize, lowestSize, marginSize));
        boardBounds = center(boardContainerBounds, clip(boardBorderBounds, percentageThicknessOfBoardBorder * lowestSize));

        // Coordinate labels
        // TODO: Depending on the co-ordinate label position, the padding size may vary
        //       Finalise this later
        coordinateLabelBounds = center(boardBounds, clip(boardBounds, getPaddingForCoordinateLabelsInPixels()));

        // TODO: This may not be at the center of the coordinate bounds if the labels are not showing in all 4 sides
        double pixelPaddingBetweenLabelsAndGrid = percentagePaddingBetweenLabelsAndGrid * Math.min(coordinateLabelBounds.getWidth(), coordinateLabelBounds.getHeight());
        gridBounds = center(coordinateLabelBounds, clip(coordinateLabelBounds, pixelPaddingBetweenLabelsAndGrid));

        // Check that the stone size is (approximately) square
        double stoneWidth = gridBounds.getWidth() / gridWidth;
        double stoneHeight = gridBounds.getHeight() / gridHeight;

        assert ComparisonUtilities.doubleEquals(stoneWidth, stoneHeight);

        stoneSize = (stoneWidth + stoneHeight) / 2;
    }

    /**
     *
     * @return The drawing boundaries for the game board image
     */
    public Rectangle getBoardBounds() {
        return boardBounds;
    }

    /**
     *
     * @return The drawing boundaries for the border around the game board image
     */
    public Rectangle getBoardBorderBounds() {
        return boardBorderBounds;
    }

    /**
     *
     * @return The drawing boundaries for the grid of intersections.
     */
    public Rectangle getGridBounds() {
        return gridBounds;
    }

    /**
     *
     * @return The drawing boundaries for the entire canvas component
     */
    public Rectangle getCanvasBounds() {
        return new Rectangle(0, 0, canvasWidth, canvasHeight);
    }

    /**
     *
     * @return The thickness of the border around the game board in pixel units.
     */
    public double getThicknessOfBoardBorderInPixels() {
        return percentageThicknessOfBoardBorder;
    }

    /**
     *
     * @return The amount of pixels between the edge of the game board image and the start of the grids.
     */
    public double getPaddingForCoordinateLabelsInPixels() {
        return percentagePaddingForCoordinateLabels * getBoardBoundsPercentageMetric();
    }

    /**
     *
     * @return The blur factor of the shadow effect used to paint board component border
     */
    public double getShadowRadius() {
        return percentageShadowBlurRadius * getBoardBoundsPercentageMetric();
    }

    /**
     *
     * @return The amount of x-offset on the board component border shadow
     */
    public double getShadowOffsetXInPixels() {
        return percentageShadowOffset * getBoardBounds().getWidth();
    }

    /**
     *
     * @return The amount of y-offset on the board component border shadow
     */
    public double getShadowOffsetYInPixels() {
        return percentageShadowOffset * getBoardBounds().getHeight();
    }

    /**
     *
     * @return The side of {@link #boardBounds} used to calculate percentages related to the board component.
     */
    private double getBoardBoundsPercentageMetric() {
        return Math.min(boardBounds.getWidth(), boardBounds.getHeight());
    }

    /**
     *
     * @return The square dimension of a rendered Go stone, in pixels.
     */
    public double getStoneSizeInPixels() {
        return stoneSize;
    }

    /**
     *
     * @return The thickness, in pixel units, of the line used to draw the board grids.
     */
    public double getGridLineThicknessInPixels() {
        return percentageGridLineThickness * getBoardBoundsPercentageMetric();
    }

    /**
     * Calculates the bounds of the component if it is centered relative to the container.
     *
     * @param container The container to center within
     * @param component The component to center
     * @return The bounds of the component after it is centered
     */
    static Rectangle center(Rectangle container, Rectangle component) {
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
    static Rectangle centerFit(Rectangle container, double targetWidthToHeightRatio, double percentageInsets) {
        boolean containerWidthWider = container.getWidth() > container.getHeight();
        boolean fitWidthWider = targetWidthToHeightRatio > 1;

        double containerWidth = container.getWidth() - container.getX();
        double containerHeight = container.getHeight() - container.getY();

        if (containerWidth < ComparisonUtilities.EPSILON || containerHeight < ComparisonUtilities.EPSILON) {
            // Fail silently, cannot fit because size is too small.
            return new Rectangle(container.getX(), container.getY(), 0, 0);
        }

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
    static Rectangle clip(Rectangle bounds, double insets) {
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
    static Rectangle clip(double x, double y, double w, double h, double insets) {
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
    static Rectangle clip(double x, double y, double w, double h, double insetLeft, double insetTop, double insetRight, double insetBottom) {
        double newX, newY, newWidth, newHeight;

        newX = x + insetLeft;
        newY = y + insetTop;
        newWidth = w - (insetLeft + insetRight);
        newHeight = h - (insetTop + insetBottom);

        return new Rectangle(newX, newY, newWidth, newHeight);
    }
}
