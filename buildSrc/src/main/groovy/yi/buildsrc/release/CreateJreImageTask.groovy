package yi.buildsrc.release

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger

import java.nio.file.DirectoryStream
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
        Set<String> requiredModules = getRequiredModulesForProject(project, ext)
        createCustomJreImage(project, ext, requiredModules)
        if (TargetPlatform.getCurrentPlatform() == TargetPlatform.macOS) {
            createMacOsJreDirectoryStructure(project, ext)
        }
    }

    private static void createMacOsJreDirectoryStructure(Project project, CreateJreImageTaskExtension ext) {
        final String INFO_PLIST = "Info.plist"

        // These directories aren't really necessary for the JRE directory, but
        // the task fails if they are not present.
        Path outputDir = Paths.get(getJreImageOutputDirectory(project, ext)).getParent()
        Path macOSDir = outputDir.resolve("MacOS")
        if (!Files.exists(macOSDir)) {
            Files.createDirectories(macOSDir)
        }

        Path infoPlistFile = outputDir.resolve(INFO_PLIST)
        Files.deleteIfExists(infoPlistFile)
        Files.createFile(infoPlistFile)
    }

    private static Set<String> getRequiredModulesForProject(Project project, CreateJreImageTaskExtension ext) {
        project.logger.lifecycle "Retrieving required modules for project '${project.path}'"

        Path artifactsDir = YiReleasePlugin.getReleaseArtifactsDirectoryAsPath(project)
        String mainJarArtifactPath = artifactsDir.resolve(ext.mainJarArtifactName).toAbsolutePath().toString()
        Set<String> requiredModules = new HashSet<>()

        project.logger.lifecycle "Main jar artifact path: $mainJarArtifactPath"

        DirectoryStream<Path> artifacts = Files.newDirectoryStream(artifactsDir)
        if (artifacts.size() == 0) {
            throw new GradleException("Failed to retrieve required modules for project, no artifacts found in '$artifactsDir'!")
        }
        String modulePath = artifactsDir.toAbsolutePath().toString()
        if (!modulePath.endsWith(File.separator)) {
            modulePath += File.separator
        }

        String cmd = "jdeps -summary -recursive --multi-release ${ext.bundledJdkVersion} --module-path $modulePath ${mainJarArtifactPath}"
        project.logger.lifecycle "Executing jdeps command: $cmd"

        Process jdepsDependencyQuery = Runtime.getRuntime().exec(cmd)

        while (jdepsDependencyQuery.isAlive()) {
            Thread.sleep(100)
        }

        Scanner processOutput = new Scanner(jdepsDependencyQuery.getInputStream())
        while (processOutput.hasNextLine()) {
            String outputLine = processOutput.nextLine().trim()

            if (outputLine.startsWith("Warning")) {
                project.logger.lifecycle outputLine
                continue
            }
            String[] segments = outputLine.split(" -> ")
            String[] moduleDependencies = segments.last().trim().split(",")
            for (String moduleDependency in moduleDependencies) {
                if (!requiredModules.contains(moduleDependency)) {
                    if (isJdkModule(moduleDependency)) {
                        requiredModules.add(moduleDependency)
                        project.logger.lifecycle "Found new JDK module dependency: '$moduleDependency'"
                    } else {
                        project.logger.lifecycle "Ignored module dependency: '$moduleDependency' because it is not part of the JDK"
                    }
                }
            }
        }

        return requiredModules
    }

    private static boolean isJdkModule(String moduleName) {
        return moduleName.startsWith("jdk.") || moduleName.startsWith("java.")
    }

    private static void createCustomJreImage(Project project,
                                             CreateJreImageTaskExtension ext,
                                             Set<String> requiredModules) {

        project.logger.lifecycle "Creating custom JRE image for '$project.name'"

        Path jdkHome = getValidJdkHomeDirectory(ext.jdkHome)
        Path binDir = jdkHome.resolve("bin")
        Path jlinkExec = binDir.resolve("jlink")
        String jlinkPath = jlinkExec.toAbsolutePath().toString()
        String sep = File.separator

        project.logger.debug "sep=" + sep
        project.logger.debug "jlinkPath=" + jlinkPath

        String outputDir = getJreImageOutputDirectory(project, ext)

        project.logger.lifecycle "jlink output directory: $outputDir"

        ensureOutputDirDoesNotExist(project.logger, outputDir)

        List<String> cmdArray = createJLinkCommand(jlinkPath, requiredModules, outputDir)
        project.logger.debug "exec command: $cmdArray"
        project.logger.lifecycle("Executing jlink with modules: " + requiredModules)

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

    private static List<String> createJLinkCommand(String jlinkPath, Set<String> modules, String outputDir) {
        def cmdArray = new ArrayList<String>()

        cmdArray.add(jlinkPath)
        cmdArray.add("--add-modules")

        StringBuilder moduleParam = new StringBuilder()
        for (String module in modules) {
            moduleParam.append(module)
            if (module != modules.last()) {
                moduleParam.append(",")
            }
        }
        cmdArray.add(moduleParam.toString())
        cmdArray.add("--output")
        cmdArray.add(outputDir)

        return cmdArray
    }

    private static String getJreImageOutputDirectory(Project project, CreateJreImageTaskExtension extension) {
        if (extension.outputDir == null) {
            Path defaultDir = YiReleasePlugin.getCustomJreImageDirectoryAsPath(project)
            if (TargetPlatform.getCurrentPlatform() == TargetPlatform.macOS) {
                defaultDir = defaultDir.resolve("Contents").resolve("Home")
            }
            if (!Files.exists(defaultDir)) {
                Files.createDirectories(defaultDir)
            }
            return defaultDir.toAbsolutePath().toString()
        } else {
            return extension.outputDir
        }
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
