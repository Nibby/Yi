package codes.nibby.yi.utility;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class IconUtility {

    public static ImageView getFxIcon(String resource) {
        return new ImageView(new Image(IconUtility.class.getResourceAsStream(resource)));
    }

    public static ImageView getFxIcon(String resource, int width, int height) {
        ImageView imgView = getFxIcon(resource);
        imgView.setFitWidth(width);
        imgView.setFitHeight(height);
        return imgView;
    }
}
