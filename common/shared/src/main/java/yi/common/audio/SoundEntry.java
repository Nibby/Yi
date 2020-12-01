package yi.common.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Objects;

public final class SoundEntry {

    protected final Class<?> resourceClass;
    protected final String entryPath;
    protected Media media = null;
    protected boolean loadAttempted = false;

    public SoundEntry(@NotNull String entryPath, @Nullable Class<?> resourceClass) {
        this.entryPath = Objects.requireNonNull(entryPath, "Sound entry path cannot be null");
        this.resourceClass = resourceClass;
    }

    public void load() throws URISyntaxException {
        boolean pathIsClassResource = resourceClass != null;
        if (pathIsClassResource) {
            media = new Media(resourceClass.getResource(entryPath).toURI().toString());
        } else {
            media = new Media(Paths.get(entryPath).toAbsolutePath().toString());
        }
        loadAttempted = true;
    }

    public void play() {
        if (!loadAttempted) {
            throw new IllegalStateException("Cannot play sound '" + entryPath + "' " +
                    "because it has not been loaded.");
        }
        new MediaPlayer(media).play();
    }

    public void dispose() {
        media = null;
        loadAttempted = false;
    }
}