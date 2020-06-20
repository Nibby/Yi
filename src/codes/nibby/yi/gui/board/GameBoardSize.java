package codes.nibby.yi.gui.board;

final class GameBoardSize {

    private double marginFromEdge;

    private double paddingForCoordinateLabels;

    private double stoneDiameter;
    private double stoneGap;

    private double totalWidth;
    private double totalHeight;

    public void recalculate(double componentWidth, double componentHeight) {
        this.totalWidth = componentWidth;
        this.totalHeight = componentHeight;
        // TODO: Finish this.
    }

}
