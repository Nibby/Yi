package yi.component.shared.audio;

/**
 * Collection of preset audio sets that can be loaded into different Yi applications.
 */
public final class CommonAudioSets {

    private CommonAudioSets() {
        // Constants collection class, no instantiation
    }

    /**
     * Collection of preset stone sound sets.
     */
    public static final class Stones {

        private Stones() {
            // Constants collection class, no instantiation
        }

        public static final StoneAudioSet CERAMIC_BICONVEX = new CeramicBiconvexStoneAudioSet();
    }

}
