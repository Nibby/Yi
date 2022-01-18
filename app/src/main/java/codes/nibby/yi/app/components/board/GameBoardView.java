package codes.nibby.yi.app.components.board;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.Nullable;

final class GameBoardView {

    public CoordinateLabelPosition coordinateLabelPosition = CoordinateLabelPosition.getDefaultValue();
//    public PresetStoneStyle presetStoneStyle = PresetStoneStyle.getDefaultValue();
    public @Nullable Image boardImage = new Image(this.getClass().getResourceAsStream("/codes/nibby/yi/app/components/board/defaultBoard.png"));
    public @Nullable Image backgroundImage = new Image(this.getClass().getResourceAsStream("/codes/nibby/yi/app/components/board/defaultBackground.jpg"));
    public Color boardGridColor = new Color(158d/255d, 103d/255d, 35d/255d, 1.0d);

}