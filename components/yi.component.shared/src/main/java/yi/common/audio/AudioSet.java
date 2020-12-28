package yi.common.audio;


import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a collection of sound effect clips that can be played. Once an audio set is
 * instantiated, its sound clips can be loaded to memory using {@link SoundManager#load(AudioSet...)}.
 * Clips must be loaded in order to play.
 * <p/>
 * Each set contains a map of sound entries identified by a unique path resource. The path
 * must be an internal resource (added via {@link #addInternalResourceEntry(String, Class)}
 * which is then loaded using the class loader when requested.
 * <p/>
 * This class does not expose public methods to add new entries. It is recommended to
 * create specialised subclasses for domain-specific playback.
 */
public abstract class AudioSet {

    private final Map<String, SoundEntry> soundMap = new HashMap<>();
    private boolean loaded = false;

    /**
     * Invoked once when {@link #load()} is called to request all the sound clips part
     * of this audio set to be registered in the sound map through
     * {@link #addInternalResourceEntry(String, Class)}.
     * <p/>
     * The added entries will then have its media contents loaded into memory. Entries
     * not registered in this process will not be considered part of this audio set.
     */
    protected abstract void enumerateMediaEntries();

//    protected void addExternalFileEntry(@NotNull Path filePath) {
//        var pathString  = filePath.toAbsolutePath().toString();
//        soundMap.put(pathString, new SoundEntry(pathString, null));
//    }

    /**
     * Adds an internal sound clip resource to this audio set. An internal resource is
     * one stored under the {@code resources} folder or some other folder part of the
     * classpath.
     *
     * @param soundResourcePath Classpath sound resource.
     * @param resourceClass Class used to load this resource. For resources in the same
     *                      module as the class, use {@link #getClass()}.
     */
    protected void addInternalResourceEntry(@NotNull String soundResourcePath, Class<?> resourceClass) {
        soundMap.put(soundResourcePath, new SoundEntry(soundResourcePath, resourceClass));
    }

    /**
     * Retrieves an added sound entry from the sound map. If the requested resource is
     * not registered, returns {@link Optional#empty()}.
     *
     * @param soundResourcePath Unique resource identifier. This is the same path
     *                          value used to register the entry.
     * @return A registered sound entry if it exists.
     */
    protected Optional<SoundEntry> getEntry(@NotNull String soundResourcePath) {
        return Optional.ofNullable(soundMap.get(soundResourcePath));
    }

    /**
     * Requests the resource represented by the sound resource path be played back.
     *
     * @param soundResourcePath Unique resource identifier. This is the same path value
     *                          used to register the entry.
     */
    protected void playSound(@NotNull String soundResourcePath) {
        var soundEntry = soundMap.get(soundResourcePath);
        if (soundEntry != null) {
            soundEntry.play();
        }
    }

    /**
     * Loads all the sound clips under this audio set. This method must be called once
     * before playing sounds.
     */
    protected final synchronized void load() {
        enumerateMediaEntries();
        for (SoundEntry entry : soundMap.values()) {
            try {
                entry.load();
            } catch (URISyntaxException e) {
                // Caused by malformed path string from class.getResource(entryPath)
                // Chances are the entry path was not correct.
                throw new IllegalArgumentException("Malformed internal entry path: " +
                        "'" + entry.entryPath + "'", e);
            }
        }
        loaded = true;
    }

    /**
     * Disposes all the sound clips that were previously loaded into memory. This method
     * is useful to free up resources when the audio set is no longer used.
     */
    protected synchronized final void unload() {
        for (SoundEntry entry : soundMap.values()) {
            entry.dispose();
        }
    }

    /**
     * @return true if {@link #load()} has been called at least once.
     */
    protected boolean isLoaded() {
        return loaded;
    }
}
