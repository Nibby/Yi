package yi.buildsrc.release

class CreateJreImageTaskExtension {

    /**
     * Custom JDK home. If null, the value in JAVA_HOME will be used.
     */
    String jdkHome = null

    /**
     * Java modules to be included in the image.
     */
    List<String> modules = new ArrayList<>()

    /**
     * Directory to store created JDK image.
     */
    String outputDir = null

    CreateJreImageTaskExtension() {
        modules.add("java.base");
    }
}
