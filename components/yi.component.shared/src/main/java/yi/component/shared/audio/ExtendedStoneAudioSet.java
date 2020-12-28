package yi.component.shared.audio;

import org.jetbrains.annotations.NotNull;
import yi.core.go.StoneColor;

import java.util.*;

/**
 * Compared to {@link BasicStoneAudioSet}, this class provides additional context when
 * requesting sound clips so as to provide a more immersive sound experience.
 * <p/>
 * Implementations are expected to play different set of sounds for different number
 * of captured stones.
 */
public abstract class ExtendedStoneAudioSet extends BasicStoneAudioSet {

    private final Map<Integer, List<String>> capturedSoundByCategory = new HashMap<>();

    @Override
    protected final String[] getCapturedStoneEntries() {
        capturedSoundByCategory.clear();

        Map<Integer, String[]> capturedSoundByCategory = getCaptureStoneSoundByCategory();

        var allValues = new ArrayList<String>();
        for (Integer category : capturedSoundByCategory.keySet()) {
            var values = capturedSoundByCategory.get(category);
            List<String> valuesAsList = Arrays.asList(values);
            this.capturedSoundByCategory.put(category, valuesAsList);
            allValues.addAll(valuesAsList);
        }
        return allValues.toArray(new String[0]);
    }

    /**
     * Returns a map of stone capture sound resources where each group is identified by
     * a unique key. This is used in conjunction with {@link #getCapturedStoneEntries()}
     * to determine which category sound effect to use for the number of captures taking
     * place.
     * <p/>
     * For example, the {@code key=1} could represent sounds for 1 stone captures,
     * {@code key=2} for 2 stones etc.
     * <p/>
     * The key used in this map must be consistent with the {@code id} supplied by
     * {@link #getCapturedStoneEntries()}.
     *
     * @return Collection of stone capture sound resources separated by arbitrary id.
     */
    protected abstract Map<Integer, String[]> getCaptureStoneSoundByCategory();

    /**
     * Returns a numerical sound category identifier for the number of stones captured.
     * This determines which set of sound effects best describes the capture. The
     * choice of the integer key is purely arbitrary, as long as it is consistent with
     * the sound samples provided in {@link #getCaptureStoneSoundByCategory()}.
     *
     * @return A sound category key for the sound set to use for this capture quantity.
     */
    protected abstract int getCaptureStoneSoundCategory(int stonesCaptured, @NotNull StoneColor stoneColor);

    @Override
    public final void playCaptureSound(int captures, @NotNull StoneColor capturedStoneColor) {
        int category = getCaptureStoneSoundCategory(captures, capturedStoneColor);
        List<String> capturedSoundsForCategory = capturedSoundByCategory.get(category);
        playRandomized(capturedSoundsForCategory);
    }
}
