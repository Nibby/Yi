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

    @Override
    void apply(Project project) {
        Task createJreImageTask = CreateJreImageTask.apply(project)
        Task createArtifactsTask = CreateArtifactsTask.apply(project)
        createJreImageTask.finalizedBy(createArtifactsTask)

        Set<Task> assembleDistTaskHits = project.getTasksByName("assembleDist", false)
        int hitSize = assembleDistTaskHits.size()
        if (hitSize != 1) {
            throw new GradleException("Cannot identify unique assembleDist task, found $hitSize tasks of same name")
        }
        Task assembleDistTask = assembleDistTaskHits.iterator().next()
        assembleDistTask.finalizedBy(createJreImageTask)
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

}
