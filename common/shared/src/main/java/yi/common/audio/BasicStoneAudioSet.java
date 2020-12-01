package yi.common.audio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.models.go.StoneColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A generic type of {@link StoneAudioSet} that provides some generic use cases for
 * stone audio playback.
 */
public abstract class BasicStoneAudioSet extends StoneAudioSet {

    /**
     * Stone sound use scenarios.
     */
    public enum SoundScenario {
        /** Played once when a new game begins. */
        GAME_START,

        /** Played each time a new move is submitted. */
        PLAY_MOVE,

        /** Played each time one or more stones are captured. */
        CAPTURE,

        /** Played each time a player passes. */
        PASS,

        /** Played once when a player resigns. */
        RESIGN
    }

    private final Map<SoundScenario, Supplier<String[]>> TYPE_VALUE_EXTRACTOR = new HashMap<>();
    {
        TYPE_VALUE_EXTRACTOR.put(SoundScenario.GAME_START, this::getGameStartMediaEntries);
        TYPE_VALUE_EXTRACTOR.put(SoundScenario.PLAY_MOVE, this::getPlayMoveMediaEntries);
        TYPE_VALUE_EXTRACTOR.put(SoundScenario.CAPTURE, this::getCapturedStoneEntries);
        TYPE_VALUE_EXTRACTOR.put(SoundScenario.PASS, this::getPassMediaEntries);
        TYPE_VALUE_EXTRACTOR.put(SoundScenario.RESIGN, this::getResignMediaEntries);
    }

    private final Map<SoundScenario, List<String>> entriesByType = new HashMap<>();

    @Override
    protected void enumerateMediaEntries() {
        for (SoundScenario soundScenario : SoundScenario.values()) {
            String[] entries = TYPE_VALUE_EXTRACTOR.get(soundScenario).get();
            for (String entry : entries) {
                this.addInternalResourceEntry(soundScenario, entry, this.getClass());
            }
        }
    }

    /**
     * @return All the resource paths to sounds that will be played under the
     * {@link SoundScenario#GAME_START} scenario.
     */
    protected abstract String[] getGameStartMediaEntries();

    /**
     * @return All the resource paths to sounds that will be played under the
     * {@link SoundScenario#PLAY_MOVE} scenario.
     */
    protected abstract String[] getPlayMoveMediaEntries();

    /**
     * @return All the resource paths to sounds that will be played under the
     * {@link SoundScenario#CAPTURE} scenario.
     */
    protected abstract String[] getCapturedStoneEntries();

    /**
     * @return All the resource paths to sounds that will be played under the
     * {@link SoundScenario#RESIGN} scenario.
     */
    protected abstract String[] getResignMediaEntries();

    /**
     * @return All the resource paths to sounds that will be played under the
     * {@link SoundScenario#PASS} scenario.
     */
    protected abstract String[] getPassMediaEntries();

    @Override
    public void playMoveSound(@NotNull StoneColor stoneColor) {
        playRandomized(SoundScenario.PLAY_MOVE);
    }

    @Override
    public void playCaptureSound(int captures, @NotNull StoneColor capturedStoneColor) {
        playRandomized(SoundScenario.CAPTURE);
    }

    @Override
    public void playPassSound() {
        playRandomized(SoundScenario.PASS);
    }

    @Override
    public void playResignSound() {
        playRandomized(SoundScenario.RESIGN);
    }

    private void playRandomized(SoundScenario soundScenario) {
        assertLoaded();
        List<String> entryKeys = entriesByType.get(soundScenario);
        assert entryKeys != null;
        playRandomized(entryKeys);
    }

    private void assertLoaded() {
        if (!isLoaded()) {
            throw new IllegalStateException("AudioSet is not loaded");
        }
    }

    /**
     * Plays a random sound in the provided list of sound entries.
     *
     * @param entryKeys List of sound resource entries to select a random sound from.
     */
    protected void playRandomized(@NotNull List<String> entryKeys) {
        assertLoaded();
        if (entryKeys.size() > 0) {
            int indexToPlay = (int) (Math.random() * entryKeys.size());
            String randomSoundEntryKey = entryKeys.get(indexToPlay);
            SoundEntry sound = getEntry(randomSoundEntryKey).orElseThrow();
            sound.play();
        }
    }

    /**
     * @implNote This is an inherited method from {@link StoneAudioSet} but for the purpose
     * of this class, it is too generic. You must use
     * {@link #addInternalResourceEntry(SoundScenario, String, Class)} instead.
     * <p/>
     * Calling this method in this class will generate an {@link UnsupportedOperationException}.
     *
     * @param soundResourcePath Classpath sound resource.
     * @param resourceClass Class used to load this resource. For resources in the same
     */
    @Override
    protected void addInternalResourceEntry(@NotNull String soundResourcePath,
                                            @Nullable Class<?> resourceClass) {
        throw new UnsupportedOperationException(
                "Use addInternalResourceEntry(SoundType, String, Class<?>) instead.");
    }

    /**
     * Wrapper call to {@link #addInternalResourceEntry(String, Class)} but also registers
     * the sound resource into different stone sound scenarios.
     *
     * @param soundScenario Scenario in which the sound applies.
     * @param entry Sound resource entry.
     * @param resourceClass Class used to load this resource.
     *
     * @see SoundScenario
     */
    protected void addInternalResourceEntry(@NotNull SoundScenario soundScenario,
                                            @NotNull String entry,
                                            @Nullable Class<?> resourceClass) {
        super.addInternalResourceEntry(entry, resourceClass);
        entriesByType.putIfAbsent(soundScenario, new ArrayList<>());
        entriesByType.get(soundScenario).add(entry);
    }
}
