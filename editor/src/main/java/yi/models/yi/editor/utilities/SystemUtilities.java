package yi.models.yi.editor.utilities;

public final class SystemUtilities {

    private SystemUtilities() { }

    public static boolean isWindows() {
        return getOsName().toLowerCase().contains("win");
    }

    public static boolean isMac() {
        return getOsName().toLowerCase().contains("mac");
    }

    public static String getOsName() {
        return System.getProperty("os.name");
    }

}
