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
}