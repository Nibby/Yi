package yi.common.i18n;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.ResourceBundle;

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
    private final ResourceBundle resourceBundle;

    public TextResource(@NotNull String resourceKey, @NotNull ResourceBundle bundle) {
        this.resourceKey = Objects.requireNonNull(resourceKey);
        this.resourceBundle = Objects.requireNonNull(bundle);
    }

    public String getLocalisedText() {
        return resourceBundle.getString(resourceKey);
    }

    public String getLocalisedText(Object ... parameters) {
        String rawString = resourceBundle.getString(resourceKey);
        for (Object parameter : parameters) {
            rawString = rawString.replaceFirst("\\$\\{}", String.valueOf(parameter));
        }
        return rawString;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + " [localised: " + getLocalisedText()
                + ", bundle: " + resourceBundle.getBaseBundleName()
                               + "_" + resourceBundle.getLocale().toString()
                + "]";
    }
}
