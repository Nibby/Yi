package yi.buildsrc.release

import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path

final class BuildUtilities {

    static void removeDirectoryRecursive(Path dir) {
        if (Files.exists(dir)) {
            if (Files.isDirectory(dir)) {
                DirectoryStream<Path> contents = Files.newDirectoryStream(dir)
                for (Path content in contents) {
                    if (Files.isDirectory(content)) {
                        removeDirectoryRecursive(content)
                    } else {
                        Files.delete(content)
                    }
                }
            }
            Files.delete(dir)
        }
    }

    static void copyDirectoriesRecursive(Path source, Path destination) {
        assert Files.exists(source)

        if (!Files.exists(destination)) {
            Files.createDirectories(destination)
        }

        DirectoryStream<Path> srcContent = Files.newDirectoryStream(source)
        for (Path content in srcContent) {
            if (Files.isDirectory(content)) {
                copyDirectoriesRecursive(content, destination.resolve(content.getFileName()))
            } else {
                Files.copy(content, destination.resolve(content.getFileName()))
            }
        }
    }

    static void copyAndReplace(Path source, Path destination) {
        assert Files.exists(source)
        assert !Files.isDirectory(source)

        Files.deleteIfExists(destination)
        Files.copy(source, destination)
    }
}
