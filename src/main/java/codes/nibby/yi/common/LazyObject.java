package codes.nibby.yi.common;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Supplier;

public class LazyObject<Type> {

    private Type object;
    private final Supplier<Type> initializer;

    public LazyObject(@NotNull Supplier<Type> initializer) {
        this.initializer = initializer;
    }

    public Optional<Type> get() {
        return Optional.ofNullable(object);
    }

    @NotNull
    public Type getOrInitialize() {
        if (object == null)
            object = initializer.get();

        return object;
    }

    public boolean isInitialized() {
        return get().isPresent();
    }
}
