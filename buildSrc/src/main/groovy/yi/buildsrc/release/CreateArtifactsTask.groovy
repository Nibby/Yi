package yi.buildsrc.release

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task

class CreateArtifactsTask extends DefaultTask {

    static Task apply(Project project) {
        return project.task("createArtifacts") {
            doLast {

            }
        }
    }
}
