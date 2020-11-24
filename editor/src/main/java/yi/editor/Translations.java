package yi.editor;

import yi.component.i18n.I18n;

import java.util.ResourceBundle;

/**
 * Stores all the translation keys used by the Yi editor module. In other words, all
 * i18n translation keys are stored here.
 */
public final class Translations {

    private static final String I18N_PACKAGE = "i18n.";

    public static final class Menu {

        public static ResourceBundle getResourceBundle() {
            return I18n.getResourceBundle(I18N_PACKAGE + "menus");
        }

        public static final Translation MENU_FILE = new Translation("menu.file");
        public static final Translation MENU_EDIT = new Translation("menu.edit");
        public static final Translation MENU_TOOLS = new Translation("menu.tools");
        public static final Translation MENU_VIEW = new Translation("menu.view");
        public static final Translation MENU_WINDOW = new Translation("menu.window");
        public static final Translation MENU_HELP = new Translation("menu.help");

        public static final Translation ITEM_NEW_GAME = new Translation("item.newGame");
        public static final Translation ITEM_OPEN_GAME = new Translation("item.openGame");
        public static final Translation ITEM_SAVE_GAME = new Translation("item.saveGame");
        public static final Translation ITEM_SAVE_AS_GAME = new Translation("item.saveAsGame");
    }

    public static final class Translation {
        private final String resourceKey;

        private Translation(String resourceKey) {
            this.resourceKey = resourceKey;
        }

        /**
         * The localised text uses {@link I18n#getCurrentLanguage()} for the default
         * translation.
         *
         * @return The localised text for this translation item.
         */
        public String getLocalised() {
            return Menu.getResourceBundle().getString(resourceKey);
        }
    }

}
