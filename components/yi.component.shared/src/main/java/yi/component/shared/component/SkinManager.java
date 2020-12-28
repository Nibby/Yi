package yi.component.shared.component;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class SkinManager {

    private static final Skin SLATE = Skin.fromResources("/yi/component/shared/skins/slate/", SkinManager.class).orElseThrow();

    private static final AtomicReference<Skin> USED_SKIN = new AtomicReference<>(null);

    private SkinManager() { }

    public static void useDefaultSkin() {
        useSkin(SLATE);
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
