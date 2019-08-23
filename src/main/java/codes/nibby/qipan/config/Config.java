package codes.nibby.qipan.config;

import codes.nibby.qipan.Main;
import codes.nibby.qipan.board.BoardTheme;
import org.json.JSONObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Loads and writes application settings to <i>config.json</i>.
 * Handles all the logic related to config.json.
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

    // Directories
    private static final String THEME_DIRECTORY = "themes";

    // The config.json document
    private static JSONObject root;

    // Current board theme template
    private static BoardTheme boardTheme;

    /*
        Loads config once upon startup.
     */
    static {
        load();
    }

    private static void load() {
        if (root != null)
            throw new RuntimeException("config.json can only be loaded once!");

        try (Scanner scan = new Scanner(Paths.get(Main.PATH_CONFIG))) {
            StringBuilder buffer = new StringBuilder();
            while (scan.hasNextLine()) {
                buffer.append(scan.nextLine());
            }

            root = new JSONObject(buffer.toString());

            // Load board theme template
            String useName = root.getJSONObject(KEY_THEME).getString(KEY_THEME_USE);
            Path directory = Paths.get(THEME_DIRECTORY).resolve(useName);
            boardTheme = new BoardTheme(directory);

        } catch (Exception e) {
            // TODO: exception handling
            e.printStackTrace();
        }
    }

    public static BoardTheme boardTheme() {
        return boardTheme;
    }
}
