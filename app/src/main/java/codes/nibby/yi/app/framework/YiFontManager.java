package codes.nibby.yi.app.framework;

import codes.nibby.yi.app.framework.Resource;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Global font handler.
 */
public final class YiFontManager {

    // These default values will make JavaFx load the system default font at the
    // default size
    private static Font DEFAULT_FONT = new Font("", -1);

    private static final Map<Class<?>, WeakReference<Font>> CACHED_FONTS = new HashMap<>();

    /**
     * Loads all the fonts in the directory and registers them in the JavaFx font
     * environment. Loaded fonts can then be initialized using {@code new Font()}. 
     * <p/>
     * @see Font#loadFont(String, double)
     *
     * @param fontDir Font directory, can be a classpath folder or absolute path.
     * @throws IOException When a directory stream cannot be created at the given path.
     */
    public static void loadFontsInDirectory(Path fontDir) throws IOException {
        DirectoryStream<Path> files = Files.newDirectoryStream(fontDir);
        for (Path file : files) {
            if (isFontFile(file)) {
                loadFont(file);
            }
        }
    }

    /**
     * Loads one font resource and register it in the JavaFx font environment. Loaded fonts
     * can then be initialized using {@code new Font()}.
     * <p/>
     * If the font is in a module outside of {@code yi.component.shared}, the module must
     * open the package the font resource is in. See javadoc of {@link Resource}
     * for details.
     *
     * @param fontResource Font resource path, must not be an alias.
     * @param resourceClass Class to be used to load this font.
     */
    public static void loadFont(String fontResource, Class<?> resourceClass) {
        InputStream inputStream = resourceClass.getResourceAsStream(fontResource);
        Font.loadFont(inputStream, -1);
    }

    /**
     * Loads one font file and register it in the JavaFx font environment. Loaded fonts
     * can then be initialized using {@code new Font()}.
     * <p/>
     * @see Font#loadFont(String, double)
     *
     * @param fontFile Font file; must not be an alias. Can be in classpath or absolute
     *                 path.
     * @throws IOException When an input stream cannot be created on the given path.
     */
    public static void loadFont(Path fontFile) throws IOException {
        Font.loadFont(Files.newInputStream(fontFile), -1);
    }

    /**
     * Creates a font of the given family at default size.
     *
     * @param familyName Font family name.
     * @return Requested font.
     */
    public static @NotNull Font getFont(String familyName) {
        return getFont(familyName, DEFAULT_FONT.getSize());
    }

    /**
     * Creates a font of the given family at a custom size.
     *
     * @param familyName Font family name.
     * @param fontSize Font size.
     * @return Requested font.
     */
    public static @NotNull Font getFont(String familyName, double fontSize) {
        var result = new Font(familyName, fontSize);
        return Objects.requireNonNull(result);
    }

    /**
     * Creates a font of the default font family at a custom size.
     *
     * @param size Font size.
     * @return Requested font.
     */
    public static Font getDefaultFont(double size) {
        return Font.font(DEFAULT_FONT.getName(), size);
    }

    /**
     * Creates a font of the default font family at a custom size, weight and italics
     * setting.
     *
     * @param size Font size.
     * @param weight Font boldness.
     * @param italics Italics or not.
     * @return Requested font.
     */
    public static Font getDefaultFont(double size, FontWeight weight, FontPosture italics) {
        return Font.font(DEFAULT_FONT.getName(), weight, italics, size);
    }

    private static boolean isFontFile(Path file) {
        return Files.isRegularFile(file) && file.getFileName().toString().endsWith(".ttf");
    }

    /**
     * Retrieves the font object cached for the given class. If no font is cached,
     * returns {@link Optional#empty()}. The cached font is stored as a weak reference,
     * so it is not always guaranteed to exist.
     *
     * @param aClass Class used to cache the font.
     * @return The cached font if it exists.
     */
    public static Optional<Font> getCachedFont(Class<?> aClass) {
        var cachedReference = CACHED_FONTS.get(aClass);
        if (cachedReference == null) {
            return Optional.empty();
        }
        var cachedValue = cachedReference.get();
        return Optional.ofNullable(cachedValue);
    }

    /**
     * Registers a font as the cached font for the given class. Each class can have up
     * to one cached font. The cached font is stored as a weak reference, so its expiry
     * is unpredictable.
     *
     * @param aClass Class used to cache the font.
     * @param font Font object to be cached.
     */
    public static void putCachedFont(Class<?> aClass, Font font) {
        CACHED_FONTS.put(aClass, new WeakReference<>(font));
    }

    /**
     * @return Default font used for this application.
     */
    public static Font getDefaultFont() {
        return DEFAULT_FONT;
    }

    /**
     * Adjusts the default font used for this application.
     *
     * @param defaultFont New default font, never null.
     */
    public static void setDefaultFont(@NotNull Font defaultFont) {
        DEFAULT_FONT = Objects.requireNonNull(defaultFont, "Default font cannot be null");
    }
}
