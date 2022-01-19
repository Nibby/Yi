package codes.nibby.yi.app.framework.global;

import codes.nibby.yi.app.framework.ResourcePath;
import codes.nibby.yi.app.framework.YiFontManager;

final class GlobalFontInitializer {

    private static final String[] BUNDLED_FONT_NAMES = {
            "NotoSans-Bold.ttf",
            "NotoSans-BoldItalic.ttf",
            "NotoSans-Italic.ttf",
            "NotoSans-Light.ttf",
            "NotoSans-Medium.ttf",
            "NotoSans-Regular.ttf"
    };

    private GlobalFontInitializer() {
        // Helper class
    }

    public static void loadBundledFonts() {
        for (String fontName : BUNDLED_FONT_NAMES) {
            String fontResource = ResourcePath.FONTS.getFolderPath() + fontName;
            YiFontManager.loadFont(fontResource, GlobalFontInitializer.class);
        }
    }

}
