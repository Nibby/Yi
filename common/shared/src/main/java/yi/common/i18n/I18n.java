package yi.common.i18n;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.spi.ResourceBundleProvider;

/**
 * Global internationalization handler for Yi application suite. Applications only work
 * with one current language, which can be adjusted using {@link #setCurrentLanguage(Language)}.
 * <p/>
 * A language resource bundle can be loaded using {@link #getResourceBundle(String)} or
 * its overloaded variants.
 *
 * @see TextResource Using localised text in components
 * @see Language Managing supported languages
 */
public class I18n {

    private static final AtomicReference<Language> currentLanguage = new AtomicReference<>(getDefaultLanguage());

    public static Language getDefaultLanguage() {
        return Language.ENGLISH;
    }

    public static Language getCurrentLanguage() {
        return currentLanguage.get();
    }

    public static void setCurrentLanguage(Language currentLanguage) {
        I18n.currentLanguage.set(currentLanguage);
    }
}
