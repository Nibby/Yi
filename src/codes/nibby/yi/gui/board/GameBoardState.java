package codes.nibby.yi.gui.board;

import java.util.Arrays;

public final class GameBoardState {

    public enum IntersectionState {
        EMPTY,
        BLACK_STONE,
        WHITE_STONE
    }

    private int intersectionsHorizontal;
    private int intersectionsVertical;
    private IntersectionState[] intersectionStates;

    public void initialize(int intersectionsHorizontal, int intersectionsVertical) {
        this.intersectionsVertical = intersectionsVertical;
        this.intersectionsHorizontal = intersectionsHorizontal;
        this.intersectionStates = new IntersectionState[intersectionsVertical * intersectionsHorizontal];
        Arrays.fill(this.intersectionStates, IntersectionState.EMPTY);
    }

    public int getIntersectionsHorizontal() {
        return intersectionsHorizontal;
    }

    public int getIntersectionsVertical() {
        return intersectionsVertical;
    }

    public IntersectionState[] getIntersectionStates() {
        return intersectionStates;
    }

    public IntersectionState getIntersectionState(int x, int y) {
        return intersectionStates[x + y * intersectionsHorizontal];
    }
}
