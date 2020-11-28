package yi.editor;

public class Yi {

    private static final Version VERSION = new Version(0, 0, 1, "alpha");
    private static final String PROGRAM_NAME = "Yi";

    public static Version getVersion() {
        return VERSION;
    }

    public static String getProgramName() {
        return PROGRAM_NAME;
    }

    public static boolean isRunningFromSource() {
        return System.getProperty("yi.fromSource", "false")
                .toLowerCase().equalsIgnoreCase("true");
    }

}
