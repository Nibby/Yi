package codes.nibby.yi.editor.gui.board;

import javafx.scene.shape.Rectangle;

/**
 * Manages the size parameters for drawing the game board.
 */
final class GameBoardSize {

    private double canvasWidth;
    private double canvasHeight;

    // Expressed as a percentage of total canvas width/height
    private final double percentageThicknessOfBoardBorder = 0.02d;
    private Rectangle boardBorderBounds;
    private Rectangle boardBounds;

    private double percentagePaddingForCoordinateLabels; // A percentage of board dimensions rather than total size
    private double pixelPaddingForCoordinateLabels;

    private Rectangle gridBounds;

    /**
     * These are percentages of gridBounds.
     */
    private double percentageStoneDiameter;
    private final double percentageStoneGap = 0.008d; // Space between two adjacent stones

    /**
     * Recalculates the board sizing using standard margins.
     *
     * @param componentWidth Width of the board canvas
     * @param componentHeight Height of the board canvas
     */
    void recalculate(double componentWidth, double componentHeight) {
        recalculate(componentWidth, componentHeight, 0.02d);
    }

    /**
     * Recalculates the board sizing using a custom margin. The margin is the space between the edges of the canvas to the start of the
     * game board.
     *
     * @param componentWidth Width of the board canvas
     * @param componentHeight Height of the board canvas
     * @param percentageMarginFromEdge Percentage of canvas component size dedicated to the margin
     */
    void recalculate(double componentWidth, double componentHeight, double percentageMarginFromEdge) {
        this.canvasWidth = componentWidth;
        this.canvasHeight = componentHeight;
        double lowestSize = Math.min(this.canvasWidth, canvasHeight);
        double marginSize = lowestSize * percentageMarginFromEdge;

        Rectangle stage = new Rectangle(0, 0, this.canvasWidth, canvasHeight);
        // TODO: What if vertical != horizontal for board size?
        //       This can happen if board size is not square, i.e. 15x3
        boardBorderBounds = center(stage, clip(0, 0, lowestSize, lowestSize, marginSize));
        boardBounds = center(stage, clip(boardBorderBounds, percentageThicknessOfBoardBorder * lowestSize));

        // TODO: Make this configurable and actually draw it.
//        percentagePaddingForCoordinateLabels = 0.1d;
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
     * @return The amount of pixels between the edge of the game board image and the start of the grids.
     */
    public double getPaddingForCoordinateLabels() {
        return pixelPaddingForCoordinateLabels;
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
    public double getPixelThicknessOfBoardBorder() {
        return percentageThicknessOfBoardBorder;
    }

    /**
     * Calculates the bounds of the component if it is centered relative to the container.
     *
     * @param container The container to center within
     * @param component The component to center
     * @return The bounds of the component after it is centered
     */
    private Rectangle center(Rectangle container, Rectangle component) {
        double x = container.getX() + container.getWidth() / 2 - component.getWidth() / 2;
        double y = container.getY() + container.getHeight() / 2 - component.getHeight() / 2;
        return new Rectangle(x, y, component.getWidth(), component.getHeight());
    }

    /**
     * Trims the given bounds on all four sides by an inset. The result is a smaller rectangle
     * centered within bounds.
     *
     * @param bounds The bounds to trim
     * @param insets Amount to trim from all sides on the bounds, in pixel units
     * @return The clipped bounds
     */
    private Rectangle clip(Rectangle bounds, double insets) {
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
    private Rectangle clip(double x, double y, double w, double h, double insets) {
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
    private Rectangle clip(double x, double y, double w, double h, double insetLeft, double insetTop, double insetRight, double insetBottom) {
        double newX, newY, newWidth, newHeight;

        newX = x + insetLeft;
        newY = y + insetTop;
        newWidth = w - (insetLeft + insetRight);
        newHeight = h - (insetTop + insetBottom);

        return new Rectangle(newX, newY, newWidth, newHeight);
    }
}
