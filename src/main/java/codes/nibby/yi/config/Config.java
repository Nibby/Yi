package codes.nibby.yi.config;

import codes.nibby.yi.Yi;
import codes.nibby.yi.board.BoardCursorType;
import codes.nibby.yi.board.BoardTheme;
import codes.nibby.yi.board.StoneStyle;
import codes.nibby.yi.editor.layout.LayoutType;
import org.json.JSONObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Loads and writes application settings to <i>config.json</i>.
 * Handles all the logic related to config.json.
 * <p>
 * TODO: Write saved data back to config.json
 *
 * @author Kevin Yang
 * Created on 23 August 2019
 */
public class Config {

    // Directories
    protected static final String THEME_DIRECTORY = "themes";
    protected static final String BOARD_THEME_DIRECTORY = "board_themes";
    protected static final String UI_THEME_DIRECTORY = "ui_themes";
    // JSON keys
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_UI_THEME = "ui_theme";
    private static final String KEY_BOARD = "board";
    private static final String KEY_BOARD_THEME_USE = "use";
    private static final String KEY_BOARD_THEME_STONES = "stones";
    private static final String KEY_BOARD_ALLOW_STONE_DISPLACEMENT = "stone_displacement";
    private static final String KEY_BOARD_THEME_BACKGROUND = "background";
    private static final String KEY_BOARD_CURSOR = "cursor";
    private static final String KEY_EDITOR = "editor";
    private static final String KEY_EDITOR_PERSPECTIVE = "perspective";
    // The config.json document.
    private static JSONObject root;

    // Current board theme template.
    private static BoardTheme boardTheme;

    // Current application display language
    private static UiLanguage uiLanguage;

    // Current application UI theme
    private static UiTheme uiTheme;

    // Type of cursor to be displayed when mouse hovers over the board.
    private static BoardCursorType cursorType;

    // Type of stone to draw by default
    private static StoneStyle stoneStyle;

    // Whether stones are allowed to have misalignment as a result of
    // stones bumping into one another. (eye candy)
    private static boolean stoneDisplacement;

    // Current editor window perspective
    private static LayoutType editorLayout;

    static {
        load();
    }

    private static void load() {
        if (root != null)
            throw new RuntimeException("config.json can only be loaded once!");

        try (Scanner scan = new Scanner(Paths.get(Yi.PATH_CONFIG))) {
            StringBuilder buffer = new StringBuilder();
            while (scan.hasNextLine()) {
                buffer.append(scan.nextLine());
            }

            root = new JSONObject(buffer.toString());

            String locale = root.getString(KEY_LANGUAGE);
            uiLanguage = new UiLanguage(locale);

            Path themeDirectory = Paths.get(THEME_DIRECTORY);
            {
                String useName = root.getString(KEY_UI_THEME);
                uiTheme = new UiTheme(useName);
            }

            // Board preferences
            // Load board theme template.
            {
                JSONObject boardConfig = root.getJSONObject(KEY_BOARD);
                String useName = boardConfig.getString(KEY_BOARD_THEME_USE);
                Path boardThemeDir = themeDirectory.resolve(BOARD_THEME_DIRECTORY).resolve(useName);
                boardTheme = new BoardTheme(boardThemeDir);
                stoneStyle = StoneStyle.parse(boardConfig.getString(KEY_BOARD_THEME_STONES));
                stoneDisplacement = boardConfig.getBoolean(KEY_BOARD_ALLOW_STONE_DISPLACEMENT);
                cursorType = BoardCursorType.parse(boardConfig.getString(KEY_BOARD_CURSOR));
            }

            // Editor window preferences
            {
                JSONObject editorConfig = root.getJSONObject(KEY_EDITOR);
                editorLayout = LayoutType.parse(editorConfig.getString(KEY_EDITOR_PERSPECTIVE));
            }

        } catch (Exception e) {
            // TODO: exception handling
            e.printStackTrace();
        }
    }

    public static BoardTheme getBoardTheme() {
        return boardTheme;
    }

    public static UiLanguage getLanguage() {
        return uiLanguage;
    }

    public static BoardCursorType getCursorType() {
        return cursorType;
    }

    public static UiTheme getUiTheme() {
        return uiTheme;
    }

    public static StoneStyle getStoneStyle() {
        return stoneStyle;
    }

    public static boolean allowStoneDisplacement() {
        return stoneDisplacement;
    }

    public static LayoutType getEditorLayout() {
        return editorLayout;
    }

    public static void setEditorLayout(LayoutType editorLayout) {
        Config.editorLayout = editorLayout;
    }
}
