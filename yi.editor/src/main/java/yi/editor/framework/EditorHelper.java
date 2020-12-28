package yi.editor.framework;

import javafx.scene.text.Font;
import org.jetbrains.annotations.Nullable;
import yi.common.audio.CommonAudioSets;
import yi.common.audio.SoundManager;
import yi.common.component.FontManager;
import yi.common.component.SkinManager;
import yi.common.component.YiScene;
import yi.common.utilities.SystemUtilities;
import yi.editor.EditorMain;
import yi.editor.framework.accelerator.EditorAcceleratorManager;
import yi.editor.components.EditorTextResources;
import yi.editor.settings.EditorSettings;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public final class EditorHelper {

    private static final Version VERSION = new Version(0, 0, 1, "alpha");
    private static final String PROGRAM_NAME = "Yi";

    private static boolean useSystemMenuBar = SystemUtilities.isMac();
    private static boolean runningAsTest = false;
    private static Path preferredSettingsRootPath = null;

    private EditorHelper() {
        // Utility class, no instantiation
    }

    public static Version getVersion() {
        return VERSION;
    }

    public static String getProgramName() {
        return PROGRAM_NAME;
    }

    public static boolean isRunningFromSource() {
        return System.getProperty("yi.fromSource", "false").equalsIgnoreCase("true");
    }

    public static boolean isDebugMode() {
        return !isRunningFromSource();
    }

    public static synchronized void setRunningAsTest(boolean isRunningAsTest) {
        EditorHelper.runningAsTest = isRunningAsTest;
    }

    public static boolean isRunningAsTest() {
        return runningAsTest;
    }

    public static synchronized void setPreferredSettingsRootPath(@Nullable Path path) {
        EditorHelper.preferredSettingsRootPath = path;
    }

    public static Optional<Path> getPreferredSettingsRootPath() {
        return Optional.ofNullable(preferredSettingsRootPath);
    }

    public static void setUseSystemMenuBar(boolean yes) {
        useSystemMenuBar = yes;
    }

    public static boolean isUsingSystemMenuBar() {
        return useSystemMenuBar;
    }

    public static void initializeContext() {
        loadBundledFonts();
        YiScene.addExtraStylesheet("/fonts/font.css", EditorHelper.class);
        EditorAcceleratorManager.initializeAll();
        FontManager.setDefaultFont(new Font("Noto Sans", 12d));
        EditorTextResources.installSupportedLanguages();
//        I18n.setCurrentLanguage(Language.getSupportedLanguages().get(1));
        SkinManager.useDefaultSkin();
        EditorSettings.load();
        SoundManager.load(CommonAudioSets.Stones.CERAMIC_BICONVEX); // TODO: Temporary value, extract this to a setting

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            EditorSettings.general.save();
            EditorSettings.accelerator.save();
        }));
    }

    private static void loadBundledFonts() {
        final String FONT_RESOURCE_DIR = "/fonts/";
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
