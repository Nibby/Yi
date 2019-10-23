package codes.nibby.yi.utility;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.nio.file.Path;

public class UiUtility {

    public static ImageView getFxIcon(String resource) {
        return new ImageView(new Image(UiUtility.class.getResourceAsStream(resource)));
    }

    public static ImageView getFxIcon(String resource, int width, int height) {
        ImageView imgView = getFxIcon(resource);
        imgView.setFitWidth(width);
        imgView.setFitHeight(height);
        return imgView;
    }

    public static FileChooser createGameRecordOpenFileChooser(String title, Path initialDirectory) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.setInitialDirectory(initialDirectory.toFile());
        // Game record file chooser
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Go game formats (.sgf)", ".sgf"));
        return fc;
    }
}
