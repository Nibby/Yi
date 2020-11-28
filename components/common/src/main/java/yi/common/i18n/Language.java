package yi.common.i18n;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Manages all the supported locales for one application. All Yi applications support
 * English ("en") by default. Additional languages can be added using {@link #add(Language)}.
 */
public class Language {
    
    private static final List<Language> SUPPORTED_LANGUAGES = new ArrayList<>();

    public static final Language ENGLISH = new Language("English", Locale.ENGLISH);

    static {
        add(ENGLISH);
    }


    private final String name;
    private final Locale locale;

    public Language(String name, Locale locale) {
        this.name = name;
        this.locale = locale;
    }

    public String getName() {
        return name;
    }

    public Locale getLocale() {
        return locale;
    }

    @Override
    public String toString() {
        return name + " (" + locale + ")";
    }

    public static synchronized void add(Language language) {
        SUPPORTED_LANGUAGES.add(language);
    }

    public static List<Language> getSupportedLanguages() {
        return Collections.unmodifiableList(SUPPORTED_LANGUAGES);
    }
    
}
