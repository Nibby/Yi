package codes.nibby.yi.app.audio;

import org.jetbrains.annotations.NotNull;
import codes.nibby.yi.models.StoneColor;

/**
 * An audio set that specialises in stone sound effects.
 */
public abstract class StoneAudioSet extends AudioSet {

    /**
     * Plays sound representing a game move has been submitted.
     *
     * @param stoneColor Color of the submitted stone.
     */
    public abstract void playMoveSound(@NotNull StoneColor stoneColor);

    /**
     * Plays sound representing some stones have been captured.
     *
     * @param captures Number of stones captured.
     * @param capturedStoneColor Color of stones captured.
     */
    public abstract void playCaptureSound(int captures, @NotNull StoneColor capturedStoneColor);

    /**
     * Plays sound representing a player has passed.
     */
    public abstract void playPassSound();

    /**
     * Plays sound representing a player has resigned.
     */
    public abstract void playResignSound();

}
