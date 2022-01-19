package codes.nibby.yi.app.audio;

import codes.nibby.yi.app.framework.ResourcePath;
import org.jetbrains.annotations.NotNull;
import codes.nibby.yi.models.StoneColor;

import java.util.HashMap;
import java.util.Map;

/**
 * A preset stone sound collection recorded using Korean ceramic bi-convex stones.
 */
final class CeramicBiconvexStoneAudioSet extends ExtendedStoneAudioSet {

    private static final int CAPTURE_ONE_STONE = 0;
    private static final int CAPTURE_TWO_STONES = 1;
    private static final int CAPTURE_THREE_STONES = 2;
    private static final int CAPTURE_SMALL_GROUP = 3;
    private static final int CAPTURE_BIG_GROUP = 4;

    private final ResourcePath resourceFolder = ResourcePath.SOUNDS.resolve("biconvex");

    @Override
    protected Map<Integer, String[]> getCaptureStoneSoundByCategory() {
        var captureSounds = new HashMap<Integer, String[]>();

        captureSounds.put(CAPTURE_ONE_STONE, new String[] {
                sound("captureOne"), sound("captureOne2")
        });

        captureSounds.put(CAPTURE_TWO_STONES, new String[] {
                sound("captureTwo"), sound("captureTwo2")
        });

        captureSounds.put(CAPTURE_THREE_STONES, new String[] {
                sound("captureThree"), sound("captureThree2")
        });

        captureSounds.put(CAPTURE_SMALL_GROUP, new String[] {
                sound("captureSmall"), sound("captureSmall2")
        });

        captureSounds.put(CAPTURE_BIG_GROUP, new String[] {
                sound("captureBig"), sound("captureBig2")
        });

        return captureSounds;
    }

    private String sound(String audioNameWithoutExtensionOrPrefix) {
        return resourceFolder.resolve(audioNameWithoutExtensionOrPrefix + ".mp3").getFilePath();
    }

    @Override
    protected int getCaptureStoneSoundCategory(int stonesCaptured, @NotNull StoneColor stoneColor) {
        if (stonesCaptured == 1) {
            return CAPTURE_ONE_STONE;
        } else if (stonesCaptured == 2) {
            return CAPTURE_TWO_STONES;
        } else if (stonesCaptured == 3) {
            return CAPTURE_THREE_STONES;
        } else if (stonesCaptured < 9) {
            return CAPTURE_SMALL_GROUP;
        } else {
            return CAPTURE_BIG_GROUP;
        }
    }

    @Override
    protected String[] getGameStartMediaEntries() {
        return new String[0];
    }

    @Override
    protected String[] getPlayMoveMediaEntries() {
        return new String[] {
            sound("play1"),
            sound("play2"),
            sound("play3"),
            sound("play4"),
            sound("play5"),
            sound("play6"),
            sound("play7"),
            sound("play8"),
            sound("play9"),
            sound("play10"),
            sound("play11"),
            sound("play12"),
        };
    }

    @Override
    protected String[] getResignMediaEntries() {
        return new String[0];
    }

    @Override
    protected String[] getPassMediaEntries() {
        return new String[0];
    }
}
