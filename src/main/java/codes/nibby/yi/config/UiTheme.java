package codes.nibby.yi.config;

import codes.nibby.yi.Yi;
import codes.nibby.yi.utility.AlertUtility;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Stores data of a selected theme in 'themes/ui_theme/' directory.
 *
 * @author Kevin Yang
 * Created on 24 August 2019
 */
public class UiTheme {

    // Path constants
    private static final String MAIN_JSON_FILE = "uiTheme.json";

    // JSON config keys
    private static final String JSON_KEY_NAME = "name";
    private static final String JSON_KEY_CSS = "css_main";

    // Contents of uiTheme.json
    private JSONObject config;

    /*
        TODO: - test: directory missing
        TODO: - test: CSS file missing
     */
    protected UiTheme(String path) {
        Path themeDir = Paths.get(Config.THEME_DIRECTORY).resolve(Config.UI_THEME_DIRECTORY).resolve(path);
        Path themeJson = themeDir.resolve(MAIN_JSON_FILE);

        StringBuilder buffer = new StringBuilder();
        try (Scanner scanner = new Scanner(themeJson)) {
            while (scanner.hasNextLine()) {
                buffer.append(scanner.nextLine());
            }

            config = new JSONObject(buffer.toString());
            String cssFile = config.getString(JSON_KEY_CSS);
            Path themeCss = themeDir.resolve(cssFile);
            UiStylesheets.add(themeCss.toAbsolutePath().toString());

        } catch (NoSuchFileException e) {
            e.printStackTrace();
            AlertUtility.showAlert("The theme configuration file is not found!\n" + e.getMessage(),
                    "UiTheme: NoSuchFileException", Alert.AlertType.ERROR, ButtonType.OK);
            Yi.exit();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtility.showAlert("Unable to read UI data.\n" + e.getMessage(),
                    "UiTheme: IOException", Alert.AlertType.ERROR, ButtonType.OK);
            Yi.exit();
        } catch (JSONException e) {
            e.printStackTrace();
            AlertUtility.showAlert("The theme configuration file is invalid:\n" + e.getMessage(),
                    "UiTheme: JSONException", Alert.AlertType.ERROR, ButtonType.OK);
            Yi.exit();
        }
    }

    public String getName() {
        return config.getString(JSON_KEY_NAME);
    }
}
