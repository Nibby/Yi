package yi.common.component;

import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import yi.common.Resource;

import java.util.HashSet;
import java.util.Set;

/**
 * Wrapper for {@link Scene} with additional stylesheets applied.
 */
public class YiScene extends Scene {

    private static final Set<Resource> EXTRA_STYLESHEETS = new HashSet<>();

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
        for (Resource extraCss : EXTRA_STYLESHEETS) {
            String resourceString = extraCss.getResourceUrlAsString();
            stylesheets.add(resourceString);
        }
    }

    public static void addExtraStylesheet(String cssResourcePath, Class<?> resourceLoaderClass) {
        var resource = new Resource(cssResourcePath, resourceLoaderClass);
        EXTRA_STYLESHEETS.add(resource);
    }
}
