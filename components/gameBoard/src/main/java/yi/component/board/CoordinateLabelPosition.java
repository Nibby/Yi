package yi.component.board;

/**
 * Denotes the sides on the game board to show co-ordinate labels
 */
public enum CoordinateLabelPosition {

    NONE(0),
    TOP_AND_LEFT(1),
    BOTTOM_AND_RIGHT(2),
    ALL_SIDES(3);

    final int index;

    CoordinateLabelPosition(int index) {
        this.index = index;
    }

    public static CoordinateLabelPosition getDefaultValue() {
        return NONE;
    }
}
