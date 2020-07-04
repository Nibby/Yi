package codes.nibby.yi.editor.gui.board;

public final class GameBoardView {

    GameBoardView() { }

    private CoordinateLabelPosition coordinateLabelPosition = CoordinateLabelPosition.getDefaultValue();

    public CoordinateLabelPosition getCoordinateLabelPosition() {
        return coordinateLabelPosition;
    }

    public void setCoordinateLabelPosition(CoordinateLabelPosition coordinateLabelPosition) {
        this.coordinateLabelPosition = coordinateLabelPosition;
    }
}
