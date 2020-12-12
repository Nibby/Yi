package yi.common.component;

import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Wrapper for {@link Scene} with additional stylesheets applied.
 */
public class YiScene extends Scene {

    private static final String CSS_FONT = "/font.css";

    public YiScene(Parent root) {
        super(root);
        applySkin();
    }

    public YiScene(Parent root, double width, double height) {
        super(root, width, height);
        applySkin();
    }

    private void applySkin() {
        var skin = SkinManager.getUsedSkin();
        var skinCss = skin.getMainCssUrl();

        ObservableList<String> stylesheets = getStylesheets();

        stylesheets.add(skinCss);
        stylesheets.add(YiScene.class.getResource(CSS_FONT).toString());
    }

}
