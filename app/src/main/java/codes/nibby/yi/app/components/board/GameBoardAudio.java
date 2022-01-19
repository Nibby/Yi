package codes.nibby.yi.app.components.board;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import codes.nibby.yi.app.framework.property.NullableProperty;
import codes.nibby.yi.app.audio.StoneAudioSet;
import codes.nibby.yi.models.StoneColor;

/**
 * Audio manager for a {@link GameBoardViewer}.
 */
public final class GameBoardAudio {

    // The current sound set in use. If null, no sounds will be played.
    private final NullableProperty<StoneAudioSet> audioSet = new NullableProperty<>(null);

    GameBoardAudio() {

    }

    /**
     * Sets the sounds to play during board actions such as submitting move.
     * This parameter can be null if no sound is to be played.
     *
     * @param audioSet An optional audio set to use for stone sounds.
     */
    public void set(@Nullable StoneAudioSet audioSet) {
        this.audioSet.set(audioSet);
    }

    /**
     * Plays sound to indicate a new game move has been submitted.
     *
     * @param stoneColor Color of the submitted move.
     */
    public void playMoveSound(@NotNull StoneColor stoneColor) {
        audioSet.get().ifPresent(audio -> audio.playMoveSound(stoneColor));
    }

    /**
     * Plays sound to indicate a stone capture has taken place.
     *
     * @param delayMs Time delay in milliseconds before playing this sound.
     * @param captures Number of stones captured.
     * @param stoneColorCaptured Color of stones captured.
     */
    public void playCaptureSound(int delayMs, int captures, StoneColor stoneColorCaptured) {
        if (delayMs == 0) {
            playCaptureSoundImpl(captures, stoneColorCaptured);
        } else {
            doDelayedTask(
                "playCaptureSound:" + captures,
                delayMs,
                () -> playCaptureSoundImpl(captures, stoneColorCaptured)
            );
        }
    }

    private void playCaptureSoundImpl(int captures, StoneColor stoneColorCaptured) {
        audioSet.get().ifPresent(audio -> audio.playCaptureSound(captures, stoneColorCaptured));
    }

    // TODO: Implement later
//    /**
//     * Plays sound to indicate a player has passed.
//     */
//    public void playPassSound() {
//        audioSet.get().ifPresent(StoneAudioSet::playPassSound);
//    }
//
//    /**
//     * Plays sound to indicate a player has resigned.
//     */
//    public void playResignSound() {
//        audioSet.get().ifPresent(StoneAudioSet::playResignSound);
//    }

    private void doDelayedTask(String threadName, int delayMs, Runnable task) {
        new Thread(() -> {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            task.run();
        }, threadName).start();
    }
}