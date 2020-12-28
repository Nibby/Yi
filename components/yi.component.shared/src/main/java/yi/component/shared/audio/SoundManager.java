package yi.component.shared.audio;

public final class SoundManager {

    public static void load(AudioSet ... audioSets) {
        for (AudioSet set : audioSets) {
            set.load();
        }
    }

    public static void unload(AudioSet ... audioSets) {
        for (AudioSet set : audioSets) {
            set.unload();
        }
    }

}
