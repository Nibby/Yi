package yi.models.yi.editor.gui.board;

public final class GameBoardView {

    GameBoardView() { }

    private CoordinateLabelPosition coordinateLabelPosition = CoordinateLabelPosition.getDefaultValue();
    private PresetStoneStyle presetStoneStyle = PresetStoneStyle.getDefaultValue();

    public CoordinateLabelPosition getCoordinateLabelPosition() {
        return coordinateLabelPosition;
    }

    public void setCoordinateLabelPosition(CoordinateLabelPosition coordinateLabelPosition) {
        this.coordinateLabelPosition = coordinateLabelPosition;
    }
}
