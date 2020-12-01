package yi.editor.settings;

import org.json.JSONObject;
import yi.component.board.GameBoardViewer;
import yi.editor.EditorHelper;
import yi.editor.EditorMain;
import yi.common.utilities.JSON;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Main controller for all configurable settings in the program.
 */
public final class EditorSettings {

    static final String THEME_DIRECTORY_NAME = "themes";

    private static boolean loadedOnce = false;
    private static Path rootPath = null;

    private static final Set<EditorSettingsModule> modules = new LinkedHashSet<>();

    public static final EditorGeneralSettings general = new EditorGeneralSettings("settings.json");
    public static final EditorAcceleratorSettings accelerator = new EditorAcceleratorSettings("shortcutKeys.json");
    public static final EditorBoardThemeSettings boardTheme = new EditorBoardThemeSettings();

    static {
        try {
            rootPath = resolveRootPath();

            addModule(general);
            addModule(accelerator);
            addModule(boardTheme);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private EditorSettings() {

    }

    /**
     * Called once upon startup, invokes {@link EditorSettingsModule#load()} on all registered modules to fetch the settings from
     * configuration files. In almost any circumstance, there is no need to use this method outside of {@link EditorMain}.
     */
    public static void load() {
        if (loadedOnce) {
            if (EditorHelper.isRunningAsTest()) {
                return;
            }
            throw new IllegalStateException("This operation can only be called once upon startup!");
        }

        // Always load general first, because other modules may depend on some values in here.
        // For other modules, there should be no dependencies.
        general.load();

        Set<EditorSettingsModule> modulesToLoadAfterGeneral = new HashSet<>(modules);
        modulesToLoadAfterGeneral.remove(general);
        modulesToLoadAfterGeneral.forEach(EditorSettingsModule::load);

        loadedOnce = true;
    }

    public static void save() {
        for (EditorSettingsModule module : modules) {
            module.save();
        }
    }

    private static void addModule(EditorSettingsModule module) {
        if (modules.contains(module))
            throw new IllegalArgumentException("Duplicated module: " + module.getClass().toString());

        modules.add(module);
    }

    /**
     *
     * @return The top-level directory for storing all program settings
     */
    public static Path getRootPath() {
        return Objects.requireNonNull(rootPath, "Top level settings path is not initialized. " +
                "Settings path operations should not be called in SettingsModule constructors.");
    }

    /**
     * Ensures a directory is available for storing setting files.
     *
     * @return The directory path to use as top-level settings directory.
     */
    private static Path resolveRootPath() throws IOException {
        return EditorHelper.getPreferredSettingsRootPath()
                .orElse(Paths.get(System.getProperty("user.dir")).toAbsolutePath());
    }

    /**
     * Alternative method to {@link #readJSON(Path)} that uses string file paths.
     *
     * This is a convenience method if the file is at the top-level.
     *
     * @param filePath Path of the JSON file
     * @return The JSON object if the file is parsed successfully, or {@link Optional#empty()} otherwise.
     * @throws IOException If the file cannot be found or read.
     *
     * @see #readJSON(Path)
     */
    public static Optional<JSONObject> readJSON(String filePath) throws IOException {
        return readJSON(getRootPath().resolve(filePath));
    }

    /**
     * Parses a file into a {@link JSONObject}, assuming the filePath is relative to {@link #getRootPath()}.
     * @param filePath Path of the JSON file to read, relative to {@link #getRootPath()}.
     * @return The {@link JSONObject} representation of the file, or {@link Optional#empty()} if the file is not found, or is directory.
     * @throws IOException If the file cannot be found or read.
     */
    public static Optional<JSONObject> readJSON(Path filePath) throws IOException {
        Path settingsFile = getRootPath().resolve(filePath);

        if (!Files.exists(settingsFile) || Files.isDirectory(settingsFile)) {
            return Optional.empty();
        }

        return Optional.of(JSON.read(settingsFile));
    }

    public static void applySavedBoardSettings(GameBoardViewer board) {
        if (!loadedOnce) {
            throw new IllegalStateException("Settings has not loaded for the first time");
        }
        board.setBackgroundImage(boardTheme.getBackgroundImage());
        board.setBoardImage(boardTheme.getBoardImage());
        board.setGridColor(boardTheme.getBoardGridColor());

        board.setShowCoordinates(general.isShowingBoardCoordinates());
    }
}
