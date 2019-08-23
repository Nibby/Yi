package codes.nibby.yi.config;

import codes.nibby.yi.Yi;
import codes.nibby.yi.board.BoardCursorType;
import codes.nibby.yi.board.BoardTheme;
import org.json.JSONObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Loads and writes application settings to <i>config.json</i>.
 * Handles all the logic related to config.json.
 *
 * TODO: Write saved data back to config.json
 *
 * @author Kevin Yang
 * Created on 23 August 2019
 */
public class Config {

    // JSON keys
    private static final String KEY_UI_THEME = "ui_theme";
    private static final String KEY_BOARD_THEME = "board_theme";
    private static final String KEY_BOARD_THEME_USE = "use";
    private static final String KEY_BOARD_THEME_STONES = "stones";
    private static final String KEY_BOARD_THEME_BACKGROUND = "background";
    private static final String KEY_BOARD_CURSOR = "board_cursor";

    // Directories
    protected static final String THEME_DIRECTORY = "themes";
    protected static final String BOARD_THEME_DIRECTORY = "board_themes";
    protected static final String UI_THEME_DIRECTORY = "ui_themes";

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

    /*
        Loads config once upon startup.
     */
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

            String locale = root.getString("language");
            uiLanguage = new UiLanguage(locale);

            Path themeDirectory = Paths.get(THEME_DIRECTORY);

            {
                String useName = root.getString(KEY_UI_THEME);
                Path uiThemeDir = themeDirectory.resolve(UI_THEME_DIRECTORY).resolve(useName);
                uiTheme = new UiTheme(useName);
            }

            {
                // Load board theme template.
                String useName = root.getJSONObject(KEY_BOARD_THEME).getString(KEY_BOARD_THEME_USE);
                Path boardThemeDir = themeDirectory.resolve(BOARD_THEME_DIRECTORY).resolve(useName);
                boardTheme = new BoardTheme(boardThemeDir);
            }
            cursorType = BoardCursorType.parse(root.getString(KEY_BOARD_CURSOR));

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
}
