package codes.nibby.yi.app.framework;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tracks all preset component {@link YiSkin} available to the platform. Each
 * skin contains CSS files specific to the
 * type of aesthetics it aims to achieve.
 */
public final class YiSkinManager {

    private static final Set<Resource> EXTRA_STYLESHEETS = new HashSet<>();

    private static final YiSkin SLATE = YiSkin.fromResources(ResourcePath.SKINS.resolve("slate"), YiSkinManager.class).orElseThrow();

    private static final AtomicReference<YiSkin> USED_SKIN = new AtomicReference<>(null);

    private YiSkinManager() { }

    public static void useDefaultSkin() {
        useSkin(SLATE);
    }

    /**
     * Selects the skin to be applied to Yi components.
     *
     * @param skin Skin to use.
     */
    public static void useSkin(YiSkin skin) {
        Objects.requireNonNull(skin);
        USED_SKIN.set(skin);
    }

    /**
     * @return Selected skin to be applied.
     */
    public static Optional<YiSkin> getUsedSkin() {
        return Optional.ofNullable(USED_SKIN.get());
    }

    /**
     * Adds one additional CSS file entry to be used by all skins when applied to the
     * {@link YiWindow}. The file will be loaded using the class loader
     * in the module of the CSS file.
     *
     * @param cssFile Internal resource path for the CSS file.
     * @param resourceLoaderClass Class loader to use to load the resource. This class should
     *                            be in the same module as the CSS file.
     */
    public static void addExtraStylesheet(ResourcePath cssFile, Class<?> resourceLoaderClass) {
        var resource = new Resource(cssFile, resourceLoaderClass);
        EXTRA_STYLESHEETS.add(resource);
    }

    static Set<Resource> getExtraStylesheets() {
        return Collections.unmodifiableSet(EXTRA_STYLESHEETS);
    }
}
