package codes.nibby.yi.app.i18n;

import codes.nibby.yi.app.framework.ResourcePath;

import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Global internationalization handler for Yi application suite. Applications only work
 * with one current language, which can be adjusted using {@link #setCurrentLanguage(Language)}.
 * <p/>
 * A language resource bundle can be loaded using {@link #getResourceBundle(ResourcePath, Module)} or
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

    /**
     * Retrieves a resource bundle under the current language given by
     * {@link I18n#getCurrentLanguage()}. The bundle name can be period (.)-delimited
     * to indicate the bundle file is in a subdirectory.
     *
     * @param bundleResource Name of the resource bundle.
     * @param module Module to load resource from.
     * @return A {@link ResourceBundle} corresponding to the bundle name.
     *
     * @see ResourceBundle#getBundle(String)
     */
    public static ResourceBundle getResourceBundle(ResourcePath bundleResource, Module module) {
        return getResourceBundle(bundleResource, I18n.getCurrentLanguage(), module);
    }

    /**
     * Retrieves a resource bundle for an arbitrary language. The bundle name can be
     * period (.)-delimited to indicate the bundle file is in a subdirectory.
     *
     * @param bundleResource Fully qualified name of the resource bundle.
     * @param language Language for the resource bundle.
     * @param module Module to load resource from.
     * @return A {@link ResourceBundle} corresponding to the bundle name.
     *
     * @see ResourceBundle#getBundle(String)
     */
    public static ResourceBundle getResourceBundle(ResourcePath bundleResource, Language language, Module module) {
        return ResourceBundle.getBundle(bundleResource.getPackagePath(), language.getLocale(), module);
    }
}
