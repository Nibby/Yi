package codes.nibby.yi.app.components.board;

import javafx.scene.shape.Rectangle;
import codes.nibby.yi.app.utilities.ShapeUtilities;

import java.util.Optional;

/**
 * Manages the size parameters for drawing the game board. Properties in this class are
 * calculated based on the dimensions of the game board component.
 * value.
 */
public final class GameBoardSize {

    /*
     * Internally, distances between regions are measured in percentages.
     * The percentage is calculated from the shorter side of a rectangular boundary
     * (Math.min(width, height) * percentage).
     */

    private Rectangle stageBounds;

    // Nested within stageBounds, used to draw the board background
    private LayoutRectangle boardBounds;

    // Nested within boardBounds, used to draw coordinate labels after a gap from the edge
    private LayoutRectangle coordinateLabelBounds;

    // Nested within coordinateLabelBounds, used to draw board intersections after a gap
    private Rectangle gridBounds;

    // These 'double' are percentages values derived from gridBounds.
    // Remember it is calculated from the shorter side of the dimensions.

    // Gap size between adjacent stones.
    private double stoneGapSize;
    // Diameter of one stone on this board, always square.
    private double stoneSize;
    // Size of the stone + gap at each intersection, always square.
    private double gridUnitSize;

    // Fx shadow parameters when drawing each stone, if going for a realism style.
    // Otherwise, these parameters are optional.
    private double stoneShadowRadius;
    private double stoneShadowOffset;

    // Cached values from last compute call
    private boolean cachedOnce = false;
    private double cachedComponentWidth;
    private double cachedComponentHeight;
    private int cachedBoardWidth;
    private int cachedBoardHeight;

    GameBoardSize() { }

    /**
     * Recalculates the board sizing assuming the total drawable region has not
     * changed. This method is used when internal drawable elements are toggled
     * on or off, which causes a change in sizing of other elements.
     * <p/>
     * This method requires {@link #compute(double, double, int, int, CoordinateLabelPosition)}
     * be called at least once.
     *
     * @param newLabelPosition New coordinate label positions.
     */
    void recompute(CoordinateLabelPosition newLabelPosition) {
        if (!cachedOnce) {
            throw new IllegalStateException("compute() must be invoked once first");
        }

        computeImpl(
            cachedComponentWidth,
            cachedComponentHeight,
            cachedBoardWidth,
            cachedBoardHeight,
            newLabelPosition,
            false
        );
    }

    /**
     * Recalculates the board sizing using a custom margin. The margin is the space between
     * the edges of the canvas to the start of the game board.
     *
     * @param componentWidth Width of the board canvas
     * @param componentHeight Height of the board canvas
     * @param boardWidth Number of board intersections horizontally
     * @param boardHeight Number of board intersections vertically
     */
    void compute(
        double componentWidth,
        double componentHeight,
        int boardWidth,
        int boardHeight,
        CoordinateLabelPosition labelPos
    ) {
        computeImpl(
            componentWidth,
            componentHeight,
            boardWidth,
            boardHeight,
            labelPos,
            true
        );
    }

    private void computeImpl(
        double componentWidth,
        double componentHeight,
        int boardWidth,
        int boardHeight,
        CoordinateLabelPosition coordinateLabelPosition,
        boolean cacheResult
    ) {
        stageBounds = new Rectangle(0, 0, componentWidth, componentHeight);

        // Start by approximating a reasonable size of the go board based on the total
        // intersection width-to-height ratio, then we find the maximum rectangle inside
        // the stage of such ratio, which serves as the basis of the grid intersections.
        // This should give us a good starting point for estimating a square stone size
        // and board scale appropriate for the current stage dimensions.
        //
        // It is a lot easier to preserve rectangle proportions incrementally
        // (i.e. by adding spacing around an existing known size) than to subtract from
        // an existing component, we adopt the former approach. First we define the correct
        // ratio of the board intersection area (which ensures the stone size is square),
        // then we expand from this region to obtain co-ordinate label bounds, board bounds,
        // board border bounds etc. The sizing of these boundaries will overflow the viewport
        // (stage) at this point, but we will re-scale them at the final step, making the
        // top-level container fit within the stage (and rescale all inner components based
        // on the scale value).

        // We need to calculate a scaled version of stone size at this point to maintain
        // the correct board ratio
        var aspectRatio = (double) boardWidth / (double) boardHeight;
        Rectangle gridFitRatio = ShapeUtilities.centerFit(stageBounds, aspectRatio, 0d);

        // Doesn't really matter the size of the grid bounds at this point, the proportion
        // is what matters. We will scale everything to the correct size later.
        var scaledGridBounds = new LayoutRectangle(gridFitRatio.getWidth(), gridFitRatio.getHeight());
        var scaledStoneSize = (scaledGridBounds.getWidth() / boardWidth + scaledGridBounds.getHeight() / boardHeight) / 2;
        var stoneArea = scaledGridBounds.createParentWithMargin(scaledStoneSize);
        coordinateLabelBounds = computeCoordinateLabelBounds(stoneArea, scaledStoneSize, coordinateLabelPosition);

        boardBounds = coordinateLabelBounds.createParentWithMargin(getPixelValue(0.01d, coordinateLabelBounds));
        // Expressed as a percentage of total canvas width/height
        // Rescale everything. All the objects are referenced internally by
        // LayoutRectangle, so the entire hierarchy will be updated. These are percentages
        // of the shorter side of board dimensions rather than total size
        double percentageMarginFromEdge = 0.02d;
        double marginFromEdgeInPixels = percentageMarginFromEdge
                * Math.min(stageBounds.getWidth(), stageBounds.getHeight());
        boardBounds.rescaleAndFinalize(stageBounds.getX(), stageBounds.getY(),
                stageBounds.getWidth(), stageBounds.getHeight(), marginFromEdgeInPixels, 1.0d, true);

        // Now we can finally calculate the correct stone size
        double stoneSizeFromWidth = scaledGridBounds.getWidth() / ((boardWidth == 1) ? 1 : (boardWidth - 1));
        double stoneSizeFromHeight = scaledGridBounds.getHeight() / ((boardHeight == 1) ? 1 : (boardHeight - 1));

        gridUnitSize = Math.min(stoneSizeFromWidth, stoneSizeFromHeight);
        // Space between two adjacent stones, expressed as a percentage of grid unit size
        double percentageStoneGap = 0.01d;
        stoneGapSize = gridUnitSize * percentageStoneGap;
        stoneSize = gridUnitSize - stoneGapSize;

        // Now that we have the final stone size, we may need to clip from the original
        // scaled grid bounds to center the intersections, because the first round of
        // calculation was a rough estimate.
        double actualGridWidth = gridUnitSize * (boardWidth - 1);
        double actualGridHeight = gridUnitSize * (boardHeight - 1);

        double excessGridWidth = scaledGridBounds.getWidth() - actualGridWidth;
        double excessGridHeight = scaledGridBounds.getHeight() - actualGridHeight;

        double horizontalClip = excessGridWidth / 2;
        double verticalClip = excessGridHeight / 2;

        gridBounds = ShapeUtilities.clip(scaledGridBounds.getX(), scaledGridBounds.getY(),
                scaledGridBounds.getWidth(), scaledGridBounds.getHeight(),
                horizontalClip, verticalClip, horizontalClip, verticalClip);

        stoneShadowRadius = stoneSize / 8d;
        stoneShadowOffset = stoneSize / 16d;

        if (cacheResult) {
            cacheParameters(componentWidth, componentHeight, boardWidth, boardHeight);
        }
    }

    private void cacheParameters(
        double componentWidth,
        double componentHeight,
        int boardWidth,
        int boardHeight
    ) {
        this.cachedComponentWidth = componentWidth;
        this.cachedComponentHeight = componentHeight;
        this.cachedBoardWidth = boardWidth;
        this.cachedBoardHeight = boardHeight;

        this.cachedOnce = true;
    }

    /*
        Determines how the grid boundaries are arranged within the game board based on the
        co-ordinate label orientation.
     */
    private LayoutRectangle computeCoordinateLabelBounds(
        LayoutRectangle gridBounds,
        double stoneSize,
        CoordinateLabelPosition lbPos
    ) {
        double marginLeft = 0, marginTop = 0, marginBottom = 0, marginRight = 0;
        LayoutRectangle.ContainerStrategy containerStrategy;

        if (lbPos == CoordinateLabelPosition.NONE) {
            return gridBounds;
        } else if (lbPos == CoordinateLabelPosition.TOP_AND_LEFT) {
            containerStrategy = LayoutRectangle.ContainerStrategy.CLAMP_BOTTOM_RIGHT;
            marginBottom = stoneSize;
            marginRight = stoneSize;
        } else if (lbPos == CoordinateLabelPosition.BOTTOM_AND_RIGHT) {
            containerStrategy = LayoutRectangle.ContainerStrategy.CLAMP_TOP_LEFT;
            marginTop = stoneSize;
            marginLeft = stoneSize;
        } else if (lbPos == CoordinateLabelPosition.ALL_SIDES) {
            marginTop = stoneSize;
            marginLeft = stoneSize;
            marginBottom = stoneSize;
            marginRight = stoneSize;
            containerStrategy = LayoutRectangle.ContainerStrategy.CENTER;
        } else {
            throw new IllegalStateException("Unimplemented coordinate label position: " + lbPos);
        }

        return gridBounds.createParentWithMargin(marginLeft, marginTop, marginRight,
                marginBottom, containerStrategy);
    }

    /**
     *
     * @return Total allocated game board drawing region (dimensions of board canvas).
     */
    public Rectangle getStageBounds() {
        return stageBounds;
    }

    /**
     * This value is optional for stones that want a 2D-feel.
     *
     * @return Fx drop shadow radius for each stone.
     */
    public double getStoneShadowRadius() {
        return stoneShadowRadius;
    }

    /**
     * This value is optional for stones that want a 2D-feel.
     *
     * @return Fx shadow offset for each stone.
     */
    public double getStoneShadowOffset() {
        return stoneShadowOffset;
    }

    /**
     * Retrieves the canvas drawing co-ordinates for a stone located in the
     * (gridX, gridY) intersection, where (0, 0) represents the top left
     * intersection.
     * <p/>
     * Result is an array of size 2 representing the top-left point to start
     * drawing a stone:
     * <pre> {@code
     * double[] pos = getStoneRenderPosition(0, 0);
     * double x = pos[0];
     * double y = pos[1];
     * double stoneSize = getStoneSizeInPixels();
     * graphics.fillOval(x, y, stoneSize, stoneSize);
     * } </pre>
     *
     * @param gridX Intersection X position
     * @param gridY Intersection Y position
     * @return Drawing co-ordinates of a stone located at (gridX, gridY) intersection.
     */
    public double[] getStoneRenderPosition(int gridX, int gridY) {
        return getGridRenderPosition(gridX, gridY, getStoneSizeInPixels());
    }

    /**
     * 
     * @see #getGridRenderPosition(int, int, double, double)
     */
    public double[] getGridRenderPosition(int gridX, int gridY, double objectSize) {
        return getGridRenderPosition(gridX, gridY, objectSize, objectSize);
    }

    /**
     * Returns the canvas drawing position of some object located at the intersection
     * {@code (gridX, gridY) } where (0, 0) represents the top left intersection
     * on the board.
     * <p/>
     * The returned co-ordinate adjust to the dimensions of the object such that
     * drawing it will make the board intersection the center point of the object.
     * <p/>
     * The result is an array of size 2 representing the top-left point to start
     * drawing the object:
     * <pre> {@code
     * double[] pos = getGridRenderPosition(0, 0, 16, 24);
     * double x = pos[0];
     * double y = pos[1];
     * // Draw the object...
     * } </pre>
     * <p/>
     * To convert a drawing position to logical board position, see
     * {@link #getGridPosition(double, double)}
     *
     * @param gridX Intersection X position
     * @param gridY Intersection Y position
     * @param objectWidth The object width in pixels
     * @param objectHeight The object height in pixels
     * @return Drawing co-ordinates of the object such that it is drawn centered
     *         on the intersection.
     */
    public double[] getGridRenderPosition(
        int gridX,
        int gridY,
        double objectWidth,
        double objectHeight
    ) {
        Rectangle gridBounds = getGridBounds();

        double x = gridBounds.getX() + getGridUnitSizeInPixels() * gridX - objectWidth / 2;
        double y = gridBounds.getY() + getGridUnitSizeInPixels() * gridY - objectHeight / 2;

        return new double[] { x, y };
    }

    /**
     * From a given render (or mouse) position, returns the logical board intersection
     * the position corresponds to.
     *
     * @param x Drawn x position
     * @param y Drawn y position
     * @return The logical board intersection represented by the provided draw position,
     *         or {@link Optional#empty()} if the position is not within grid boundaries.
     */
    public Optional<int[]> getGridPosition(double x, double y) {
        Rectangle gridBounds = getGridBounds();

        double normalizedX = x - gridBounds.getX();
        double normalizedY = y - gridBounds.getY();

        if (normalizedX < -getGridUnitSizeInPixels() / 2
                || normalizedY < -getGridUnitSizeInPixels() / 2
                || normalizedX > gridBounds.getWidth() + getGridUnitSizeInPixels() / 2
                || normalizedY > gridBounds.getHeight() + getGridUnitSizeInPixels() / 2) {
            return Optional.empty();
        }

        int gridX = (int) Math.round(normalizedX / getGridUnitSizeInPixels());
        int gridY = (int) Math.round(normalizedY / getGridUnitSizeInPixels());

        int[] gridPosition = new int[] { gridX, gridY };

        return Optional.of(gridPosition);
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
     * @return The drawing boundaries for the game co-ordinates
     */
    public Rectangle getCoordinateLabelBounds() {
        return coordinateLabelBounds;
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
     * @return The blur factor of the shadow effect used to paint board component border
     */
    public double getShadowRadius() {
        final double percentageShadowBlurRadius = 0.05d;
        return percentageShadowBlurRadius * getBoardBoundsPercentageMetric();
    }

    /**
     *
     * @return The amount of offset in both x and y direction of the board border shadow
     */
    public double getBoardBorderShadowOffsetInPixels() {
        double percentageBoardShadowOffset = 0.012d;
        return percentageBoardShadowOffset * getBoardBounds().getHeight();
    }

    /**
     *
     * @return The side of {@link #boardBounds} used to calculate percentages related to
     * the board component.
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
        double percentageGridLineThickness = 0.00125d;
        double thickness = percentageGridLineThickness * getBoardBoundsPercentageMetric();
        return Math.max(1d, thickness);
    }

    /**
     *
     * @return Pixel length of the gap between adjacent stones.
     */
    public double getStoneGapSizeInPixels() {
        return stoneGapSize;
    }

    /**
     *
     * @return Pixel length of one stone + gap size.
     */
    public double getGridUnitSizeInPixels() {
        return gridUnitSize;
    }

    private double getPixelValue(double percentage, Rectangle rectangle) {
        return percentage * Math.min(rectangle.getWidth(), rectangle.getHeight());
    }

    /**
     *
     * @return true if {@link #compute(double, double, int, int, CoordinateLabelPosition)}
     *         has been called at least once.
     */
    public boolean hasComputedOnce() {
        return cachedOnce;
    }

    /**
     * A rectangle with one child hierarchy. The child rectangle is assumed to be fully
     * contained within this rectangle. When the parent rectangle is rescaled, the child
     * will also be rescaled accordingly.
     * <p/>
     * Each rectangle should have a (x, y) position at (0, 0).
     */
    private static class LayoutRectangle extends Rectangle {

        enum ContainerStrategy {
            CENTER,
            CLAMP_BOTTOM_RIGHT,
            CLAMP_TOP_LEFT
        }

        private LayoutRectangle child;

        private double marginLeft = 0;
        private double marginTop = 0;
        private double marginRight = 0;
        private double marginBottom = 0;
        private ContainerStrategy containerStrategy = ContainerStrategy.CENTER;

        private boolean finalized = false;

        public LayoutRectangle(double width, double height) {
            super(width, height);
        }

        /**
         *
         * @see #createParentWithMargin(double, double, double, double, ContainerStrategy)
         */
        public LayoutRectangle createParentWithMargin(double marginOnAllSides) {
            return createParentWithMargin(marginOnAllSides, marginOnAllSides,
                    marginOnAllSides, marginOnAllSides);
        }

        /**
         * 
         * @see #createParentWithMargin(double, double, double, double, ContainerStrategy)
         */
        public LayoutRectangle createParentWithMargin(double marginLeft, double marginTop,
                                                      double marginRight, double marginBottom) {
            return createParentWithMargin(marginLeft, marginTop, marginRight, marginBottom,
                    ContainerStrategy.CENTER);
        }

        /**
         * Create and return a parent for this rectangle whose child is set to this instance.
         * The parent will fully contain the child rectangle and have its position starting
         * at (0, 0). The child position will be updated with the margin data depending on the
         * container strategy.
         *
         * @param marginLeft Space to the left of the child before reaching left edge of parent
         * @param marginTop Space above the child before reaching top edge of parent
         * @param marginRight Space to the right of the child before reaching the right edge of parent
         * @param marginBottom Space to the bottom of the child before reaching the bottom edge of parent
         * @param strategy How the child fits inside the parent. It is set to
         *                 {@link ContainerStrategy#CENTER} by default, which means the
         *                 child will be centered in the parent bounds.
         *
         * @return The parent rectangle for this instance.
         */
        private LayoutRectangle createParentWithMargin(double marginLeft, double marginTop,
                                                       double marginRight, double marginBottom,
                                                       ContainerStrategy strategy) {
            assertNotFinalized();

            this.marginLeft = marginLeft;
            this.marginTop = marginTop;
            this.marginRight = marginRight;
            this.marginBottom = marginBottom;
            this.containerStrategy = strategy;

            var parent = new LayoutRectangle(getWidth() + marginLeft + marginRight, getHeight() + marginTop + marginBottom);

            switch (strategy) {
                case CENTER:
                    offsetContents(marginLeft, marginTop);
                    break;
                case CLAMP_BOTTOM_RIGHT:
                    offsetContents(marginLeft + marginRight, marginTop + marginBottom);
                    break;
                case CLAMP_TOP_LEFT:
                    // No offset
                    break;
                default:
                    throw new IllegalStateException("Unsupported container strategy: " + strategy);
            }

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
         * Scales all the contents within this rectangle to an appropriate size while
         * preserving width to height ratio and centers the scaled version inside the
         * component bounds.
         * <p/>
         * Additionally, marks this rectangle and its entire sub-hierarchy as finalized.
         * This means no more changes can be made to this rectangle and its hierarchy
         * after this operation.
         *
         * @param containerX Container bounds to center this component within
         * @param containerY Container bounds to center this component within
         * @param containerWidth Container bounds to center this component within
         * @param containerHeight Container bounds to center this component within
         * @param parentScale Scale used to transform parent rectangle dimensions
         */
        public void rescaleAndFinalize(double containerX, double containerY,
                                       double containerWidth, double containerHeight,
                                       double margin, double parentScale,
                                       boolean firstIteration) {
            assertNotFinalized();

            double originalWidth = getWidth();
            double widthToHeightRatio = getWidth() / getHeight();

            this.marginLeft *= parentScale;
            this.marginTop *= parentScale;
            this.marginRight *= parentScale;
            this.marginBottom *= parentScale;

            double fitContainerX = containerX + marginLeft + margin;
            double fitContainerY = containerY + marginTop + margin;
            double fitContainerWidth = containerWidth - (marginLeft + marginRight) - margin * 2;
            double fitContainerHeight = containerHeight - (marginTop + marginBottom) - margin * 2;
            Rectangle fitContainerBounds = new Rectangle(fitContainerX, fitContainerY, fitContainerWidth, fitContainerHeight);

            Rectangle fitBounds = ShapeUtilities.centerFit(fitContainerBounds, widthToHeightRatio, 0);
            double fitScale = fitBounds.getWidth() / originalWidth;

            setWidth(fitBounds.getWidth());
            setHeight(fitBounds.getHeight());

            switch (containerStrategy) {
                case CENTER:
                    setX(fitBounds.getX());
                    setY(fitBounds.getY());
                    break;
                case CLAMP_TOP_LEFT:
                    // If first iteration, it will clamp to (0, 0) of the stage if we don't snap to fitBounds.
                    if (firstIteration) {
                        setX(fitBounds.getWidth());
                        setY(fitBounds.getHeight());
                    } else {
                        setX(containerX);
                        setY(containerY);
                    }
                    break;
                case CLAMP_BOTTOM_RIGHT:
                    setX(containerX + containerWidth - fitBounds.getWidth());
                    setY(containerY + containerHeight - fitBounds.getHeight());
                    break;
                default:
                    throw new IllegalStateException("Unsupported container strategy: " + containerStrategy);
            }

            finalized = true;

            if (child != null) {
                child.rescaleAndFinalize(getX(), getY(), this.getWidth(), this.getHeight(), 0, fitScale, false);
            }
        }

        private void assertNotFinalized() {
            assert !finalized : "Cannot modify layout rectangle after it has been finalized!";
        }
    }
}