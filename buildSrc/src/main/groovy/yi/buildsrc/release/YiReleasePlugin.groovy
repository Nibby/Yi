package yi.buildsrc.release

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

final class YiReleasePlugin implements Plugin<Project> {

    protected static final String RELEASE_FOLDER = "release"
    protected static final String PACKAGING_FOLDER = "packaging"

    @Override
    void apply(Project project) {
        Task createJreImageTask = CreateJreImageTask.apply(project)

        Set<Task> assembleDistTaskHits = project.getTasksByName("assembleDist", false)
        int hitSize = assembleDistTaskHits.size()
        if (hitSize != 1) {
            throw new GradleException("Cannot identify unique assembleDist task, found $hitSize tasks of same name")
        }
        Task assembleDistTask = assembleDistTaskHits.iterator().next()
//        assembleDistTask.finalizedBy(createJreImageTask)

        Task prepareArtifactsTask = PrepareArtifactsForPackagingTask.apply(project)
        prepareArtifactsTask.dependsOn(assembleDistTask)
    }

    static String getReleaseDirectory(Project project) {
        String sep = File.separator
        String rootDir = project.rootProject.projectDir.toString()
        String releaseDir = rootDir + sep + RELEASE_FOLDER + sep
        String projectReleaseDir = releaseDir + project.name + sep

        Path path = Paths.get(projectReleaseDir)
        if (!Files.isDirectory(path)) {
            Files.createDirectories(path)
        }

        return projectReleaseDir
    }

    static Path getReleaseDirectoryAsPath(Project project) {
        return Paths.get(getReleaseDirectory(project))
    }

    static Path getPackagingDirectoryAsPath(Project project, TargetPlatform platform) {
        return project.projectDir.toPath().resolve(PACKAGING_FOLDER).resolve(platform.getName())
    }
}
