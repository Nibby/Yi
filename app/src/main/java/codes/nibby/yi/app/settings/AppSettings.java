package codes.nibby.yi.app.settings;

import codes.nibby.yi.app.framework.global.GlobalHelper;
import org.json.JSONObject;
import codes.nibby.yi.app.components.board.GameBoardViewer;
import codes.nibby.yi.app.utilities.JsonUtilities;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Main controller for all configurable settings in the program.
 */
public final class AppSettings {

    static final String THEME_DIRECTORY_NAME = "themes";

    private static boolean loadedOnce = false;
    private static Path rootPath = null;

    private static final Set<AppSettingsModule> modules = new LinkedHashSet<>();

    public static final AppGeneralSettings general = new AppGeneralSettings("settings.json");
    public static final AppAcceleratorSettings accelerator = new AppAcceleratorSettings("shortcutKeys.json");
    public static final AppBoardThemeSettings boardTheme = new AppBoardThemeSettings();

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

    private AppSettings() {

    }

    public static void load() {
        if (loadedOnce) {
            if (GlobalHelper.isRunningAsTest()) {
                return;
            }
            throw new IllegalStateException("This operation can only be called once upon startup!");
        }

        // Always load general first, because other modules may depend on some values in here.
        // For other modules, there should be no dependencies.
        general.load();

        Set<AppSettingsModule> modulesToLoadAfterGeneral = new HashSet<>(modules);
        modulesToLoadAfterGeneral.remove(general);
        modulesToLoadAfterGeneral.forEach(AppSettingsModule::load);

        loadedOnce = true;
    }

    public static void save() {
        for (AppSettingsModule module : modules) {
            module.save();
        }
    }

    private static void addModule(AppSettingsModule module) {
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
        Path rootPath = GlobalHelper.getPreferredSettingsRootPath().orElse(getDefaultRootPath());

        if (!Files.isDirectory(rootPath)) {
            Files.createDirectories(rootPath);
        }

        // Test directory writable
        Path tempFile = rootPath.resolve("test-writable");
        try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
            writer.write("x");
        } catch (IOException e) {
            // TODO: Prompt user to set a new location?
            throw new IllegalStateException("Settings root directory is not writable!", e);
        }
        Files.deleteIfExists(tempFile);

        return rootPath;
    }

    private static Path getDefaultRootPath() {
        Path path;
        if (GlobalHelper.isRunningFromSource()) {
            path = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        } else {
            path = Paths.get(System.getProperty("user.home")).resolve(GlobalHelper.getProgramName());
        }
        return path;
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

        return Optional.of(JsonUtilities.read(settingsFile));
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
