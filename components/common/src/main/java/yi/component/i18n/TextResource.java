package yi.component.i18n;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a resource key for localised text in a resource bundle. At runtime, the
 * actual text can be retrieved using {@link #getLocalisedText()}, which picks the
 * {@link java.util.ResourceBundle} that corresponds to the current locale given by
 * {@link I18n#getCurrentLanguage()}.
 *
 * @see Language Managing supported languages
 */
public class TextResource {

    private final String resourceKey;
    private final String bundleName;

    public TextResource(@NotNull String resourceKey,
                        @NotNull String bundleName) {
        this.resourceKey = resourceKey;
        this.bundleName = bundleName;
    }

    public String getLocalisedText() {
        return I18n.getResourceBundle(bundleName).getString(resourceKey);
    }

    public String getLocalisedText(Object ... parameters) {
        String rawString = I18n.getResourceBundle(bundleName).getString(resourceKey);
        for (Object parameter : parameters) {
            rawString = rawString.replaceFirst("\\$\\{}", String.valueOf(parameter));
        }
        return rawString;
    }

}
