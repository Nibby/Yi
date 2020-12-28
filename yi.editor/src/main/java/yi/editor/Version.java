package yi.editor;

/**
 * Version information for the application. Uses semantic versioning.
 * See https://semver.org/ for reference.
 */
public final class Version {

    private final int major;
    private final int minor;
    private final int patch;
    private final String suffix;

    public Version(int major, int minor, int patch, String suffix) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.suffix = suffix;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    public String getSuffix() {
        return suffix;
    }

    @Override
    public String toString() {
        String version = major + "." + minor;
        if (patch > 0) {
            version += "." + patch;
        }
        if (!suffix.isEmpty()) {
            version += "-" + suffix;
        }
        return version;
    }
}
