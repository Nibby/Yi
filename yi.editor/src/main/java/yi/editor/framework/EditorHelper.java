package yi.editor.framework;

import javafx.scene.text.Font;
import yi.component.shared.audio.CommonAudioSets;
import yi.component.shared.audio.SoundManager;
import yi.component.shared.component.FontManager;
import yi.component.shared.component.SkinManager;
import yi.component.shared.component.YiScene;
import yi.component.shared.utilities.SystemUtilities;
import yi.editor.EditorMain;
import yi.editor.components.EditorTextResources;
import yi.editor.framework.accelerator.EditorAcceleratorManager;
import yi.editor.settings.EditorSettings;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

/**
 * Global helper class that manages various startup configurations for the editor
 * application.
 */
public final class EditorHelper {

    private static final Version VERSION = new Version(0, 0, 1, "alpha");
    private static final String PROGRAM_NAME = "Yi";

    private static boolean useSystemMenuBar = SystemUtilities.isMac();
    private static boolean runningAsTest = false;

    private EditorHelper() {
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
        EditorHelper.runningAsTest = isRunningAsTest;
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
     * Initializes various core components as part of the startup procedure, before
     * any {@link yi.editor.EditorWindow} is created.
     *
     * This step should only be performed once upon startup.
     */
    public static void initializeContext() {
        loadBundledFonts();
        YiScene.addExtraStylesheet("/yi/editor/fonts/font.css", EditorHelper.class);
        EditorAcceleratorManager.initializeAll();
        FontManager.setDefaultFont(new Font("Noto Sans", 12d));
        EditorTextResources.installSupportedLanguages();
//        I18n.setCurrentLanguage(Language.getSupportedLanguages().get(1));
        SkinManager.useDefaultSkin();
        EditorSettings.load();

        if (!isRunningAsTest()) {
            SoundManager.load(CommonAudioSets.Stones.CERAMIC_BICONVEX); // TODO: Temporary value, extract this to a setting
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            EditorSettings.general.save();
            EditorSettings.accelerator.save();
        }));
    }

    private static void loadBundledFonts() {
        final String FONT_RESOURCE_DIR = "/yi/editor/fonts/";
        URI fontDirectoryUri;

        try {
            fontDirectoryUri = EditorMain.class.getResource(FONT_RESOURCE_DIR).toURI();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Malformed font resource directory value: " +
                    "\"" + FONT_RESOURCE_DIR + "\"");
        }

        var fontDirectoryAsPath = Paths.get(fontDirectoryUri);
        try {
            FontManager.loadFontsInDirectory(fontDirectoryAsPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
