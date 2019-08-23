package codes.nibby.yi.board;

/**
 * Determines the type of graphics to be displayed when the mouse hovers over
 * a board intersection.
 *
 * @author Kevin Yang
 * Created on 23 August 2019
 */
public enum BoardCursorType {

    CIRCLE,
    SQUARE,
    STONE;

    public static BoardCursorType parse(String value) {
        value = value.replace("$", "");
        for (BoardCursorType v : BoardCursorType.values()) {
            if (value.toUpperCase().equals(v.name()))
                return v;
        }
        return null;
    }
}
