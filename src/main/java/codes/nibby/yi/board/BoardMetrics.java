package codes.nibby.yi.board;

import codes.nibby.yi.game.Game;

/**
 * A collection of variables that is used to calculate various component spacings
 * on the rendered go board.
 *
 * @author Kevin Yang
 * Created on 23 August 2019
 */
public class BoardMetrics {

    /**
     * Pixels reserved for overlaid components.
     */
    /*
        Allow some toolbars to be stacked on top of the game board
        for aesthetic purposes.
     */
    public static final int RESERVED_TOOLBAR_SIZE = 40;

    /**
     * Constant representing a top and left side board co-ordinate display.
     */
    public static final int LABEL_ORIENTATION_TOP_LEFT = 0;

    /**
     * Constant representing a top and right side board co-ordinate display.
     */
    public static final int LABEL_ORIENTATION_TOP_RIGHT = 1;

    /**
     * Constant representing a bottom and left side board co-ordinate display.
     */
    public static final int LABEL_ORIENTATION_BOTTOM_LEFT = 2;

    /**
     * Constant representing a bottom and right side board co-ordinate display.
     */
    public static final int LABEL_ORIENTATION_BOTTOM_RIGHT = 3;

    /**
     * Constant representing an all-sided board co-ordinate display.
     */
    public static final int LABEL_ORIENTATION_ALL = 4;

    // The order from which the numerical vertical coordinate column begins
    // ASCENDING = 1 -> 19 (on 19x19)
    // DESCENDING = 19 -> 1 (on 19x19)
    // Normally speaking, most western servers begin the y-coordinate at 1 from top to bottom.
    public static final int LABEL_Y_ASCENDING = 0;
    public static final int LABEL_Y_DESCENDING = 1;

    /**
     * Diameter of a single stone, in pixels
     */
    private double stoneSize;

    /**
     * Margin between stones, in pixels
     */
    private double stoneGap;

    /**
     *
     */
    private double offsetX;
    private double offsetY;

    /**
     * X offset position to start drawing board grid lines.
     */
    private double gridOffsetX;

    /**
     * Y offset position to start drawing board grid lines.
     */
    private double gridOffsetY;

    /**
     *
     */
    private double gap;

    /**
     *
     */
    private double drawWidth;
    private double drawHeight;

    /**
     * The lower of drawWidth and drawHeight. Used as a basis measurement for rest of the metrics.
     */
    private double minSize;

    /**
     * The rectangular dimensions of each intersection.
     */
    private double gridSize;

    /**
     * Game board width.
     */
    private int boardWidth;

    /**
     * Game board height.
     */
    private int boardHeight;

    // TODO: Move this to BoardBackgroundCanvas
    /**
     * A reference flag for coordinate display.
     */
    private boolean drawLabels = false;

    // TODO: Move this to BoardBackgroundCanvas
    /**
     * A reference parameter for coordinate alignment.
     */
    private int labelOrientation = LABEL_ORIENTATION_TOP_RIGHT;

    // TODO: Move this to BoardBackgroundCanvas
    /**
     * A reference flag for Y-axis coordinate reversal.
     */
    private int labelYOrder = LABEL_Y_ASCENDING;

    public void calibrate(GameBoard board) {
        // TODO: Add a preference for coordinates to be displayed OUTSIDE of the board background
        // therefore need to adjust some spacing calculations below...

        // Amount of spacing between the start of the board background and the grid lines
        double margin = (!drawLabels) ? 10 : (labelOrientation == LABEL_ORIENTATION_ALL) ? 45 : 30;
        Game game = board.getGame();
        this.boardWidth = game.getBoardWidth();
        this.boardHeight = game.getBoardHeight();
        drawWidth = board.getWidth() - margin;
        drawHeight = board.getHeight() - margin;
        // Account for reserved topComponent
        if (board.getTopToolBar() != null) {
            drawHeight -= RESERVED_TOOLBAR_SIZE;
        }
        minSize = Math.min(drawWidth, drawHeight);
        stoneSize = (minSize - 50) / Math.max(boardWidth, boardHeight);
        stoneGap = stoneSize / 30d;
        gridSize = stoneSize + stoneGap;

        // TODO potentially want to shift these according to arrangement of nearby panes
        offsetX = board.getWidth() / 2 - (boardWidth - 1) * gridSize / 2;
        offsetY = board.getHeight() / 2 - (boardHeight - 1) * gridSize / 2;
        if (board.getTopToolBar() != null) {
            offsetY += (double) RESERVED_TOOLBAR_SIZE / 2;
        }
        gridOffsetX = drawLabels ? (labelOrientation == LABEL_ORIENTATION_ALL) ? 40 : 20 : 0;
        gridOffsetY = drawLabels ? (labelOrientation == LABEL_ORIENTATION_ALL) ? 40 : 20 : 0;
        gap = drawLabels ? stoneSize / stoneGap / 11 : 0;


        // Debug:
//        System.out.println("==========================");
//        System.out.println("Board metrics calibration:");
//        System.out.println("==========================");
//        System.out.printf("Board:           %d x %d\n", boardWidth, boardHeight);
//        System.out.printf("Draw bounds:     %f x %f\n", drawWidth, drawHeight);
//        System.out.printf("minSize:         %f\n", minSize);
//        System.out.printf("stoneSize:       %f\n", stoneSize);
//        System.out.printf("stoneGap:        %f\n", stoneGap);
//        System.out.printf("gridSize:        %f\n", gridSize);
//        System.out.printf("Offsets:         %f, %f\n", offsetX, offsetY);
//        System.out.printf("Grid offsets:    %f, %f\n", gridOffsetX, gridOffsetY);
//        System.out.printf("gap:             %f\n", gap);
    }

    /**
     * Returns the x value on-screen that corresponds to the given x co-ordinate on the board.
     *
     * @param x The x co-ordinate on the board.
     * @return
     */
    public double getGridX(int x) {
        return gridOffsetX + offsetX + x * (gridSize - gap);
    }

    public double getBoardStoneX(int x) {
        return getGridX(x) - gridSize / 2 + gap * 2;
    }

    /**
     * Returns the y value on-screen that corresponds to the given y co-ordinate on the board.
     *
     * @param y The y co-ordinate on the board.
     * @return
     */
    public double getGridY(int y) {
        return gridOffsetY + offsetY + y * (gridSize);
    }

    public double getBoardStoneY(int y) {
        return getGridY(y) - gridSize / 2 + gap * 2;
    }

    // Getters

    public double getStoneSize() {
        return stoneSize;
    }

    public double getStoneGap() {
        return stoneGap;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public double getGridOffsetX() {
        return gridOffsetX;
    }

    public double getGridOffsetY() {
        return gridOffsetY;
    }

    public double getGap() {
        return gap;
    }

    public double getDrawWidth() {
        return drawWidth;
    }

    public double getDrawHeight() {
        return drawHeight;
    }

    public double getMinSize() {
        return minSize;
    }

    public double getGridSize() {
        return gridSize;
    }

    public int getBoardWidth() {
        return boardWidth;
    }

    public int getBoardHeight() {
        return boardHeight;
    }

    public boolean isDrawLabels() {
        return drawLabels;
    }

    public int getLabelOrientation() {
        return labelOrientation;
    }

    public int getLabelYOrder() {
        return labelYOrder;
    }

}
