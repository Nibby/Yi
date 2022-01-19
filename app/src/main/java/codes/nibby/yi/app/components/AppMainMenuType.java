package codes.nibby.yi.app.components;

import org.jetbrains.annotations.NotNull;
import codes.nibby.yi.app.i18n.TextResource;
import codes.nibby.yi.app.framework.global.GlobalHelper;
import codes.nibby.yi.app.framework.AppText;

public enum AppMainMenuType {

    FILE(0, AppText.MENU_FILE),
    EDIT(1, AppText.MENU_EDIT),
    NAVIGATE(2, AppText.MENU_NAVIGATE),
    TOOLS(3, AppText.MENU_TOOLS),
    VIEW(4, AppText.MENU_VIEW),
    HELP(5, AppText.MENU_HELP),

    DEBUG(99, AppText.MENU_DEBUG) {
        @Override
        public boolean isVisible() {
            return GlobalHelper.isDebugMode();
        }
    },
    TESTING(100, AppText.MENU_TESTING) {
        @Override
        public boolean isVisible() {
            return GlobalHelper.isRunningAsTest();
        }
    }
    ;

    private final TextResource name;
    private final int order;

    AppMainMenuType(int order, @NotNull TextResource name) {
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
