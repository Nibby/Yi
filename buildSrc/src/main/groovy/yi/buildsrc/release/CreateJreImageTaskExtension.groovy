package yi.buildsrc.release

class CreateJreImageTaskExtension {

    /**
     * Custom JDK home. If null, the value in JAVA_HOME will be used.
     */
    String jdkHome = null

    /**
     * Directory to store created JDK image.
     */
    String outputDir = null

    /**
     * File name of the main module containing the main class.
     */
    String mainModuleName = null

    /**
     * File name of the main executable jar artifact.
     */
    String mainJarArtifactName = null

    /**
     * Version of JDK to be packaged
     */
    String bundledJdkVersion = null
}
