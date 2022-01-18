package codes.nibby.yi.app.framework;

final class GlobalFontInitializer {

    private static final String FONT_RESOURCE_DIR = "/codes/nibby/yi/app/fonts/";
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
            String fontResource = FONT_RESOURCE_DIR + fontName;
            YiFontManager.loadFont(fontResource, GlobalFontInitializer.class);
        }
    }

}
