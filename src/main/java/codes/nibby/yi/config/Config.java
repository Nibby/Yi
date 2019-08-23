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
    private static final String KEY_THEME = "theme";
    private static final String KEY_THEME_USE = "use";
    private static final String KEY_THEME_STONES = "stones";
    private static final String KEY_THEME_BACKGROUND = "background";
    private static final String KEY_BOARD_CURSOR = "board_cursor";

    // Directories
    private static final String BOARD_THEME_DIRECTORY = "boardThemes";
    private static final String UI_THEME_DIRECTORY = "uiThemes";

    // The config.json document.
    private static JSONObject root;

    // Current board theme template.
    private static BoardTheme boardTheme;

    // Current application display language
    private static UiLanguage uiLanguage;

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

            // Load board theme template.
            String useName = root.getJSONObject(KEY_THEME).getString(KEY_THEME_USE);
            Path directory = Paths.get(BOARD_THEME_DIRECTORY).resolve(useName);
            boardTheme = new BoardTheme(directory);

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
}
