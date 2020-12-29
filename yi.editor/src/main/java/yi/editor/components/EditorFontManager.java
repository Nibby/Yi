package yi.editor.components;

import yi.component.shared.component.FontManager;

public final class EditorFontManager {

    private static final String FONT_RESOURCE_DIR = "/yi/editor/fonts/";
    private static final String[] BUNDLED_FONT_NAMES = {
            "NotoSans-Bold.ttf",
            "NotoSans-BoldItalic.ttf",
            "NotoSans-Italic.ttf",
            "NotoSans-Light.ttf",
            "NotoSans-Medium.ttf",
            "NotoSans-Regular.ttf"
    };

    private EditorFontManager() {
        // Helper class
    }

    public static void loadBundledFonts() {
        for (String fontName : BUNDLED_FONT_NAMES) {
            String fontResource = FONT_RESOURCE_DIR + fontName;
            FontManager.loadFont(fontResource, EditorFontManager.class);
        }
    }

}
