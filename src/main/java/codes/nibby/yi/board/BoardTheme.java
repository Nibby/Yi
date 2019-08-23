package codes.nibby.qipan.board;

import codes.nibby.qipan.config.ConfigValueType;
import codes.nibby.qipan.utility.ColorUtility;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Represents a theme template found in the <strong>themes/</strong> directory.
 * The actual displayed theme is based on a template (provided by an instance of
 * this class) with some user customisation on top.
 *
 * @author Kevin Yang
 * Created on 23 August 2019
 */
public class BoardTheme {

    private static final String THEME_SETTINGS_FILE = "theme.json";
    private static final String KEY_NAME = "name";
    private static final String KEY_BACKGROUND = "background";
    private static final String KEY_BOARD = "board";
    private static final String KEY_BOARD_TEXTURE = "texture";
    private static final String KEY_STONE_BLACK = "stone_black";
    private static final String KEY_STONE_WHITE = "stone_white";
    private static final String KEY_GRID_COLOR = "grid_color";
    private static final String KEY_COORDINATES_COLOR = "coordinates_color";

    private Path directory;
    private JSONObject themeConfig;
    private String name;

    private Image boardTexture;
    private Image boardBackgroundTexture;

    private ConfigValueType stoneBlackConfig;
    private Image whiteStoneTexture;
    private ConfigValueType stoneWhiteConfig;
    private Image blackStoneTexture;

    private ConfigValueType gridColorConfig;
    private Color gridColor;
    private ConfigValueType coordColorConfig;
    private Color coordColor;

    public BoardTheme(Path themeDirectory) {
        this.directory = themeDirectory;
        load();
    }

    /**
     * Loads the theme data.
     * Can be called again to refresh.
     */
    private void load() {
        Path configFile = directory.resolve(THEME_SETTINGS_FILE);
        try (Scanner scan = new Scanner(configFile)) {
            StringBuilder buffer = new StringBuilder();
            while (scan.hasNextLine()) {
                buffer.append(scan.nextLine());
            }
            themeConfig = new JSONObject(buffer.toString());

            // Parses JSON data
            name = themeConfig.getString(KEY_NAME);

            // Parse background data
            // The absence of a config value means draw nothing
            boolean hasBackground = themeConfig.has(KEY_BACKGROUND);
            if (hasBackground) {
                String backgroundValue = themeConfig.getString(KEY_BACKGROUND);
                boardBackgroundTexture = new Image(Files.newInputStream(directory.resolve(backgroundValue)));
            }

            // Parse board texture
            JSONObject boardObj = themeConfig.getJSONObject(KEY_BOARD);
            {
                boolean hasTexture = boardObj.has(KEY_BOARD_TEXTURE);
                if (hasTexture) {
                    String textureValue = boardObj.getString(KEY_BOARD_TEXTURE);
                    boardTexture = new Image(Files.newInputStream(directory.resolve(textureValue)));
                }

                // Black and white stones are mandatory settings, cannot allow missing values
                String stoneBlackValue = boardObj.getString(KEY_STONE_BLACK);
                stoneBlackConfig = ConfigValueType.parse(stoneBlackValue);
                if (stoneBlackConfig.equals(ConfigValueType.OVERRIDE))
                    whiteStoneTexture = new Image(Files.newInputStream(directory.resolve(stoneBlackValue)));

                String stoneWhiteValue = boardObj.getString(KEY_STONE_WHITE);
                stoneWhiteConfig = ConfigValueType.parse(stoneWhiteValue);
                if (stoneWhiteConfig.equals(ConfigValueType.OVERRIDE))
                    blackStoneTexture = new Image(Files.newInputStream(directory.resolve(stoneWhiteValue)));

                // Grid color optional? Might be fun to play without the lines x)
                boolean hasGridColor = boardObj.has(KEY_GRID_COLOR);
                if (hasGridColor) {
                    String gridColorValue = boardObj.getString(KEY_GRID_COLOR);
                    gridColorConfig = ConfigValueType.parse(gridColorValue);
                    if (gridColorConfig.equals(ConfigValueType.OVERRIDE))
                        gridColor = ColorUtility.parseRGBA_255(gridColorValue);
                }

                boolean hasCoordColor = boardObj.has(KEY_COORDINATES_COLOR);
                if (hasCoordColor) {
                    String coordColorValue = boardObj.getString(KEY_GRID_COLOR);
                    coordColorConfig = ConfigValueType.parse(coordColorValue);
                    if (coordColorConfig.equals(ConfigValueType.OVERRIDE))
                        coordColor = ColorUtility.parseRGBA_255(coordColorValue);
                }
            }

        } catch (IOException e) {
            // TODO: better exception handling
            e.printStackTrace();
        }
    }

    public boolean isBlackStoneAppRendered() {
        return stoneBlackConfig.equals(ConfigValueType.APP_RENDERED);
    }

    public boolean isWhiteStoneAppRendered() {
        return stoneWhiteConfig.equals(ConfigValueType.APP_RENDERED);
    }

    public boolean isGridAppRendered() {
        return gridColorConfig.equals(ConfigValueType.APP_RENDERED);
    }

    public boolean isCoordAppRendered() {
        return coordColorConfig.equals(ConfigValueType.APP_RENDERED);
    }

    public boolean shouldDrawBackground() {
        return boardBackgroundTexture != null;
    }

    public boolean shouldDrawBoardTexture() {
        return boardTexture != null;
    }

    public boolean shouldDrawGrid() {
        return gridColor != null;
    }

    public String getName() {
        return name;
    }

    public Image getBoardTexture() {
        return boardTexture;
    }

    public Image getBoardBackgroundTexture() {
        return boardBackgroundTexture;
    }

    public Image getBlackStoneTexture() {
        return whiteStoneTexture;
    }

    public Image getWhiteStoneTexture() {
        return blackStoneTexture;
    }

    public Color getGridColor() {
        return gridColor;
    }

    public Color getCoordColor() {
        return coordColor;
    }
}
