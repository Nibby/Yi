package yi.component.board;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.Optional;

public final class GameBoardSettings {

    private Image backgroundImage;
    private Image boardImage;
    private Color gridColor;

    public void setBoardImage(Image image) {
        this.boardImage = image;
    }

    public Optional<Image> getBoardImage() {
        return Optional.ofNullable(boardImage);
    }

    public void setGridColor(Color gridColor) {
        this.gridColor = gridColor;
    }

    public Optional<Color> getGridColor() {
        return Optional.ofNullable(gridColor);
    }

    public void setBackgroundImage(Image image) {
        backgroundImage = image;
    }

    public Optional<Image> getBackgroundImage() {
        return Optional.ofNullable(backgroundImage);
    }
}
