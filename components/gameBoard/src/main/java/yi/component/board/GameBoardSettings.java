package yi.component.board;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.Optional;

public final class GameBoardSettings {

    private Image backgroundImage;
    private Color gridColor;

    public void setBackgroundImage(Image image) {
        this.backgroundImage = image;
    }

    public Optional<Image> getBackgroundImage() {
        return Optional.ofNullable(backgroundImage);
    }

    public void setGridColor(Color gridColor) {
        this.gridColor = gridColor;
    }

    public Optional<Color> getGridColor() {
        return Optional.ofNullable(gridColor);
    }
}
