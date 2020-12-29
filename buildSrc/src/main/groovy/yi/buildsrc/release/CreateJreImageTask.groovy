package yi.buildsrc.release


import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Creates a custom JRE image using {@code jlink} tool that contains only the required
 * Java modules.
 */
class CreateJreImageTask {

    private static final String TASK_NAME = "createJreImage"
    private static final String EXTENSION_NAME = "jlinkOptions"

    static Task apply(Project project) {
        def ext = project.extensions.create(EXTENSION_NAME, CreateJreImageTaskExtension)

        Task result = project.task(TASK_NAME) {
            description: """Creates a custom JRE image using jlink"""

            doLast {
                run(project, ext)
            }
        }

        return result
    };

    private static void run(Project project, CreateJreImageTaskExtension ext) {
        project.logger.lifecycle "Creating custom JRE image for '$project.name'"

        Path jdkHome = getValidJdkHomeDirectory(ext.jdkHome)
        Path binDir = jdkHome.resolve("bin")
        Path jlinkExec = binDir.resolve("jlink")
        String jlinkPath = jlinkExec.toAbsolutePath().toString()
        String sep = File.separator

        project.logger.debug "sep=" + sep
        project.logger.debug "jlinkPath=" + jlinkPath

        String outputDir = ext.outputDir == null
                ? YiReleasePlugin.getReleaseDirectory(project) + "jre"
                : ext.outputDir

        project.logger.lifecycle "jlink output directory: $outputDir"

        ensureOutputDirDoesNotExist(project.logger, outputDir)

        List<String> cmdArray = createCommandArray(jlinkPath, ext.modules, outputDir)
        project.logger.debug "exec command: $cmdArray"
        project.logger.lifecycle("Executing jlink with modules: " + ext.modules)

        int exitCode = execute(project.logger, cmdArray)

        if (exitCode != 0) {
            throw new GradleException("Failed to create custom JRE via jlink, code: $exitCode")
        } else {
            project.logger.lifecycle "JRE image created successfully!"
        }
    }

    private static Path getValidJdkHomeDirectory(String customJdkHomeDirectory) {
        String javaHome = customJdkHomeDirectory

        if (javaHome == null) {
            println "No JDK home specified in extension configs, using JAVA_HOME instead"
            javaHome = System.getenv("JAVA_HOME")
        }

        if (javaHome == null) {
            throw new GradleException("No JDK home directory is specified. " +
                    "Configure one using extension: ${EXTENSION_NAME}.jdkHome " +
                    "or set your JAVA_HOME environment variable to a valid JDK path.")
        }

        Path javaHomePath = Paths.get(javaHome)
        String javaHomePathString = javaHomePath.toAbsolutePath().toString()

        println "Using JDK home path: $javaHomePathString"

        if (!Files.isDirectory(javaHomePath)) {
            throw new GradleException("Invalid JDK home path: " + javaHomePathString)
        }

        // Make sure this actually looks like a JDK home
        List<Path> requiredSubfolders = new ArrayList<Path>()
        requiredSubfolders.add(javaHomePath.resolve("bin"))
        requiredSubfolders.add(javaHomePath.resolve("jmods")) // Signs of a JDK 9+ version
        requiredSubfolders.add(javaHomePath.resolve("lib"))

        for (Path subfolder in requiredSubfolders) {
            if (!Files.isDirectory(subfolder)) {
                throw new GradleException("Invalid JDK home path: " + javaHomePathString)
            }
        }

        return javaHomePath
    }

    private static List<String> createCommandArray(String jlinkPath, List<String> modules, String outputDir) {
        def cmdArray = new ArrayList<String>()

        cmdArray.add(jlinkPath)
        cmdArray.add("--add-modules")
        for (String module in modules) {
            cmdArray.add(module)
        }
        cmdArray.add("--output")
        cmdArray.add(outputDir)

        return cmdArray
    }

    private static void ensureOutputDirDoesNotExist(Logger logger, String dir) {
        Path dirPath = Paths.get(dir)
        if (Files.exists(dirPath) || Files.isDirectory(dirPath)) {
            logger.lifecycle("Output directory already exists, deleting it...")
            BuildUtilities.removeDirectoryRecursive(dirPath)
            logger.lifecycle("Done.")
        }
    }

    private static int execute(Logger logger, List<String> cmdArray) {
        Process process = Runtime.getRuntime().exec((String[]) cmdArray.toArray(new String[0]))
        while (process.isAlive()) {
            Thread.sleep(100)
        }
        byte[] outputData = process.inputStream.readAllBytes()
        String outputMessage = new String(outputData)
        if (!outputMessage.isBlank()) {
            if (process.exitValue() != 0) {
                logger.error("jlink: " + outputMessage)
            } else {
                logger.lifecycle("jlink: " + outputMessage)
            }
        }

        return process.exitValue()
    }
}
