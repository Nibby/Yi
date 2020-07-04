package codes.nibby.yi.editor.gui.board;

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
    private LayoutRectangle boardBorderBounds;
    private LayoutRectangle boardBounds;

    // These are percentages of the shorter side of board dimensions rather than total size
    private final double percentageShadowBlurRadius = 0.015d;
    private final double percentageShadowOffset = 0.012d;
    private final double percentagePaddingForCoordinateLabels = 0.02d;
    private final double percentagePaddingBetweenLabelsAndGrid = 0.02d;
    private LayoutRectangle coordinateLabelBounds;
    private Rectangle gridBounds;
    private LayoutRectangle stoneBounds;
    private final double percentageGridLineThickness = 0.00125d;

    // These are percentages of gridBounds.
    // TODO: Properly implement stone rendering and size calculation
    private double percentageStoneDiameter;
    private final double percentageStoneGap = 0.1d; // Space between two adjacent stones, expressed as a percentage of grid unit size
    private double stoneGapSize;
    private double stoneSize;
    private double gridUnitSize; // Size of the stone + gap at each intersection

    /**
     * Recalculates the board sizing using standard margins.
     *
     * @param componentWidth Width of the board canvas
     * @param componentHeight Height of the board canvas
     * @param gridWidth Number of board intersections horizontally
     * @param gridHeight Number of board intersections vertically
     */
    void compute(double componentWidth, double componentHeight, int gridWidth, int gridHeight) {
        compute(componentWidth, componentHeight, gridWidth, gridHeight, 0.02d);
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
    void compute(double componentWidth, double componentHeight, int gridWidth, int gridHeight, double percentageMarginFromEdge) {
        this.canvasWidth = componentWidth;
        this.canvasHeight = componentHeight;

        Rectangle stage = new Rectangle(0, 0, this.canvasWidth, canvasHeight);

        // Overview:
        // =========
        // Start by approximating a reasonable size of the go board based on the total intersection width-to-height ratio,
        // then we find the maximum rectangle inside the stage of such ratio, which serves as the basis of the grid intersections.
        // This should give us a good starting point for estimating a square stone size and board scale appropriate for the current
        // stage dimensions.
        //
        // It is a lot easier to preserve rectangle proportions incrementally (i.e. by adding spacing around an existing known size) than
        // than to subtract from an existing component, we adopt the former approach. First we define the correct ratio of the board
        // intersection area (which ensures the stone size is square), then we expand from this region to obtain co-ordinate label bounds,
        // board bounds, board border bounds etc. The sizing of these boundaries will overflow the viewport (stage) at this point, but
        // we will re-scale them at the final step, making the top-level container fit within the stage (and rescale all inner components based on the
        // scale value).

        // We need to calculate a scaled version of stone size at this point to maintain the correct board ratio
        Rectangle gridFitRatio = centerFit(stage, (double) gridWidth / (double) gridHeight, 0d);
        double scaledStoneSizeFromWidth = gridFitRatio.getWidth() / gridWidth;
        double scaledStoneSizeFromHeight = gridFitRatio.getHeight() / gridHeight;
        double scaledStoneSize = Math.min(scaledStoneSizeFromWidth, scaledStoneSizeFromHeight);

        LayoutRectangle scaledGridBounds = new LayoutRectangle(scaledStoneSize * gridWidth, scaledStoneSize * gridHeight);

        stoneBounds = scaledGridBounds.addParentWithMargin(scaledStoneSize);
        // TODO: Calculate coordinate label bounds
        boardBounds = stoneBounds; // TODO: Temporary until coordinate spaces are calculated properly
        boardBorderBounds = boardBounds.addParentWithMargin(percentageThicknessOfBoardBorder * Math.min(boardBounds.getWidth(), boardBounds.getHeight()));

        // Rescale everything. All the objects are referenced internally by LayoutRectangle, so the entire hierarchy will be updated
        double marginFromEdgeInPixels = percentageMarginFromEdge * Math.min(stage.getWidth(), stage.getHeight());
        boardBorderBounds.rescaleAndFinalize(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight(), marginFromEdgeInPixels, 1.0d);

        // Now we can finally calculate the correct stone size
        double stoneSizeFromWidth = scaledGridBounds.getWidth() / gridWidth;
        double stoneSizeFromHeight = scaledGridBounds.getHeight() / gridHeight;
        gridUnitSize = Math.min(stoneSizeFromWidth, stoneSizeFromHeight);
        stoneGapSize = gridUnitSize * percentageStoneGap;
        stoneSize = gridUnitSize - stoneGapSize;

        // Re-calculate the grid bounds using the square stone size
        // Unfortunately this will not be part of the layout rectangle hierarchy. But at this point
        // we should no longer have to use that anymore.
        gridBounds = center(boardBounds, new LayoutRectangle(gridUnitSize * (gridWidth - 1), gridUnitSize * (gridHeight - 1)));
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
        double thickness = percentageGridLineThickness * getBoardBoundsPercentageMetric();
        return Math.max(1d, thickness);
    }

    public double getStoneGapSizeInPixels() {
        return stoneGapSize;
    }

    public double getGridUnitSizeInPixels() {
        return gridUnitSize;
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

    /**
     * A rectangle with one child hierarchy. The child rectangle is assumed to be fully contained within this rectangle.
     * When the parent rectangle is rescaled, the child will also be rescaled accordingly.
     * <p/>
     * Each rectangle should have a (x, y) position at (0, 0).
     */
    private static class LayoutRectangle extends Rectangle {

        private LayoutRectangle child;

        private double marginLeft = 0;
        private double marginTop = 0;
        private double marginRight = 0;
        private double marginBottom = 0;

        private boolean finalized = false;

        public LayoutRectangle(double width, double height) {
            super(width, height);
        }

        public LayoutRectangle addParentWithMargin(double marginOnAllSides) {
            return addParentWithMargin(marginOnAllSides, marginOnAllSides, marginOnAllSides, marginOnAllSides);
        }

        // Creates a parent whose size is identical to the current rectangle and rescales existing content according to the margins
        public LayoutRectangle addParentWithMargin(double marginLeft, double marginTop, double marginRight, double marginBottom) {
            assertNotFinalized();

            this.marginLeft = marginLeft;
            this.marginTop = marginTop;
            this.marginRight = marginRight;
            this.marginBottom = marginBottom;

            var parent = new LayoutRectangle(getWidth() + marginLeft + marginRight, getHeight() + marginTop + marginBottom);
            offsetContents(marginLeft, marginTop);
            parent.child = this;

            return parent;
        }

        private void offsetContents(double x, double y) {
            assertNotFinalized();

            setX(getX() + x);
            setY(getY() + y);

            if (child != null) {
                child.offsetContents(x, y);
            }
        }

        /**
         * Scales all the contents within this rectangle to an appropriate size while preserving width to height ratio and
         * centers the scaled version inside the component bounds.
         * <p/>
         * Additionally, marks this rectangle and its entire sub-hierarchy as finalized. This means no more changes can
         * be made to this rectangle and its hierarchy after this operation.
         *
         * @param containerX Container bounds to center this component within
         * @param containerY Container bounds to center this component within
         * @param containerWidth Container bounds to center this component within
         * @param containerHeight Container bounds to center this component within
         * @param parentScale Scale used to transform parent rectangle dimensions
         */
        public void rescaleAndFinalize(double containerX, double containerY, double containerWidth, double containerHeight, double margin, double parentScale) {
            assertNotFinalized();

            double originalWidth = getWidth();
            double widthToHeightRatio = getWidth() / getHeight();

            double fitContainerX = containerX + marginLeft * parentScale + margin;
            double fitContainerY = containerY + marginTop * parentScale + margin;
            double fitContainerWidth = containerWidth - (marginLeft + marginRight) * parentScale - margin * 2;
            double fitContainerHeight = containerHeight - (marginTop + marginBottom) * parentScale - margin * 2;
            Rectangle fitContainerBounds = new Rectangle(fitContainerX, fitContainerY, fitContainerWidth, fitContainerHeight);

            Rectangle fitBounds = centerFit(fitContainerBounds, widthToHeightRatio, 0);
            double fitScale = fitBounds.getWidth() / originalWidth;

            setWidth(fitBounds.getWidth());
            setHeight(fitBounds.getHeight());
            setX(fitBounds.getX());
            setY(fitBounds.getY());

            finalized = true;

            if (child != null) {
                child.rescaleAndFinalize(getX(), getY(), this.getWidth(), this.getHeight(), 0, fitScale);
            }
        }

        private void assertNotFinalized() {
            assert !finalized : "Cannot modify layout rectangle after it has been finalized!";
        }
    }
}
