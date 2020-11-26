package yi.editor;

import yi.component.i18n.Language;
import yi.component.i18n.TextResource;

import java.util.Locale;

/**
 * Stores all the i18n translation keys used by the Yi editor module.
 */
public final class TextKeys {

    private static final String I18N_PACKAGE = "i18n.";

    public static void installSupportedLanguages() {
        Language.add(new Language("简体中文", Locale.SIMPLIFIED_CHINESE));
    }

    private static final String BUNDLE = I18N_PACKAGE + "editor";

    public static final TextResource MENU_FILE = new TextResource("menu.file", BUNDLE);

    public static final TextResource MENU_EDIT = new TextResource("menu.edit", BUNDLE);
    public static final TextResource MENU_PERSPECTIVE = new TextResource("menu.perspective", BUNDLE);
    public static final TextResource MENUITEM_PERSPECTIVE_COMPACT = new TextResource("menuItem.perspective.compact", BUNDLE);
    public static final TextResource MENUITEM_PERSPECTIVE_REVIEW = new TextResource("menuItem.perspective.review", BUNDLE);

    public static final TextResource MENU_TOOLS = new TextResource("menu.tools", BUNDLE);
    public static final TextResource MENU_VIEW = new TextResource("menu.view", BUNDLE);
    public static final TextResource MENU_WINDOW = new TextResource("menu.window", BUNDLE);
    public static final TextResource MENU_HELP = new TextResource("menu.help", BUNDLE);

    public static final TextResource MENUITEM_NEW_GAME = new TextResource("menuItem.newGame", BUNDLE);
    public static final TextResource MENUITEM_OPEN_GAME = new TextResource("menuItem.openGame", BUNDLE);
    public static final TextResource MENUITEM_SAVE_GAME = new TextResource("menuItem.saveGame", BUNDLE);
    public static final TextResource MENUITEM_SAVE_AS_GAME = new TextResource("menuItem.saveAsGame", BUNDLE);

    public static final TextResource UNDO = new TextResource("undo", BUNDLE);
    public static final TextResource REDO = new TextResource("redo", BUNDLE);

    public static final TextResource TOOL_PLAY_MOVE = new TextResource("tool.playMove", BUNDLE);
    public static final TextResource TOOL_ADD_BLACK = new TextResource("tool.addBlack", BUNDLE);
    public static final TextResource TOOL_ADD_WHITE = new TextResource("tool.addWhite", BUNDLE);
    public static final TextResource TOOL_TRIANGLE = new TextResource("tool.triangle", BUNDLE);
    public static final TextResource TOOL_CIRCLE = new TextResource("tool.circle", BUNDLE);
    public static final TextResource TOOL_SQUARE = new TextResource("tool.square", BUNDLE);
    public static final TextResource TOOL_CROSS = new TextResource("tool.cross", BUNDLE);
    public static final TextResource TOOL_LABEL_LETTER = new TextResource("tool.labelLetter", BUNDLE);
    public static final TextResource TOOL_LABEL_NUMBER = new TextResource("tool.labelNumber", BUNDLE);
    public static final TextResource TOOL_LINE = new TextResource("tool.line", BUNDLE);
    public static final TextResource TOOL_ARROW = new TextResource("tool.arrow", BUNDLE);
    public static final TextResource TOOL_DIM = new TextResource("tool.dim", BUNDLE);

    public static final TextResource TOGGLE_PERSPECTIVE_REVIEW = new TextResource("shortcut.togglePerspectiveReview", BUNDLE);
    public static final TextResource TOGGLE_PERSPECTIVE_COMPACT = new TextResource("shortcut.togglePerspectiveCompact", BUNDLE);

    public static final TextResource DEFAULT_BLACK_NAME = new TextResource("default.blackName", BUNDLE);
    public static final TextResource DEFAULT_WHITE_NAME = new TextResource("default.whiteName", BUNDLE);

    public static final TextResource MOVE_COUNT = new TextResource("moveCount", BUNDLE);
}
