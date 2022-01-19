package codes.nibby.yi.app.components.board;

import codes.nibby.yi.app.framework.ResourcePath;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.Objects;

final class GameBoardView {

    public CoordinateLabelPosition coordinateLabelPosition = CoordinateLabelPosition.getDefaultValue();
//    public PresetStoneStyle presetStoneStyle = PresetStoneStyle.getDefaultValue();
    public @Nullable Image boardImage = new Image(getInputStream(ResourcePath.BOARD.resolve("defaultBoard.png").getFilePath()));
    public @Nullable Image backgroundImage = new Image(getInputStream(ResourcePath.BOARD.resolve("defaultBackground.jpg").getFilePath()));
    public Color boardGridColor = new Color(158d/255d, 103d/255d, 35d/255d, 1.0d);

    @NotNull
    private InputStream getInputStream(String resourceUrl) {
        InputStream stream = this.getClass().getResourceAsStream(resourceUrl);
        return Objects.requireNonNull(stream, "Invalid resource URL: " + resourceUrl);
    }

}