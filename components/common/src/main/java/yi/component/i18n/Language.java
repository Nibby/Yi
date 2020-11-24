package yi.component.i18n;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Supported languages for Yi applications.
 */
public enum Language {

    ENGLISH("English", Locale.ENGLISH),
    CHINESE_SIMPLIFIED("简体中文", Locale.SIMPLIFIED_CHINESE),
    CHINESE_TRADITIONAL("繁體中文", Locale.TRADITIONAL_CHINESE)
    ;

    private final String name;
    private final Locale locale;

    Language(String codename, Locale locale) {
        this.name = codename;
        this.locale = locale;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull Locale getLocale() {
        return locale;
    }
}
