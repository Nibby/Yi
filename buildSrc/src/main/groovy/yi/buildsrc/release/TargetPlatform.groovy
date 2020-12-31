package yi.buildsrc.release

enum TargetPlatform {

    macOS("macOS"),
    Windows("win"),
    Linux("linux")

    ;

    private final String name

    TargetPlatform(String name) {
        this.name = name
    }

    String getName() {
        return name
    }

    static TargetPlatform getCurrentPlatform() {
        String osName = System.getProperty("os.name").toLowerCase()
        TargetPlatform platform = null
        if (osName.contains("win")) {
            platform = Windows
        } else if (osName.contains("mac")) {
            platform = macOS
        } else {
            // Assume linux
            platform = Linux
        }
        return platform
    }
}