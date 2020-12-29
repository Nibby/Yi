package yi.buildsrc.release

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task

import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class PrepareArtifactsForPackagingTask extends DefaultTask {

    private static final String TASK_NAME = "prepareArtifactsForPackaging"

    /*
        At this stage we assume 'assembleDist' has run and the zip package is
        produced correctly. This step unzips the tar file and moves all *.jar in the
        lib directory to the 'release/${project.name}/artifacts' directory for packaging.
     */
    static Task apply(Project project) {
        return project.task(TASK_NAME) {
            doLast {
                run(project)
            }
        }
    }

    private static void run(Project project) {
        createMacOSPackagingEnvironment(project);
        copyAssembleDistArtifactsToReleaseDir(project);
    }

    private static void copyAssembleDistArtifactsToReleaseDir(Project project) {
        Path buildDir = project.buildDir.toPath()
        Path distDir = buildDir.resolve("distributions")
        DirectoryStream<Path> distArtifacts = Files.newDirectoryStream(distDir);
        Path zipFile = null
        for (Path artifact : distArtifacts) {
            if (artifact.getFileName().toString().endsWith(".zip")) {
                zipFile = artifact
                break
            }
        }

        if (zipFile == null) {
            throw new GradleException("No .zip or .tar files found in '$distDir', " +
                    "ensure '${project.path}:assembleDist' has been run before running " +
                    "'$TASK_NAME'.")
        }

        if (zipFile != null) {
            handleZip(project, zipFile)
        }
    }

    private static void handleZip(Project project, Path zipFile) {
        Path destination = zipFile.getParent().resolve("unzipped")
        unzip(zipFile, destination)
        DirectoryStream<Path> extractedContents = Files.newDirectoryStream(destination)

        Path extractedDir = null
        for (Path content in extractedContents) {
            if (Files.isDirectory(content)) {
                extractedDir = content
            }
        }

        assert extractedDir != null : "Extracted zip file but did not obtain a folder"

        Path sourceDir = extractedDir.resolve("lib")
        Path targetDir = YiReleasePlugin.getReleaseDirectoryAsPath(project).resolve("artifacts")
        copyAllArtifacts(sourceDir, targetDir)
    }

    private static void unzip(Path zipFile, Path destination) {
        byte[] buffer = new byte[1024]
        def inputStream = new ZipInputStream(Files.newInputStream(zipFile))
        ZipEntry entry = inputStream.getNextEntry()
        while (entry != null) {
            File newFile = new File(destination.toAbsolutePath().toString(), entry.getName())

            if (entry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile)
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent)
                }

                FileOutputStream fos = new FileOutputStream(newFile)
                int len
                while ((len = inputStream.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            entry = inputStream.getNextEntry();
        }
        inputStream.closeEntry()
        inputStream.close()
    }

    private static void copyAllArtifacts(Path source, Path destination) {
        assert Files.isDirectory(source)
        BuildUtilities.removeDirectoryRecursive(destination)
        BuildUtilities.copyDirectoriesRecursive(source, destination)
    }

    private static void createMacOSPackagingEnvironment(Project project) {
        Path releaseDir = YiReleasePlugin.getReleaseDirectoryAsPath(project)
        Path macOSDir = releaseDir.resolve("MacOS")
        if (!Files.exists(macOSDir)) {
            Files.createDirectories(macOSDir)
        }

        Path infoPlist = YiReleasePlugin.getPackagingDirectoryAsPath(project, TargetPlatform.macOS)
                .resolve("Info.plist")
        assert Files.exists(infoPlist) : "No Info.plist found in: $infoPlist"

        BuildUtilities.copyAndReplace(infoPlist, releaseDir.resolve("Info.plist"))
    }
}
