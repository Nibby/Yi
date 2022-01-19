package codes.nibby.yi.app.framework;

import codes.nibby.yi.app.utilities.GuiUtilities;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.util.Objects;

public final class AppIcon extends ImageView {

    // Stones
    public static final AppIcon BLACK_STONE = standardIcon("blackStone32");
    public static final AppIcon WHITE_STONE = standardIcon("whiteStone32");

    // Node navigation
    public static final AppIcon ARROW_UP = standardIcon("arrowUp32");
    public static final AppIcon ARROW_DOWN = standardIcon("arrowDown32");

    public static final AppIcon ARROW_UP_DOUBLE = standardIcon("arrowUpDouble32");
    public static final AppIcon ARROW_DOWN_DOUBLE = standardIcon("arrowDownDouble32");

    public static final AppIcon ARROW_UP_EDGE = standardIcon("arrowUpmost32");
    public static final AppIcon ARROW_DOWN_EDGE = standardIcon("arrowDownmost32");

    // Edit tools
    public static final AppIcon TOOL_PLAY_MOVE = standardIcon("playMove32");
    public static final AppIcon TOOL_ADD_BLACK = standardIcon("addBlack32");
    public static final AppIcon TOOL_ADD_WHITE = standardIcon("addWhite32");
    public static final AppIcon TOOL_CROSS = standardIcon("cross32");
    public static final AppIcon TOOL_TRIANGLE = standardIcon("triangle32");
    public static final AppIcon TOOL_SQUARE = standardIcon("square32");
    public static final AppIcon TOOL_CIRCLE = standardIcon("circle32");
    public static final AppIcon TOOL_LABEL_LETTER = standardIcon("letter32");
    public static final AppIcon TOOL_LABEL_NUMBER = standardIcon("number32");
    public static final AppIcon TOOL_DIM = standardIcon("dim32");
    public static final AppIcon TOOL_LINE = standardIcon("line32");
    public static final AppIcon TOOL_ARROW = standardIcon("arrow32");

    // Misc
    public static final AppIcon PENCIL = standardIcon("pencil32");

    private final String fileExtension;
    private final String[] iconFilePath;
    private final int size;

    private final Flavor flavor;

    public AppIcon(int size, String fileExtension, String ... iconFilePath) {
        this(Flavor.AUTO_DETECT, size, fileExtension, iconFilePath);
    }

    public AppIcon(Flavor flavor, int size, String fileExtension, String ... iconFilePath) {
        this.iconFilePath = iconFilePath;
        this.fileExtension = fileExtension;
        this.flavor = flavor;
        this.size = size;

        updateIcon();
        setFitWidth(size);
        setFitHeight(size);
    }

    private void updateIcon() {
        var path = ResourcePath.ICONS;

        for (int i = 0; i < iconFilePath.length; i++) {
            String pathSegment = iconFilePath[i];

            if (i == iconFilePath.length - 1) {
                pathSegment = getIconFileByFlavor(pathSegment);
            }

            path = path.resolve(pathSegment);
        }

        Image icon = loadIconImage(path);
        setImage(icon);
    }

    private String getIconFileByFlavor(String baseFileName) {
        switch (flavor) {
            case DARK_MODE:
                return getDarkModeIconFileName(baseFileName);
            case LIGHT_MODE:
                return getLightModeIconFileName(baseFileName);
            case AUTO_DETECT:
                if (GuiUtilities.isDarkMode()) return getDarkModeIconFileName(baseFileName);
                else return getLightModeIconFileName(baseFileName);
        }

        throw new IllegalStateException("Unimplemented flavor type: " + flavor);
    }

    private String getDarkModeIconFileName(String baseFileName) {
        return baseFileName + "_darkMode." + fileExtension;
    }

    private String getLightModeIconFileName(String baseFileName) {
        return baseFileName + "." + fileExtension;
    }

    public AppIcon deriveIcon(int newSize) {
        return new AppIcon(newSize, fileExtension, iconFilePath);
    }

    public AppIcon getLightModeIcon() {
        return new AppIcon(Flavor.LIGHT_MODE, size, fileExtension, iconFilePath);
    }

    public AppIcon getDarkModeIcon() {
        return new AppIcon(Flavor.DARK_MODE, size, fileExtension, iconFilePath);
    }

    /**
     * Loads an icon image from anywhere inside the app project {@code resources} folder.
     * <p/>
     * If the icon is loaded successfully, a fit size will be set to indicate its preferred
     * dimensions when displayed on a {@link javafx.scene.Node}. This is useful for loading
     * high resolution icons for display at a lower dimension.
     *
     * @param iconFilePath Resource file path, notation should be the same as the parameter
     *                     used in {@link Class#getResourceAsStream(String)}.
     * @return The loaded icon if it exists and is loaded successfully.
     */
    private static Image loadIconImage(ResourcePath iconFilePath) {
        InputStream resourceStream;
        resourceStream = AppIcon.class.getResourceAsStream(iconFilePath.getFilePath());
        Objects.requireNonNull(resourceStream, "Invalid icon file path: " + iconFilePath);
        return new Image(resourceStream);
    }

    private static AppIcon standardIcon(String ... filePath) {
        return new AppIcon(16, "png", filePath);
    }

    public enum Flavor {
        LIGHT_MODE,
        DARK_MODE,
        AUTO_DETECT
    }
}
