package yi.core.go.docformat

import yi.core.go.*
import yi.core.go.Annotation
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/*
    See SGF format specification: https://www.red-bean.com/sgf/user_guide/index.html
 */
internal class SgfFileFormatHandler : FileFormatHandler {

    override fun getUserFriendlyName(): String {
        return "smart go file"
    }

    override fun getAbbreviatedName(): String {
        return "sgf"
    }

    override fun getSupportedFileNameExtensions(): Set<String> {
        return setOf("sgf")
    }

    override fun isLikelyLoadable(file: InputStream): Boolean {
        TODO("Not yet implemented")
    }

    override fun doImport(file: InputStream): GameModel {
        val reader = file.reader(Charset.defaultCharset()) // TODO: The SGF file may specify the charset to use in the root node, maybe pre-scan the document and set it first?
        return SgfImporter.doImport(reader)
    }

    override fun doExport(gameModel: GameModel, destination: OutputStream) {
        TODO("Not yet implemented")
    }

    companion object {
        private const val BRANCH_START = '('
        private const val BRANCH_END = ')'
        private const val NODE_START = ';'
        private const val TAG_VALUE_START = '['
        private const val TAG_VALUE_END = ']'

        private const val SGF_BOARD_SIZE = "SZ"
        private const val SGF_RULESET = "RU"
        private const val SGF_BLACK_MOVE = "B"
        private const val SGF_WHITE_MOVE = "W"
        private const val SGF_ADD_BLACK = "AB"
        private const val SGF_ADD_WHITE = "AW"
        private const val SGF_ADD_ERASE = "AE" // OGS makes use of this to remove a stone at a position
        private const val SGF_MARKUP_CROSS = "MA"
        private const val SGF_MARKUP_TRIANGLE = "TR"
        private const val SGF_MARKUP_CIRCLE = "CR"
        private const val SGF_MARKUP_SQUARE = "SQ"
        private const val SGF_MARKUP_LABEL = "LB"
        private const val SGF_MARKUP_LETTER = "L" // Old, TODO: support it?
        private const val SGF_MARKUP_DIM = "DD"
        private const val SGF_MARKUP_ARROW = "AR"
        private const val SGF_MARKUP_LINE = "LN"
        private const val SGF_MARKUP_SL = "SL" // Old, TODO: What's this?
    }

    private object SgfImporter {
        fun doImport(reader: InputStreamReader): GameModel {
            var charCode: Int
            var char: Char? = null
            var gameModel: GameModel? = null

            // Parsing steps
            val branchStack = Stack<SgfBranch>()
            var readCharNextLoop = true

            do {
                if (readCharNextLoop) {
                    charCode = reader.read()
                    char = charCode.toChar()

                    if (charCode == -1) {
                        break
                    }
                }

                if (char == BRANCH_START) {
                    val parent = if (branchStack.isEmpty()) null else branchStack.peek().latestNode
                    branchStack.push(SgfBranch(parent, parent))
                    readCharNextLoop = true
                } else if (char == BRANCH_END) {
                    branchStack.pop()
                    readCharNextLoop = true
                } else if (char == NODE_START) {
                    val readResult = readUntil(reader, BRANCH_START, BRANCH_END, NODE_START)

                    val nodeData = readResult.first
                    char = readResult.second

                    val organizedNodeData = organizeNodeData(nodeData)
//                    println("Read $nodeData")
//                    println("$organizedNodeData")
//                    println()

                    // We're already at the next token, so we want the next loop to evaluate this.
                    readCharNextLoop = false

                    if (gameModel == null) {
                        if (branchStack.size != 1) {
                            throw BadFormatException("No main branch defined before providing node definition")
                        }

                        // Root node, create the model and setup the rest
                        gameModel = createGameModel(organizedNodeData)
                        branchStack.peek().parentNode = gameModel.getRootNode()
                        branchStack.peek().latestNode = gameModel.getRootNode()
                        continue
                    } else {
                        // Standard node
                        val nodeParent = branchStack.peek().latestNode!!
                        val node = parseNode(organizedNodeData, nodeParent, gameModel)
                        gameModel.appendNode(nodeParent, node)
                        branchStack.peek().latestNode = node
                    }
                }

            } while (true)

            if (gameModel == null) {
                throw BadFormatException("Premature end of file")
            }

            return gameModel
        }

        private fun createGameModel(rootNodeData: SgfNodeData): GameModel {
            val boardSize = rootNodeData.getOrDefault(SGF_BOARD_SIZE, listOf("19", "19"))
            val ruleset = rootNodeData.getOrDefault(SGF_RULESET, listOf("")) // Fall through to default ruleset if empty

            val width: Int
            val height: Int

            try {
                when {
                    boardSize.size == 1 -> {
                        width = Integer.parseInt(boardSize[0])
                        height = width
                    }
                    boardSize.size > 1 -> {
                        width = Integer.parseInt(boardSize[0])
                        height = Integer.parseInt(boardSize[1])
                    }
                    else -> {
                        // This assumption appears to be a convention
                        width = 19
                        height = 19
                    }
                }

                val gameRules = GameRules.parse(ruleset[0]).orElse(GameRules.CHINESE)
                val gameModel = GameModel(width, height, gameRules)

                // TODO: Enumerate other node data here

                return gameModel
            } catch (e: NumberFormatException) {
                throw BadFormatException("Invalid board size values: $boardSize")
            }
        }

        private fun parseNode(nodeData: SgfNodeData, parentNode: GameNode, gameModel: GameModel): GameNode {
            val gameNode = parseNodeType(nodeData, parentNode, gameModel)
            parseHelperStones(nodeData, gameNode, gameModel)
            parseAnnotations(nodeData, gameNode, gameModel)

            val unusedData = nodeData.getUnusedData()
            gameNode.putMetadata(unusedData)

            return gameNode
        }

        private fun parseAnnotations(nodeData: SgfNodeData, gameNode: GameNode, gameModel: GameModel) {
            val annotations = ArrayList<Annotation>()

            annotations.addAll(nodeData.getOrDefault(SGF_MARKUP_CROSS, listOf()).map {
                val pos = convertCoordinates(it)
                Annotation.Cross(pos[0], pos[1])
            })
            annotations.addAll(nodeData.getOrDefault(SGF_MARKUP_CIRCLE, listOf()).map {
                val pos = convertCoordinates(it)
                Annotation.Circle(pos[0], pos[1])
            })
            annotations.addAll(nodeData.getOrDefault(SGF_MARKUP_TRIANGLE, listOf()).map {
                val pos = convertCoordinates(it)
                Annotation.Triangle(pos[0], pos[1])
            })
            annotations.addAll(nodeData.getOrDefault(SGF_MARKUP_SQUARE, listOf()).map {
                val pos = convertCoordinates(it)
                Annotation.Square(pos[0], pos[1])
            })
            annotations.addAll(nodeData.getOrDefault(SGF_MARKUP_DIM, listOf()).map {
                val pos = convertCoordinates(it)
                Annotation.Fade(pos[0], pos[1])
            })

            val labels = nodeData.getOrDefault(SGF_MARKUP_LABEL, listOf())
            for (label in labels) {
                val data = label.split(":")
                if (data.size == 2) {
                    val pos = convertCoordinates(data[0])
                    val text = data[1]

                    annotations.add(Annotation.Label(pos[0], pos[1], text))
                }
            }

            val twoPointAnnotationConverter = { data: String, positionConsumer: (Array<Int>, Array<Int>) -> Unit ->
                val segments = data.split(":")
                if (segments.size == 2) {
                    val posFrom = convertCoordinates(segments[0])
                    val posTo = convertCoordinates(segments[1])

                    positionConsumer.invoke(posFrom, posTo)
                }
            }

            nodeData.getOrDefault(SGF_MARKUP_LINE, listOf()).forEach { data ->
                twoPointAnnotationConverter.invoke(data) { from, to -> annotations.add(Annotation.Line(from[0], from[1], to[0], to[1])) }
            }
            nodeData.getOrDefault(SGF_MARKUP_ARROW, listOf()).forEach { data ->
                twoPointAnnotationConverter.invoke(data) { from, to -> annotations.add(Annotation.Arrow(from[0], from[1], to[0], to[1])) }
            }

            gameModel.addAnnotations(gameNode, annotations)
        }

        /*
            Parses all the helper stone data (AB and AW) for the node
         */
        private fun parseHelperStones(nodeData: SgfNodeData, gameNode: GameNode, gameModel: GameModel) {
            val demoBlackStones = nodeData.getOrDefault(SGF_ADD_BLACK, listOf()).map { convertCoordinates(it) }
            val demoWhiteStones = nodeData.getOrDefault(SGF_ADD_WHITE, listOf()).map { convertCoordinates(it) }
            val eraseStones = nodeData.getOrDefault(SGF_ADD_ERASE, listOf()).map { convertCoordinates(it) }

            demoBlackStones.forEach {
                gameModel.addStoneEdit(gameNode, Stone(it[0], it[1], StoneColor.BLACK))
            }

            demoWhiteStones.forEach {
                gameModel.addStoneEdit(gameNode, Stone(it[0], it[1], StoneColor.WHITE))
            }

            eraseStones.forEach {
                val x = it[0]
                val y = it[1]

                // Remove existing helper stones first
                gameModel.removeStoneEdit(gameNode, Stone(x, y, StoneColor.BLACK))
                gameModel.removeStoneEdit(gameNode, Stone(x, y, StoneColor.WHITE))

                gameModel.addStoneEdit(gameNode, Stone(x, y, StoneColor.NONE))
            }
        }

        /*
            Yi makes some arbitrary distinctions between different node types, where each node type represents the main information stored on that node.
            This information is interpreted from the presence (or absence) of notable tag keys and values.

            This step must be performed first before parsing other type of node information as this will create the node itself.
         */
        private fun parseNodeType(nodeData: SgfNodeData, parentNode: GameNode, gameModel: GameModel): GameNode {
            var gameNode: GameNode? = null
            var gamePrimaryMove: Stone? = null
            var gameNodeType: GameNodeType = GameNodeType.STONE_EDIT

            if (nodeData.containsKey(SGF_BLACK_MOVE)) {
                val sgfCoords = nodeData[SGF_BLACK_MOVE][0]

                if (sgfCoords.isEmpty()) {
                    gameNodeType = GameNodeType.PASS
                } else {
                    val realCoords = convertCoordinates(sgfCoords)
                    gamePrimaryMove = Stone(realCoords[0], realCoords[1], StoneColor.BLACK)
                    gameNodeType = GameNodeType.MOVE_PLAYED
                }
            } else if (nodeData.containsKey(SGF_WHITE_MOVE)) {
                val sgfCoords = nodeData[SGF_WHITE_MOVE][0]

                if (sgfCoords.isEmpty()) {
                    gameNodeType = GameNodeType.PASS
                } else {
                    val realCoords = convertCoordinates(sgfCoords)
                    gamePrimaryMove = Stone(realCoords[0], realCoords[1], StoneColor.WHITE)
                    gameNodeType = GameNodeType.MOVE_PLAYED
                }
            } else if (nodeData.containsKey(SGF_ADD_BLACK) || nodeData.containsKey(SGF_ADD_WHITE)) {
                gameNodeType = GameNodeType.STONE_EDIT
            }

            if (gameNodeType == GameNodeType.MOVE_PLAYED) {
                val results = GameMoveSubmitter.createMoveNodeForProposedMove(gameModel, parentNode, gamePrimaryMove!!, true)
                val validationResult = results.first
                assert(validationResult == MoveValidationResult.OK)
                gameNode = results.second!!
            } else if (gameNodeType == GameNodeType.STONE_EDIT) {
                gameNode = GameMoveSubmitter.createMoveNodeForStoneEdit(parentNode)
            } else if (gameNodeType == GameNodeType.PASS) {
                gameNode = GameMoveSubmitter.createMoveNodeForPass(parentNode)
                // TODO: There's actually no differentiation between passing and resigning. The resignation node is
                //       handled as a pass, except that the root node game result is set to W+R or B+R.
            }

            if (gameNode == null) {
                // TODO: Probably unlikely given that we have a good coverage. But if we do get here then maybe just
                //      insert a generic node?
                gameNode = GameMoveSubmitter.createMoveNodeForStoneEdit(parentNode)
            }

            return gameNode
        }

        /**
         * Interprets the raw node data by sorting them into a data structure that allows programmatic retrieval of
         * individual key-value pairs.
         */
        private fun organizeNodeData(nodeData: String): SgfNodeData {
            val result = HashMap<String, ArrayList<String>>()
            var readingKeyRatherThanValue = true

            val tagKey = StringBuilder()
            val tagValue = StringBuilder()

            var skipNextChar = false

            for (index in nodeData.indices) {
                val char = nodeData[index]

                if (skipNextChar) {
                    skipNextChar = false
                    continue
                }

                if (readingKeyRatherThanValue) {
                    if (char == TAG_VALUE_START) {
                        readingKeyRatherThanValue = false
                    } else {
                        tagKey.append(char)
                    }
                } else {
                    if (char == TAG_VALUE_END) {
                        // Some annotations have multi-part values in the following format:
                        // AB[aa][bb][cc]
                        // So if at the end of one value term we immediately encounter a new
                        // value start symbol, don't clear the key value and parse another value.
                        // We store each value individually under the same key.
                        //
                        // Other annotations value have two parts,
                        // such as the arrow AR[aa:bb] where the values represent
                        // start-to-end coordinates for the two-point annotation. In this case
                        // we will store the values together as one entry.
                        val key = tagKey.toString()
                        val value = tagValue.toString()

                        result.putIfAbsent(key, ArrayList())
                        result[key]!!.add(value)
                        readingKeyRatherThanValue = true

                        if (index < nodeData.length - 1) {
                            if (nodeData[index+1] == TAG_VALUE_START) {
                                readingKeyRatherThanValue = false
                                skipNextChar = true
                            } else {
                                tagKey.clear()
                            }
                        }

                        tagValue.clear()
                    } else {
                        tagValue.append(char)
                    }
                }
            }

            return SgfNodeData(result)
        }

        /**
         * Reads a sequence of String from an input stream until one of the delimiters is reached.
         *
         * @return A result pair where the first element is the String that is read, including the
         * delimiter. The second element is the delimiter character the operation stopped at for quick
         * access.
         */
        fun readUntil(reader: InputStreamReader, vararg delimiters: Char): Pair<String, Char> {
            var charCode: Int
            var char: Char
            val buffer = StringBuilder()

            do {
                charCode = reader.read()
                if (charCode == -1) {
                    throw BadFormatException("Premature end of file")
                }
                char = charCode.toChar()
                buffer.append(char)
            } while (!delimiters.contains(char))

            return Pair(buffer.toString(), char)
        }

        private val SGF_COORDINATES = "abcdefghijklmnopqrstuvwxyz"
        fun convertCoordinates(sgfCoords: String): Array<Int> {
            val x = SGF_COORDINATES.indexOf(sgfCoords.toLowerCase()[0])
            val y = SGF_COORDINATES.indexOf(sgfCoords.toLowerCase()[1])
            return arrayOf(x, y)
        }

        /**
         * A temporary object that maps SGF tag key/value pairs into an organised data structure.
         * Each key can have multiple values, hence the list of strings for value.
         */
        // This is essentially a wrapper for the layers of ugly generics so that we don't have to
        // pass the raw generic types everywhere. Plus if we decide to change the data structure,
        // the usage won't be affected.
        private class SgfNodeData(private val data: Map<String, List<String>>) {

            // Keep track of the data tags that we've used to store meaningful information so that
            // we can store the unused tags as client-side metadata. This way we don't lose information
            // that the application doesn't support when the user decides to save the document again.
            private val usedData = HashSet<String>()

            operator fun get(key: String): List<String> {
                usedData.add(key)
                return data.getValue(key)
            }

            fun getOrDefault(key: String, defaultValue: List<String>): List<String> {
                usedData.add(key)
                return data.getOrDefault(key, defaultValue)
            }

            fun containsKey(key: String): Boolean {
                return data.containsKey(key)
            }

            fun getUnusedData(): Map<String, List<String>> {
                val result = HashMap<String, List<String>>()
                for (key in data.keys) {
                    if (!usedData.contains(key)) {
                        result[key] = data.getValue(key)
                    }
                }
                return result
            }

            override fun toString(): String {
                return data.toString()
            }
        }

        private class SgfBranch(var parentNode: GameNode?, var latestNode: GameNode?)
    }

    private object SgfExporter {
        // TODO: Implement me!
    }
}