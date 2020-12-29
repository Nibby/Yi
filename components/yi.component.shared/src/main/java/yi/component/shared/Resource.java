package yi.component.shared;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

/**
 * Represents an internal resource entry.
 *
 * @apiNote Since the migration to JPMS, resources across modules are no longer dumped
 * onto the {@code classpath}. For that reason, resource entries must associate their
 * resource path with the module the entry must be loaded from.
 * <p/>
 * For the resource to be accessible, the module providing the resource must open the
 * package the resource belongs to in its {@code module-info.java} like so:
 * <pre>opens resource.package.path;</pre>
 */
public final class Resource {

    private final String resourcePath;
    private final Class<?> resourceLoaderClass;

    /**
     * Defines an internal resource from a given module.
     *
     * @param resourcePath Resource path.
     * @param resourceLoaderClass Class to be used to load the resource.
     */
    public Resource(@NotNull String resourcePath, @NotNull Class<?> resourceLoaderClass) {
        this.resourcePath = Objects.requireNonNull(resourcePath);
        this.resourceLoaderClass = Objects.requireNonNull(resourceLoaderClass);
    }

    /**
     * @return URL path description of this resource.
     */
    public URL getResourceUrl() {
        return resourceLoaderClass.getResource(resourcePath);
    }

    /**
     * @return URL path description of this resource as String.
     */
    public String getResourceUrlAsString() {
        return getResourceUrl().toString();
    }

    /**
     * @return {@link InputStream} for loading this resource. May be {@code null} if the
     * resource does not exist, or is not discoverable.
     */
    public InputStream getInputStream() {
        return resourceLoaderClass.getResourceAsStream(resourcePath);
    }
}
