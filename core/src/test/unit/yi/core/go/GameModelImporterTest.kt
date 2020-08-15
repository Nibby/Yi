package yi.core.go

import org.junit.jupiter.api.Test
import yi.core.go.docformat.FileFormat

class GameModelImporterTest {

    @Test
    fun `import regular sgf works`() {
        var gameModel = GameModelImporter.fromInternalResources("/sgf/agm_vs_agz.sgf", FileFormat.SGF, GameModelImporterTest::class.java)
    }

}