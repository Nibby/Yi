package yi.common;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

public final class Resource {

    private final String resourcePath;
    private final Class<?> resourceLoaderClass;

    public Resource(@NotNull String resourcePath, @NotNull Class<?> resourceLoaderClass) {
        this.resourcePath = Objects.requireNonNull(resourcePath);
        this.resourceLoaderClass = Objects.requireNonNull(resourceLoaderClass);
    }

    public URL getResourceUrl() {
        return resourceLoaderClass.getResource(resourcePath);
    }

    public String getResourceUrlAsString() {
        return getResourceUrl().toString();
    }

    public InputStream getInputStream() {
        return resourceLoaderClass.getResourceAsStream(resourcePath);
    }
}
