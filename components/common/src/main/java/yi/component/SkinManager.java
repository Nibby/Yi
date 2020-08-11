package yi.component;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class SkinManager {

    private static final Skin DEFAULT_LIGHT = Skin.fromResources("/skins/megumi/", SkinManager.class).orElseThrow();

    private static final AtomicReference<Skin> USED_SKIN = new AtomicReference<>(null);

    private SkinManager() { }

    public static void useDefaultSkin() {
        useSkin(DEFAULT_LIGHT);
    }

    public static void useSkin(Skin skin) {
        Objects.requireNonNull(skin);
        USED_SKIN.set(skin);
    }

    public static Skin getUsedSkin() {
        return USED_SKIN.get();
    }

    // TODO: Support extra/overridable CSS?
}
