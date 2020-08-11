package yi.component;

import javafx.scene.Parent;
import javafx.scene.Scene;

public class YiScene extends Scene {

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
        System.out.println(skinCss);
        getStylesheets().add(skinCss);
    }

}
