package codes.nibby.yi.app.framework;

import codes.nibby.yi.app.i18n.I18n;
import codes.nibby.yi.app.i18n.TextResource;

import java.util.ResourceBundle;

/**
 * A collection of default component text.
 */
public final class YiPresetTextResource {

    private YiPresetTextResource() {

    }

    private static final ResourcePath I18N_PACKAGE = ResourcePath.I18N;
    private static final ResourcePath DIALOG_TRANSLATIONS_BUNDLE = I18N_PACKAGE.resolve("DialogTranslations");
    private static final ResourceBundle DIALOG_TRANSLATIONS = I18n.getResourceBundle(
        DIALOG_TRANSLATIONS_BUNDLE,
        YiPresetTextResource.class.getModule()
    );

    public static final TextResource EMPTY = resource("empty");
    public static final TextResource BUTTON_OKAY = resource("okay");
    public static final TextResource BUTTON_CANCEL = resource("cancel");
    public static final TextResource BUTTON_APPLY = resource("apply");

    private static TextResource resource(String i18nKey) {
        return new TextResource(i18nKey, DIALOG_TRANSLATIONS);
    }
}
