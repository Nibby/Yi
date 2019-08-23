package codes.nibby.yi.config;

import codes.nibby.yi.utility.AlertUtility;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.text.Font;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

/**
 * Stores data of a selected language pack inside 'resources/lang' directory.
 *
 * @author Kevin Yang
 * Created on 24 August 2019
 */
public class UiLanguage {

    private static final int FONTS_USED = 1;
    private static final String LANGUAGE_DIRECTORY = "/lang/";

    // Font categories, used to access font array
    public static final int FONT_STANDARD = 0;

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
        String langDirectory = LANGUAGE_DIRECTORY + locale + "/";
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
        String path = UiLanguage.class.getResource(langDirectory).getPath();
        File[] files = new File(path).listFiles();
        assert files != null;
        for (File file : files) {
            if (!file.getName().endsWith(".properties"))
                continue;
            ResourceBundle bundle;
            String fileName = file.getName().replace(".properties", "");
            String bundleName = langDirectory.replace("/", ".").substring(1, langDirectory.length()) + fileName;

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
