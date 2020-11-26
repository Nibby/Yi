package yi.editor.utilities;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import yi.editor.components.GameBoardToolBar;

import java.util.Optional;

public final class IconUtilities {

    private IconUtilities() {
    }

    public static Optional<ImageView> getIcon(String resourceFile) {
        try {
            var resourceStream = GameBoardToolBar.class.getResourceAsStream(resourceFile);
            var iconImage = new Image(resourceStream);
            var icon = new ImageView(iconImage);

            int ICON_SIZE = 16;
            icon.setFitWidth(ICON_SIZE);
            icon.setFitHeight(ICON_SIZE);

            return Optional.of(icon);
        } catch (NullPointerException e) {
            return Optional.empty();
        }
    }

}
