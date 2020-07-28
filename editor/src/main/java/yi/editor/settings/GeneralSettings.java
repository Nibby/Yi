package yi.editor.settings;

import org.json.JSONObject;
import yi.editor.components.ContentLayout;
import yi.editor.utilities.JSON;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Stores general preference values for {@link yi.editor.EditorFrame}.
 */
public final class GeneralSettings extends SettingsModule {

    private final String settingsFile;

    private static final String KEY_BOARD_THEME_DIRECTORY = "boardTheme";
    private static final String KEY_CONTENT_LAYOUT = "contentLayout";

    // The theme directory to be loaded
    private ContentLayout currentLayout = ContentLayout.getDefaultValue();
    private String selectedBoardThemeDirectory;

    GeneralSettings(String settingsFilePath) {
        this.settingsFile = settingsFilePath;
    }

    @Override
    public void load() {
        try {
            Optional<JSONObject> settingsWrapper = Settings.readJSON(settingsFile);
            if (settingsWrapper.isEmpty()) {
                useAndSaveDefaults();
            } else {
                loadFromJson(settingsWrapper.get());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFromJson(JSONObject settings) {
        JSON.getString(settings, KEY_BOARD_THEME_DIRECTORY).ifPresent(value -> selectedBoardThemeDirectory = value);
        JSON.getString(settings, KEY_CONTENT_LAYOUT).ifPresent(value -> currentLayout = ContentLayout.getValue(value));


    }

    @Override
    public void save() {
        JSONObject settings = new JSONObject();
        settings.put(KEY_BOARD_THEME_DIRECTORY, GameBoardThemeSettings.getDefaultThemeDirectory());
        settings.put(KEY_CONTENT_LAYOUT, currentLayout.name());

        Path file = Paths.get(settingsFile);
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write(settings.toString(4));
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void useAndSaveDefaults() {
        setSelectedBoardThemeDirectory(GameBoardThemeSettings.getDefaultThemeDirectory());
        setCurrentLayout(ContentLayout.getDefaultValue());
        save();
    }

    public String getSelectedBoardTheme() {
        return selectedBoardThemeDirectory;
    }

    public void setSelectedBoardThemeDirectory(String selectedBoardThemeDirectory) {
        this.selectedBoardThemeDirectory = selectedBoardThemeDirectory;
    }

    public ContentLayout getCurrentLayout() {
        return currentLayout;
    }

    public void setCurrentLayout(ContentLayout currentLayout) {
        this.currentLayout = currentLayout;
    }
}
