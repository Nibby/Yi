package yi.component.board;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

final class GameBoardView {

    GameBoardView() { }

    public CoordinateLabelPosition coordinateLabelPosition = CoordinateLabelPosition.getDefaultValue();
    public PresetStoneStyle presetStoneStyle = PresetStoneStyle.getDefaultValue();
    public Image boardImage = new Image(this.getClass().getResourceAsStream("/defaultBoard.png"));
    public Image backgroundImage = new Image(this.getClass().getResourceAsStream("/defaultBackground.jpg"));
    public Color boardGridColor = new Color(158d/255d, 103d/255d, 35d/255d, 1.0d);
}
