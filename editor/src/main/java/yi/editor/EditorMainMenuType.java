package yi.editor;

import org.jetbrains.annotations.NotNull;
import yi.common.i18n.TextResource;

public enum EditorMainMenuType {

    FILE(0, EditorTextResources.MENU_FILE),
    EDIT(1, EditorTextResources.MENU_EDIT),
    VIEW(2, EditorTextResources.MENU_VIEW),
    HELP(3, EditorTextResources.MENU_HELP),

    DEBUG(99, EditorTextResources.MENU_DEBUG) {
        @Override
        public boolean isVisible() {
            return EditorHelper.isDebugMode();
        }
    },
    TESTING(100, EditorTextResources.MENU_TESTING) {
        @Override
        public boolean isVisible() {
            return EditorHelper.isRunningAsTest();
        }
    }
    ;

    private final TextResource name;
    private final int order;

    EditorMainMenuType(int order, @NotNull TextResource name) {
        this.order = order;
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public String getLocalisedName() {
        return name.getLocalisedText();
    }

    public boolean isVisible() {
        return true;
    }

    public TextResource getName() {
        return name;
    }
}
