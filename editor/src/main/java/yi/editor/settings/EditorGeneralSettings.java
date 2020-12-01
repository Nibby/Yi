package yi.editor.settings;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import yi.editor.EditorWindow;
import yi.editor.components.EditorPerspective;
import yi.common.utilities.JSON;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

/**
 * Stores general preference values for {@link EditorWindow}.
 */
public final class EditorGeneralSettings extends EditorSettingsModule {

    private final String settingsFile;

    private static final String KEY_BOARD_THEME_DIRECTORY = "boardTheme";
    private static final String KEY_CONTENT_LAYOUT = "contentLayout";
    private static final String KEY_SHOW_BOARD_COORDINATES = "showCoordinates";

    // The theme directory to be loaded
    private EditorPerspective perspective = EditorPerspective.getDefaultValue();
    private String boardThemeDirectory;
    private boolean showBoardCoordinates;

    EditorGeneralSettings(String settingsFilePath) {
        this.settingsFile = settingsFilePath;
    }

    @Override
    public void load() {
        try {
            Optional<JSONObject> settingsWrapper = EditorSettings.readJSON(settingsFile);
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
        JSON.getString(settings, KEY_BOARD_THEME_DIRECTORY).ifPresent(value -> boardThemeDirectory = value);
        JSON.getString(settings, KEY_CONTENT_LAYOUT).ifPresent(value -> perspective = EditorPerspective.getValue(value));
        setShowBoardCoordinates(JSON.getBoolean(settings, KEY_SHOW_BOARD_COORDINATES, true));
    }

    @Override
    public void save() {
        JSONObject settings = new JSONObject();
        settings.put(KEY_BOARD_THEME_DIRECTORY, EditorBoardThemeSettings.getDefaultThemeDirectory());
        settings.put(KEY_CONTENT_LAYOUT, perspective.name());
        settings.put(KEY_SHOW_BOARD_COORDINATES, showBoardCoordinates);

        Path file = Paths.get(settingsFile);
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write(settings.toString(4));
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void useAndSaveDefaults() {
        setBoardThemeDirectory(EditorBoardThemeSettings.getDefaultThemeDirectory());
        setPerspective(EditorPerspective.getDefaultValue());
        save();
    }

    public String getSelectedBoardTheme() {
        return boardThemeDirectory;
    }

    public void setBoardThemeDirectory(String boardThemeDirectory) {
        this.boardThemeDirectory = boardThemeDirectory;
    }

    public EditorPerspective getPerspective() {
        return perspective;
    }

    public void setPerspective(EditorPerspective perspective) {
        this.perspective = perspective;
    }

    protected final @NotNull String getSettingsFileName() {
        return Objects.requireNonNull(settingsFile);
    }

    public void setShowBoardCoordinates(boolean show) {
        this.showBoardCoordinates = show;
    }

    public boolean isShowingBoardCoordinates() {
        return showBoardCoordinates;
    }
}
