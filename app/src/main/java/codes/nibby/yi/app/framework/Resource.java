package codes.nibby.yi.app.framework;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

/**
 * Represents a file in the app project {@code resources} folder. This class always
 * assumes the target is a file, so it calls {@link ResourcePath#getFilePath()} on
 * the supplied path.
 */
public final class Resource {

    private final ResourcePath path;
    private final Class<?> resourceLoaderClass;

    /**
     * Defines an internal resource from a given module.
     *
     * @param path Resource path.
     * @param resourceLoaderClass Class to be used to load the resource.
     */
    public Resource(@NotNull ResourcePath path, @NotNull Class<?> resourceLoaderClass) {
        this.path = Objects.requireNonNull(path);
        this.resourceLoaderClass = Objects.requireNonNull(resourceLoaderClass);
    }

    /**
     * @return URL path description of this resource.
     */
    @NotNull
    public URL getResourceUrl() {
        String filePath = path.getFilePath();
        URL url = resourceLoaderClass.getResource(filePath);
        Objects.requireNonNull(url, "Invalid resource path: " + path);
        return url;
    }

    /**
     * @return URL path description of this resource as String.
     */
    @NotNull
    public String getResourceUrlAsString() {
        return getResourceUrl().toString();
    }

    /**
     * @return {@link InputStream} for loading this resource. May be {@code null} if the
     * resource does not exist, or is not discoverable.
     */
    @NotNull
    public InputStream getInputStream() {
        String filePath = path.getFilePath();
        InputStream inputStream = resourceLoaderClass.getResourceAsStream(filePath);
        Objects.requireNonNull(inputStream, "Invalid resource path: " + path);
        return inputStream;
    }
}
