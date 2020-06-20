package codes.nibby.yi.gui.board;

import javafx.scene.shape.Rectangle;

/**
 * Manages the size parameters for drawing the game board.
 */
final class GameBoardSize {

    private double canvasWidth;
    private double canvasHeight;

    private Rectangle boardBounds;

    private double percentagePaddingForCoordinateLabels; // A percentage of board dimensions rather than total size
    private double pixelPaddingForCoordinateLabels;

    private Rectangle gridBounds;

    // A percentage of grid bounds rather than total size
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
        boardBounds = center(stage, clip(0, 0, lowestSize, lowestSize, marginSize));

        // TODO: Make this configurable and actually draw it.
//        percentagePaddingForCoordinateLabels = 0.1d;
    }

    private Rectangle center(Rectangle container, Rectangle component) {
        // FIXME: It doesn't take into account component.x or component.y when centering
        double x = container.getX() + container.getWidth() / 2 - component.getWidth() / 2;
        double y = container.getY() + container.getHeight() / 2 - component.getHeight() / 2;
        return new Rectangle(x, y, component.getWidth(), component.getHeight());
    }

    private Rectangle clip(Rectangle bounds, double insets) {
        return clip(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), insets);
    }

    private Rectangle clip(double x, double y, double w, double h, double insets) {
        return clip(x, y, w, h, insets, insets, insets, insets);
    }

    private Rectangle clip(double x, double y, double w, double h, double insetLeft, double insetTop, double insetRight, double insetBottom) {
        double newX, newY, newWidth, newHeight;

        newX = x + insetLeft;
        newY = y + insetTop;
        newWidth = w - (insetLeft + insetRight);
        newHeight = h - (insetTop + insetBottom);

        return new Rectangle(newX, newY, newWidth, newHeight);
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
}
