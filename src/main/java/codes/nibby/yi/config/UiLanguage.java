package codes.nibby.yi.config;

import codes.nibby.yi.utility.AlertUtility;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.text.Font;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

import static codes.nibby.yi.io.IoConstants.SEP;

/**
 * Stores data of a selected language pack inside 'resources/lang' directory.
 *
 * @author Kevin Yang
 * Created on 24 August 2019
 */
public class UiLanguage {

    // Font categories, used to access font array
    public static final int FONT_STANDARD = 0;
    private static final int FONTS_USED = 1;
    private static final String LANGUAGE_DIRECTORY = SEP + "lang" + SEP;
    private Font[] fonts;
    private Locale locale;

    // Contains all the key:value language translations
    private Map<String, ResourceBundle> resourceBundles = new HashMap<>();

    // The language-specific CSS files to be applied to the UI
    private String cssFile;

    public UiLanguage(String localeString) {
        String[] localeData = localeString.split("_");
        this.locale = new Locale(localeData[0], localeData[1]);
        fonts = new Font[FONTS_USED];

        // First load the config.json file
        String langDirectory = LANGUAGE_DIRECTORY + locale + SEP;
        String configFilePath = langDirectory + "config.json";
        Scanner scanner = new Scanner(UiLanguage.class.getResourceAsStream(configFilePath));
        StringBuilder buffer = new StringBuilder();
        while (scanner.hasNextLine()) {
            buffer.append(scanner.nextLine());
        }
        scanner.close();

        // Load all fonts as specified by the config
        JSONObject config = new JSONObject(buffer.toString());
        for (int i = 0; i < FONTS_USED; i++) {
            String fontValue = config.getString("font" + i);
            String fontFile = langDirectory + fontValue;
            fonts[i] = Font.loadFont(UiLanguage.class.getResourceAsStream(fontFile), 14);
        }

        // Add the language-specific CSS file to the list of UI applied CSS
        cssFile = langDirectory + "lang.css";
        UiStylesheets.add(cssFile);

        // Load all the resource bundles
        String pathString = UiLanguage.class.getResource(langDirectory).getPath();
        File file = new File(pathString);
        File[] files = file.listFiles();
        // Usually it's impossible for a local resource listing to fail unless Java fails to locate
        // the file internally. That is beyond our control.
        // Tested on macOS Catalina - I ran Yi on a separate volume (space character in the path
        // replaced by %20 instead).
        if (files == null) {
            AlertUtility.showAlert("Failed to load resource bundle for locale:" + this.locale.getDisplayScript()
                            + "\nCheck that you are not on a partition whose name contains a space!",
                    "Conf -> UiLanguage", Alert.AlertType.ERROR, ButtonType.OK);
            System.exit(1);
        }
        for (File f : files) {
            if (!f.getName().endsWith(".properties"))
                continue;
            ResourceBundle bundle;
            String fileName = f.getName().replace(".properties", "");
            String bundleName = langDirectory.replace(SEP, ".").substring(1, langDirectory.length()) + fileName;

            try {
                bundle = ResourceBundle.getBundle(bundleName, this.locale);
                resourceBundles.put(fileName, bundle);
            } catch (MissingResourceException e) {
                e.printStackTrace();
                AlertUtility.showAlert("Failed to load resource bundle for locale:" + this.locale.getDisplayScript()
                        + "\n" + e.getMessage(), "Conf -> UiLanguage", Alert.AlertType.ERROR, ButtonType.OK);
            }
        }
    }

    public ResourceBundle getResourceBundle(String resource) {
        return resourceBundles.get(resource);
    }

    public Font getFont(int fontType) {
        return fonts[fontType];
    }
}
