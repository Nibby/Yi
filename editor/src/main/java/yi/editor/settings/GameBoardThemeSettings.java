package yi.editor.settings;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.json.JSONObject;
import yi.component.utilities.ComparisonUtilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Configurable options related to the aesthetics of the game board.
 */
public final class GameBoardThemeSettings extends SettingsModule {

    private static final String THEME_SETTINGS_FILE = "theme.json";

    // JSON keys
    private static final String KEY_BLACK_STONE_IMAGE = "blackStoneImage";
    private static final String KEY_WHITE_STONE_IMAGE = "whiteStoneImage";
    private static final String KEY_STONE_SHADOW_SIZE = "stoneShadowSize";
    private static final String KEY_STONE_SHADOW_BLUR = "stoneShadowBlur";
    private static final String KEY_STONE_SHADOW_COLOR = "stoneShadowColor";
    private static final String KEY_BOARD_IMAGE = "boardImage";
    private static final String KEY_BOARD_GRID_THICKNESS = "boardGridThickness";
    private static final String KEY_BOARD_GRID_COLOR = "boardGridColor";
    private static final String KEY_BACKGROUND_IMAGE = "backgroundImage";

    private Image blackStoneImage;
    private Image whiteStoneImage;

    private double stoneShadowSize;
    private double stoneShadowBlur;
    private Color stoneShadowColor;

    private Image boardImage;
    private double boardGridThickness;
    private Color boardGridColor;
    private Image backgroundImage;

    @Override
    public void load() {
        Path themeDirectory = Settings.getRootPath()
                                      .resolve(Settings.THEME_DIRECTORY_NAME)
                                      .resolve("board");
        if (!Files.exists(themeDirectory)) {
            try {
                Files.createDirectories(themeDirectory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Path selectedThemeDirectory = themeDirectory.resolve(getSelectedThemeDirectory());
        if (!Files.exists(selectedThemeDirectory)) {
            throw new IllegalStateException("Cannot find board theme: theme directory \""
                    + selectedThemeDirectory + "\" is missing!");
        }

        try {
            Settings.readJSON(selectedThemeDirectory.resolve(THEME_SETTINGS_FILE))
                    .ifPresent(settingsJson -> this.loadFromJson(selectedThemeDirectory, settingsJson));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFromJson(Path themeDirectory, JSONObject themeSettings) {
        stoneShadowSize = themeSettings.getDouble(KEY_STONE_SHADOW_SIZE);
        stoneShadowBlur = themeSettings.getDouble(KEY_STONE_SHADOW_BLUR);
        parseColor(themeSettings.getString(KEY_STONE_SHADOW_COLOR)).ifPresent(color -> stoneShadowColor = color);
        boardGridThickness = themeSettings.getDouble(KEY_BOARD_GRID_THICKNESS);
        parseColor(themeSettings.getString(KEY_BOARD_GRID_COLOR)).ifPresent(color -> boardGridColor = color);

        loadOptionalImage(themeDirectory, themeSettings, KEY_BLACK_STONE_IMAGE).ifPresent(image -> blackStoneImage = image);
        loadOptionalImage(themeDirectory, themeSettings, KEY_WHITE_STONE_IMAGE).ifPresent(image -> whiteStoneImage = image);
        loadOptionalImage(themeDirectory, themeSettings, KEY_BOARD_IMAGE).ifPresent(image -> boardImage = image);
        loadOptionalImage(themeDirectory, themeSettings, KEY_BACKGROUND_IMAGE).ifPresent(image -> backgroundImage = image);
    }

    /**
     * Converts a String of format "R,G,B,a" to a {@link Color} object.
     *
     * @param colorString The color string to parse
     * @return The color object representation
     */
    private Optional<Color> parseColor(String colorString) {
        String[] segments = colorString.split(",");
        if (segments.length >= 3) {
            int r = Integer.parseInt(segments[0]);
            int g = Integer.parseInt(segments[1]);
            int b = Integer.parseInt(segments[2]);
            double a = 1.0d;
            if (segments.length == 4) {
                a = Double.parseDouble(segments[3]);
            }

            double rr = trimToBounds(r / 255d);
            double gg = trimToBounds(g / 255d);
            double bb = trimToBounds(b / 255d);
            double aa = trimToBounds(a);
            return Optional.of(new Color(rr, gg, bb, aa));
        }
        return Optional.empty();
    }

    /**
     * Ensures that a given colorValue does not exceed permitted range.
     * Numbers near boundaries (difference less than {@link ComparisonUtilities#EPSILON}
     * will be rounded accordingly.
     *
     * @param colorValue Color value to format
     * @return Trimmed color value bounded between 0 to 1.0 inclusive.
     */
    private double trimToBounds(double colorValue) {
        final double epsilon = 0.00005d;

        if (colorValue - 1.0d > epsilon) {
            colorValue = 1.0d;
        }

        if (colorValue < epsilon) {
            colorValue = 0.0d;
        }
        return colorValue;
    }

    private Optional<Image> loadOptionalImage(Path themeDirectory, JSONObject themeSettings, String key) {
        if (themeSettings.has(key)) {
            Path imagePath = themeDirectory.resolve(themeSettings.getString(key));
            try {
                return Optional.of(new Image(Files.newInputStream(imagePath)));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    @Override
    public void save() {
        // Not applicable, themes cannot only be edited externally.
    }

    // For convenience, since it's intuitive to assume the theme directory information is stored here.
    public String getSelectedThemeDirectory() {
        return Settings.general.getSelectedBoardTheme();
    }

    public static String getDefaultThemeDirectory() {
        return "megumi";
    }

    public Image getBlackStoneImage() {
        return blackStoneImage;
    }

    public Image getWhiteStoneImage() {
        return whiteStoneImage;
    }

    public double getStoneShadowSize() {
        return stoneShadowSize;
    }

    public double getStoneShadowBlur() {
        return stoneShadowBlur;
    }

    public Color getStoneShadowColor() {
        return stoneShadowColor;
    }

    public Image getBoardImage() {
        return boardImage;
    }

    public double getBoardGridThickness() {
        return boardGridThickness;
    }

    public Color getBoardGridColor() {
        return boardGridColor;
    }

    public Image getBackgroundImage() {
        return backgroundImage;
    }
}
