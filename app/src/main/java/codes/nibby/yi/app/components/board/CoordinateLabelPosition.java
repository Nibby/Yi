package codes.nibby.yi.app.components.board;

/**
 * Represents the side(s) on the game board to show co-ordinate labels.
 */
public enum CoordinateLabelPosition {

    /**
     * Show no coordinate labels.
     */
    NONE(0),

    /**
     * Show coordinates on the top and left side.
     */
    TOP_AND_LEFT(1),

    /**
     * Show coordinates on bottom and right side.
     */
    BOTTOM_AND_RIGHT(2),

    /**
     * Show coordinates on all sides.
     */
    ALL_SIDES(3);


    final int index;

    CoordinateLabelPosition(int index) {
        this.index = index;
    }

    public static CoordinateLabelPosition getDefaultValue() {
        return NONE;
    }
}
