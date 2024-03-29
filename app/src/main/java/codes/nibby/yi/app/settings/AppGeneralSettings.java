package codes.nibby.yi.app.settings;

import codes.nibby.yi.app.components.AppPerspective;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import codes.nibby.yi.app.utilities.JsonUtilities;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * Stores general preference values for {@link EditorWindow}.
 */
public final class AppGeneralSettings extends AppSettingsModule {

    private final String settingsFile;

    private static final String KEY_BOARD_THEME_DIRECTORY = "boardTheme";
    private static final String KEY_CONTENT_LAYOUT = "contentLayout";
    private static final String KEY_SHOW_BOARD_COORDINATES = "showCoordinates";

    // The theme directory to be loaded
    private AppPerspective perspective = AppPerspective.getDefaultValue();
    private String boardThemeDirectory;
    private boolean showBoardCoordinates;

    AppGeneralSettings(String settingsFilePath) {
        this.settingsFile = settingsFilePath;
    }

    @Override
    public void load() {
        try {
            Optional<JSONObject> settingsWrapper = AppSettings.readJSON(settingsFile);
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
        JsonUtilities.getString(settings, KEY_BOARD_THEME_DIRECTORY).ifPresent(value -> boardThemeDirectory = value);
        JsonUtilities.getString(settings, KEY_CONTENT_LAYOUT).ifPresent(value -> perspective = AppPerspective.getValue(value));
        setShowBoardCoordinates(JsonUtilities.getBoolean(settings, KEY_SHOW_BOARD_COORDINATES, true));
    }

    @Override
    public void save() {
        JSONObject settings = new JSONObject();
        settings.put(KEY_BOARD_THEME_DIRECTORY, AppBoardThemeSettings.getDefaultThemeDirectory());
        settings.put(KEY_CONTENT_LAYOUT, perspective.name());
        settings.put(KEY_SHOW_BOARD_COORDINATES, showBoardCoordinates);

        Path file = AppSettings.getRootPath().resolve(settingsFile);
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write(settings.toString(4));
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void useAndSaveDefaults() {
        setBoardThemeDirectory(AppBoardThemeSettings.getDefaultThemeDirectory());
        setPerspective(AppPerspective.getDefaultValue());
        save();
    }

    public String getSelectedBoardTheme() {
        return boardThemeDirectory;
    }

    public void setBoardThemeDirectory(String boardThemeDirectory) {
        this.boardThemeDirectory = boardThemeDirectory;
    }

    public AppPerspective getPerspective() {
        return perspective;
    }

    public void setPerspective(AppPerspective perspective) {
        this.perspective = perspective;
    }

    @NotNull String getSettingsFileName() {
        return Objects.requireNonNull(settingsFile);
    }

    public void setShowBoardCoordinates(boolean show) {
        this.showBoardCoordinates = show;
    }

    public boolean isShowingBoardCoordinates() {
        return showBoardCoordinates;
    }
}
