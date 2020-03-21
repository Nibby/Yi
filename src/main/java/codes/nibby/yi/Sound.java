package codes.nibby.yi;

import javafx.scene.media.AudioClip;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sound {

    public enum Type {
        BOWL_OPEN,
        BOWL_CLOSE,
        STONE_RUSTLE_WHITE,
        STONE_RUSTLE_BLACK,
        STONE_PLACE_SINGLE,
//        STONE_PLACE_ADJACENT,
//        STONE_PLACE_SNAP,
//        STONE_COLLISION_WOBBLE,
//        STONE_COLLISION_WOBBLE_BIG,
        STONE_CAPTURE_SINGLE,
        STONE_CAPTURE_DOUBLE,
        STONE_CAPTURE_TRIPLE,
        STONE_CAPTURE_MULTIPLE,
        STONE_PUTBACK
    }

    private static final Map<Type, List<AudioClip>> AUDIO_DB = new HashMap<>();

    static {
        loadAudio(Type.STONE_PLACE_SINGLE, "/sound/biconvex/clack1.aiff");
        loadAudio(Type.STONE_PLACE_SINGLE, "/sound/biconvex/clack2.aiff");
        loadAudio(Type.STONE_PLACE_SINGLE, "/sound/biconvex/clack3.aiff");
        loadAudio(Type.STONE_PLACE_SINGLE, "/sound/biconvex/clack4.aiff");
        loadAudio(Type.STONE_PLACE_SINGLE, "/sound/biconvex/clack5.aiff");
        loadAudio(Type.STONE_PLACE_SINGLE, "/sound/biconvex/clack6.aiff");
        loadAudio(Type.STONE_PLACE_SINGLE, "/sound/biconvex/clack7.aiff");
        loadAudio(Type.STONE_PLACE_SINGLE, "/sound/biconvex/clack8.aiff");
        loadAudio(Type.STONE_PLACE_SINGLE, "/sound/biconvex/clack9.aiff");
        loadAudio(Type.STONE_PLACE_SINGLE, "/sound/biconvex/clack10.aiff");
        loadAudio(Type.STONE_PLACE_SINGLE, "/sound/biconvex/clack11.aiff");
        loadAudio(Type.STONE_PLACE_SINGLE, "/sound/biconvex/clack12.aiff");
        loadAudio(Type.STONE_PUTBACK, "/sound/biconvex/putback1.aiff");
        loadAudio(Type.STONE_PUTBACK, "/sound/biconvex/putback2.aiff");
        loadAudio(Type.STONE_PUTBACK, "/sound/biconvex/putback3.aiff");
        loadAudio(Type.STONE_PUTBACK, "/sound/biconvex/putback4.aiff");
        loadAudio(Type.STONE_PUTBACK, "/sound/biconvex/putback5.aiff");
        loadAudio(Type.STONE_PUTBACK, "/sound/biconvex/putback6.aiff");
        loadAudio(Type.STONE_PUTBACK, "/sound/biconvex/putback7.aiff");
        loadAudio(Type.STONE_RUSTLE_BLACK, "/sound/biconvex/rustle_black1.aiff");
        loadAudio(Type.STONE_RUSTLE_BLACK, "/sound/biconvex/rustle_black2.aiff");
        loadAudio(Type.STONE_RUSTLE_BLACK, "/sound/biconvex/rustle_black3.aiff");
        loadAudio(Type.STONE_RUSTLE_BLACK, "/sound/biconvex/rustle_black4.aiff");
        loadAudio(Type.STONE_RUSTLE_BLACK, "/sound/biconvex/rustle_black5.aiff");
        loadAudio(Type.STONE_RUSTLE_BLACK, "/sound/biconvex/rustle_black6.aiff");
        loadAudio(Type.STONE_RUSTLE_BLACK, "/sound/biconvex/rustle_black7.aiff");
        loadAudio(Type.STONE_RUSTLE_BLACK, "/sound/biconvex/rustle_black8.aiff");
        loadAudio(Type.STONE_RUSTLE_BLACK, "/sound/biconvex/rustle_black9.aiff");
        loadAudio(Type.STONE_RUSTLE_WHITE, "/sound/biconvex/rustle_white1.aiff");
        loadAudio(Type.STONE_RUSTLE_WHITE, "/sound/biconvex/rustle_white2.aiff");
        loadAudio(Type.STONE_RUSTLE_WHITE, "/sound/biconvex/rustle_white3.aiff");
        loadAudio(Type.STONE_RUSTLE_WHITE, "/sound/biconvex/rustle_white4.aiff");
        loadAudio(Type.STONE_RUSTLE_WHITE, "/sound/biconvex/rustle_white5.aiff");
        loadAudio(Type.STONE_RUSTLE_WHITE, "/sound/biconvex/rustle_white6.aiff");
        loadAudio(Type.STONE_RUSTLE_WHITE, "/sound/biconvex/rustle_white7.aiff");
        loadAudio(Type.STONE_RUSTLE_WHITE, "/sound/biconvex/rustle_white8.aiff");
        loadAudio(Type.STONE_RUSTLE_WHITE, "/sound/biconvex/rustle_white9.aiff");
    }

    public static AudioClip loadAudio(Type type, String res) {
        AudioClip clip;
        try {
            clip = new AudioClip(Sound.class.getResource(res).toURI().toString());
            AUDIO_DB.putIfAbsent(type, new ArrayList<>());
            AUDIO_DB.get(type).add(clip);
            return clip;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void playRandom(Type type) {
        AUDIO_DB.get(type).get((int) (Math.random() * AUDIO_DB.get(type).size())).play();
    }

    public static void playStonePlacement(ActionCallback callback) {
        new Thread(() -> {
            playRandom(Type.STONE_PLACE_SINGLE);
            if (callback != null)
                callback.performAction();
        }).start();
    }

    public interface ActionCallback {
        void performAction();
    }
}