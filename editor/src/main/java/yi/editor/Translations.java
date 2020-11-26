package yi.editor;

import yi.component.i18n.Language;
import yi.component.i18n.TextResource;

import java.util.Locale;

/**
 * Stores all the translation keys used by the Yi editor module. In other words, all
 * i18n translation keys are stored here.
 */
public final class Translations {

    private static final String I18N_PACKAGE = "i18n.";

    public static void installSupportedLanguages() {
        Language.add(new Language("简体中文",Locale.SIMPLIFIED_CHINESE));
    }

    public static final class Menu {

        private static final String BUNDLE = I18N_PACKAGE + "menus";

        public static final TextResource MENU_FILE = new TextResource("menu.file", BUNDLE);
        public static final TextResource MENU_EDIT = new TextResource("menu.edit", BUNDLE);
        public static final TextResource MENU_TOOLS = new TextResource("menu.tools", BUNDLE);
        public static final TextResource MENU_VIEW = new TextResource("menu.view", BUNDLE);
        public static final TextResource MENU_WINDOW = new TextResource("menu.window", BUNDLE);
        public static final TextResource MENU_HELP = new TextResource("menu.help", BUNDLE);

        public static final TextResource ITEM_NEW_GAME = new TextResource("item.newGame", BUNDLE);
        public static final TextResource ITEM_OPEN_GAME = new TextResource("item.openGame", BUNDLE);
        public static final TextResource ITEM_SAVE_GAME = new TextResource("item.saveGame", BUNDLE);
        public static final TextResource ITEM_SAVE_AS_GAME = new TextResource("item.saveAsGame", BUNDLE);
    }

    public static final class Editor {
        private static final String BUNDLE = I18N_PACKAGE + "editor";

        public static final TextResource UNDO = new TextResource("undo", BUNDLE);
        public static final TextResource REDO = new TextResource("redo", BUNDLE);

        public static final TextResource TOOLBAR_TOOL_PLAY_MOVE = new TextResource("toolbar.tool.playMove", BUNDLE);
        public static final TextResource TOOLBAR_TOOL_ADD_BLACK = new TextResource("toolbar.tool.addBlack", BUNDLE);
        public static final TextResource TOOLBAR_TOOL_ADD_WHITE = new TextResource("toolbar.tool.addWhite", BUNDLE);
        public static final TextResource TOOLBAR_TOOL_TRIANGLE = new TextResource("toolbar.tool.triangle", BUNDLE);
        public static final TextResource TOOLBAR_TOOL_CIRCLE = new TextResource("toolbar.tool.circle", BUNDLE);
        public static final TextResource TOOLBAR_TOOL_SQUARE = new TextResource("toolbar.tool.square", BUNDLE);
        public static final TextResource TOOLBAR_TOOL_CROSS = new TextResource("toolbar.tool.cross", BUNDLE);
        public static final TextResource TOOLBAR_TOOL_LABEL_LETTER = new TextResource("toolbar.tool.labelLetter", BUNDLE);
        public static final TextResource TOOLBAR_TOOL_LABEL_NUMBER = new TextResource("toolbar.tool.labelNumber", BUNDLE);
        public static final TextResource TOOLBAR_TOOL_LINE = new TextResource("toolbar.tool.line", BUNDLE);
        public static final TextResource TOOLBAR_TOOL_ARROW = new TextResource("toolbar.tool.arrow", BUNDLE);
        public static final TextResource TOOLBAR_TOOL_DIM = new TextResource("toolbar.tool.dim", BUNDLE);

    }

}
