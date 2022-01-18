package codes.nibby.yi.app.framework;

import javafx.application.Application;
import javafx.scene.text.Font;
import org.jetbrains.annotations.Nullable;
import codes.nibby.yi.app.audio.CommonAudioSets;
import codes.nibby.yi.app.audio.SoundManager;
import codes.nibby.yi.app.utilities.SystemUtilities;
import codes.nibby.yi.app.settings.AppSettings;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Global helper class that manages various startup configurations for the editor
 * application.
 */
public final class GlobalHelper {

    private static final Version VERSION = new Version(0, 0, 1, "alpha");
    private static final String PROGRAM_NAME = "Yi";

    private static boolean useSystemMenuBar = SystemUtilities.isMac();
    private static boolean runningAsTest = false;
    private static Path preferredSettingsRootPath = null;
    private static boolean isInitialized = false;

    private GlobalHelper() {
        // Utility class, no instantiation
    }

    /**
     * @return Version of this editor application.
     */
    public static Version getVersion() {
        return VERSION;
    }

    /**
     * @return Name of this editor application.
     */
    public static String getProgramName() {
        return PROGRAM_NAME;
    }

    /**
     * @return {@code true} if the application is currently run within an IDE rather than
     * a distribution.
     */
    public static boolean isRunningFromSource() {
        return System.getProperty("yi.fromSource", "false").equalsIgnoreCase("true");
    }

    /**
     * In debug mode, components are expected to display additional technical information
     * reserved for development purposes.
     *
     * @return {@code true} if debug mode is to be enabled.
     */
    public static boolean isDebugMode() {
        return !isRunningFromSource();
    }

    /**
     * @param isRunningAsTest {@code true} if the application is being run as part of a
     * test suite.
     */
    public static synchronized void setRunningAsTest(boolean isRunningAsTest) {
        GlobalHelper.runningAsTest = isRunningAsTest;
    }

    /**
     * @return {@code true} if the application is being run as part of a test suite.
     */
    public static boolean isRunningAsTest() {
        return runningAsTest;
    }

    /**
     * Set whether the application menu bar should be displayed in a manner that appears
     * native to the current operating system. For example on macOS, if this is enabled,
     * the menu bar will appear on the top of the screen rather than a custom JavaFx
     * component in the window itself.
     *
     * @param yes Whether to use system native menu bar appearance.
     */
    public static void setUseSystemMenuBar(boolean yes) {
        useSystemMenuBar = yes;
    }

    /**
     * @return {@code true} if using system native menu bar appearance.
     */
    public static boolean isUsingSystemMenuBar() {
        return useSystemMenuBar;
    }

    /**
     * Sets a path to save settings data, which will override the default settings path
     * determined by {@link AppSettings} when the application starts up.
     *
     * @param path Settings data override path.
     */
    public static synchronized void setPreferredSettingsRootPath(@Nullable Path path) {
        GlobalHelper.preferredSettingsRootPath = path;
    }

    /**
     * @return Settings data override path.
     * @see #setPreferredSettingsRootPath(Path)
     */
    public static Optional<Path> getPreferredSettingsRootPath() {
        return Optional.ofNullable(preferredSettingsRootPath);
    }

    public static boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Initializes various core components as part of the startup procedure, before
     * any {@link AppWindow} is created.
     *
     * This step should only be performed once upon startup.
     */
    public static synchronized void initializeContext(@Nullable Application.Parameters parameters) {
        if (!isRunningAsTest()) {
            GlobalUncaughtExceptionHandler.initialize();
        }
        if (isInitialized && !isRunningAsTest()) {
            throw new IllegalStateException("initializeContext() should only be called " +
                    "once in production");
        }
        GlobalApplicationEventHandler.initialize(parameters);
        GlobalFontInitializer.loadBundledFonts();
        YiSkinManager.addExtraStylesheet("/codes/nibby/yi/app/fonts/font.css", GlobalHelper.class);
        YiFontManager.setDefaultFont(new Font("Noto Sans", 12d));
        AppText.installSupportedLanguages();
//        I18n.setCurrentLanguage(Language.getSupportedLanguages().get(1));
        YiSkinManager.useDefaultSkin();
        AppSettings.load();

        if (!isRunningAsTest()) {
            SoundManager.load(CommonAudioSets.Stones.CERAMIC_BICONVEX); // TODO: Temporary value, extract this to a setting
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            AppSettings.general.save();
            AppSettings.accelerator.save();
        }));

        isInitialized = true;

        if (!isRunningAsTest()) {
            GlobalApplicationEventHandler.loadAllQueuedOpenFiles();
        }
    }
}
