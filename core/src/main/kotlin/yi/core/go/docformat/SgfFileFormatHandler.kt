package yi.core.go.docformat

import yi.core.go.*
import yi.core.go.Annotation
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Parses game data of Smart Go Format (SGF) into a [GameModel]
 *
 * See SGF format specification at: https://www.red-bean.com/sgf/user_guide/index.html
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
        val writer = destination.writer(Charsets.UTF_8)
        writer.use {
            return SgfExporter.doExport(gameModel, writer)
        }
    }

    companion object {
        private const val SGF_COORDINATES = "abcdefghijklmnopqrstuvwxyz"

        private const val DELIM_BRANCH_START = '('
        private const val DELIM_BRANCH_END = ')'
        private const val DELIM_NODE_START = ';'
        private const val DELIM_TAG_VALUE_START = '['
        private const val DELIM_TAG_VALUE_END = ']'
        private const val DELIM_TAG_VALUE_SPLIT = ':'

        private const val SGF_BOARD_SIZE = "SZ"
        private const val SGF_RULESET = "RU"
        private const val SGF_KOMI = "KM"
        private const val SGF_CHARSET = "CA"
        private const val SGF_GAME_TYPE = "GM"
        private const val SGF_FILE_FORMAT = "FF"
        private const val SGF_BLACK_MOVE = "B"
        private const val SGF_WHITE_MOVE = "W"
        private const val SGF_ADD_BLACK = "AB"
        private const val SGF_ADD_WHITE = "AW"
        private const val SGF_ADD_ERASE = "AE"
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
        private const val SGF_COMMENT = "C"
    }

    private object SgfImporter {

        fun doImport(reader: InputStreamReader): GameModel {
            var charCode: Int
            var char: Char? = null
            var gameModel: GameModel? = null

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

                if (char == DELIM_BRANCH_START) {
                    val parent = if (branchStack.isEmpty()) null else branchStack.peek().latestNode
                    branchStack.push(SgfBranch(parent, parent))
                    readCharNextLoop = true
                } else if (char == DELIM_BRANCH_END) {
                    branchStack.pop()
                    readCharNextLoop = true
                } else if (char == DELIM_NODE_START) {
                    val readResult = readUntil(reader, DELIM_BRANCH_START, DELIM_BRANCH_END, DELIM_NODE_START)

                    val nodeData = readResult.first
                    char = readResult.second

                    val organizedNodeData = asKeyValuePairs(nodeData)
                    removeMalformedData(organizedNodeData);

                    // We're already at the next token, so we want the next loop to evaluate this.
                    readCharNextLoop = false

                    if (gameModel == null) {
                        if (branchStack.size != 1) {
                            throw GameParseException("No main branch defined before providing node definition")
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
                throw GameParseException("Premature end of file")
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
                gameModel._setRootNode(parseNode(rootNodeData, null, gameModel))

                // TODO: Enumerate other node data here

                return gameModel
            } catch (e: NumberFormatException) {
                throw GameParseException("Invalid board size values: $boardSize")
            }
        }

        private fun parseNode(nodeData: SgfNodeData, parentNode: GameNode?, gameModel: GameModel): GameNode {
            val gameNode = parseNodeType(nodeData, parentNode, gameModel)
            parseHelperStones(nodeData, gameNode, gameModel)
            parseAnnotations(nodeData, gameNode, gameModel)

            gameNode.delta.comments = nodeData.getOrDefault(SGF_COMMENT, listOf(""))[0]
            gameNode.putMetadata(nodeData.getAsHashMap())

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
            Yi makes some arbitrary distinctions between different node types,
            where each node type represents the main information stored on that node.

            This information is interpreted from the presence (or absence) of notable
            tag keys and values.

            This step must be performed first before parsing other type of node
            information as this will create the node itself.
         */
        private fun parseNodeType(nodeData: SgfNodeData, parentNode: GameNode?, gameModel: GameModel): GameNode {
            var gameNode: GameNode? = null
            var gamePrimaryMove: Stone? = null
            var gameNodeType: GameNodeType = GameNodeType.STONE_EDIT

            if (parentNode == null) {
                gameNodeType = GameNodeType.ROOT
            } else if (nodeData.containsKey(SGF_BLACK_MOVE)) {
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
                val results = GameMoveSubmitter.createMoveNodeForProposedMove(gameModel, parentNode!!, gamePrimaryMove!!, true)
                val validationResult = results.first
                assert(validationResult == MoveValidationResult.OK)
                gameNode = results.second!!
            } else if (gameNodeType == GameNodeType.STONE_EDIT) {
                gameNode = GameMoveSubmitter.createMoveNodeForStoneEdit(parentNode!!)
            } else if (gameNodeType == GameNodeType.PASS) {
                gameNode = GameMoveSubmitter.createMoveNodeForPass(parentNode!!)
                // TODO: There's actually no differentiation between passing and resigning. The resignation node is
                //       handled as a pass, except that the root node game result is set to W+R or B+R.
            } else if (gameNodeType == GameNodeType.ROOT) {
                gameNode = GameMoveSubmitter.createMoveNodeForRoot(gameModel)
            }

            if (gameNode == null) {
                // TODO: Probably unlikely given that we have a good coverage. But if we do get here then maybe just
                //      insert a generic node?
                gameNode = GameMoveSubmitter.createMoveNodeForStoneEdit(parentNode!!)
            }

            return gameNode
        }

        /**
         * Interprets the raw node data by sorting them into a data structure
         * that allows programmatic retrieval of individual key-value pairs.
         */
        private fun asKeyValuePairs(nodeData: String): SgfNodeData {
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
                    if (char == DELIM_TAG_VALUE_START) {
                        readingKeyRatherThanValue = false
                    } else {
                        tagKey.append(char)
                    }
                } else {
                    if (char == DELIM_TAG_VALUE_END) {
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
                        val key = tagKey.toString().trim()
                        var value = tagValue.toString()

                        result.putIfAbsent(key, ArrayList())
                        result[key]!!.add(value)
                        readingKeyRatherThanValue = true

                        if (index < nodeData.length - 1) {
                            if (nodeData[index+1] == DELIM_TAG_VALUE_START) {
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

        /*
            Removes tags with data that does not conform to the expected format so that
            valid data tags are still loaded.
         */
        private fun removeMalformedData(organizedNodeData: SgfNodeData) {
            for (formatConstraint in SgfTagFormatConstraints.values()) {
                val tagKey = formatConstraint.tagKey
                if (organizedNodeData.containsKey(tagKey)) {
                    val valuesToRemove = ArrayList<String>()
                    for (value in organizedNodeData[tagKey]) {
                        if (!formatConstraint.isValueValid(value)) {
                            valuesToRemove.add(value)
                        }
                    }
                    for (value in valuesToRemove) {
                        organizedNodeData.removeValueForKey(tagKey, value)
                    }
                }
            }
        }

        /**
         * Reads a sequence of characters from an input stream until one of the
         * delimiters is reached.
         *
         * @return A result pair where the first element is the String that is read,
         * including the terminal delimiter. The second element is the delimiter character
         * the read operation stopped at, for quick access.
         */
        fun readUntil(reader: InputStreamReader, vararg delimiters: Char): Pair<String, Char> {
            var charCode: Int
            var char: Char
            val buffer = StringBuilder()

            do {
                charCode = reader.read()
                if (charCode == -1) {
                    throw GameParseException("Premature end of file")
                }
                char = charCode.toChar()
                buffer.append(char)
            } while (!delimiters.contains(char))

            return Pair(buffer.toString(), char)
        }

        fun convertCoordinates(sgfCoords: String): Array<Int> {
            val x = SGF_COORDINATES.indexOf(sgfCoords.toLowerCase()[0])
            val y = SGF_COORDINATES.indexOf(sgfCoords.toLowerCase()[1])
            // TODO: If the co-ordinate data is malformed, x,y may be negative values. Handle this.
            return arrayOf(x, y)
        }

        /**
         * A temporary object that maps SGF tag key/value pairs into an organised data structure.
         * Each key can have multiple values, hence the list of strings for value.
         */
        // This is essentially a wrapper for the layers of ugly generics so that we don't have to
        // pass the raw generic types everywhere. Plus if we decide to change the data structure,
        // the usage won't be affected.
        private class SgfNodeData(private val data: HashMap<String, ArrayList<String>>) {

            operator fun get(key: String): List<String> {
                return data.getValue(key)
            }

            fun getOrDefault(key: String, defaultValue: List<String>): List<String> {
                return if (defaultValue is ArrayList<String>) {
                    data.getOrDefault(key, defaultValue)
                } else {
                    data.getOrDefault(key, ArrayList(defaultValue))
                }
            }

            fun containsKey(key: String): Boolean {
                return data.containsKey(key)
            }

            /**
             * Removes the key and all its values from the node data.
             */
            fun removeKey(tagKey: String) {
                data.remove(tagKey)
            }

            /**
             * Removes the value for a key. If multiple values are equal to
             * the value to be removed, then all of them are removed. If after
             * this operation the key has no more values, then the key is
             * removed also.
             */
            fun removeValueForKey(tagKey: String, valueToRemove: String) {
                if (containsKey(tagKey)) {
                    while (data[tagKey]!!.contains(valueToRemove)) {
                        println("Removed $valueToRemove")
                        data[tagKey]!!.remove(valueToRemove)
                    }

                    if (data[tagKey]!!.isEmpty()) {
                        removeKey(tagKey)
                    }
                }
            }

            override fun toString(): String {
                return data.toString()
            }

            fun getAsHashMap(): Map<String, List<String>> {
                return data
            }
        }

        private class SgfBranch(var parentNode: GameNode?, var latestNode: GameNode?)


        /**
         * Defines the value format of some SGF tags so that its validity can be verified.
         * Invalid SGF tags are skipped to avoid creating a bad game tree from malformed
         * game file data.
         */
        private enum class SgfTagFormatConstraints(val tagKey: String, vararg val acceptableValueFormats: SgfTagValueType) {

            B("B", SgfTagValueType.Empty, SgfTagValueType.OnePointLocation),
            W("W", SgfTagValueType.Empty, SgfTagValueType.OnePointLocation),

            MA("MA", SgfTagValueType.OnePointLocation),
            SQ("SQ", SgfTagValueType.OnePointLocation),
            TR("TR", SgfTagValueType.OnePointLocation),
            CR("CR", SgfTagValueType.OnePointLocation),
            DD("DD", SgfTagValueType.OnePointLocation),
            LB("LB", SgfTagValueType.LabelAnnotation),

            AR("AR", SgfTagValueType.TwoPointLocation),
            LN("LN", SgfTagValueType.TwoPointLocation),

            KM("KM", SgfTagValueType.Numerical)

            ;

            fun isValueValid(valueToTest: String): Boolean {
                for (format in acceptableValueFormats) {
                    if (format.isValueValid(valueToTest)) {
                        return true
                    }
                }
                return false
            }
        }

        private enum class SgfTagValueType {
            Empty {
                override fun isValueValid(valueToTest: String): Boolean {
                    return valueToTest.isEmpty()
                }
            },


            OnePointLocation {
                override fun isValueValid(valueToTest: String): Boolean {
                    return valueToTest.length == 2 // format "aa"
                            && isValidCoordinateValue(valueToTest[0])
                            && isValidCoordinateValue(valueToTest[1])
                }
            },

            TwoPointLocation {
                override fun isValueValid(valueToTest: String): Boolean {
                    return valueToTest.length == 5 // format "aa:bb"
                            && isValidCoordinateValue(valueToTest[0])
                            && isValidCoordinateValue(valueToTest[1])
                            && valueToTest[2] == ':'
                            && isValidCoordinateValue(valueToTest[3])
                            && isValidCoordinateValue(valueToTest[4])

                }
            },

            LabelAnnotation {
                override fun isValueValid(valueToTest: String): Boolean {
                    val dataSegments = valueToTest.split(":")
                    return dataSegments.size == 2 // format "ab:TextData"
                            && dataSegments[0].length == 2
                            && isValidCoordinateValue(valueToTest[0])
                            && isValidCoordinateValue(valueToTest[1])
                            && valueToTest[2] == ':'
                            && dataSegments[1].isNotEmpty()
                }
            },

            Numerical {
                override fun isValueValid(valueToTest: String): Boolean {
                    return try {
                        java.lang.Double.parseDouble(valueToTest)
                        true
                    } catch (e: NumberFormatException) {
                        false
                    }
                }
            }

            /*
                Fields like "Comment" are purely textual, and have no format constraints,
                so we don't list them here.
             */
            ;

            abstract fun isValueValid(valueToTest: String): Boolean

            fun isValidCoordinateValue(value: Char): Boolean {
                return SGF_COORDINATES.indexOf(value) >= 0
            }
        }
    }

    private object SgfExporter {

        fun doExport(gameModel: GameModel, writer: OutputStreamWriter) {
            exportBranch(gameModel, gameModel.getRootNode(), writer)
        }

        private fun exportBranch(gameModel: GameModel, branchStartNode: GameNode, writer: OutputStreamWriter) {
            var currentNode = branchStartNode
            var done = false
            writer.write(DELIM_BRANCH_START.toString())
            while (!done) {
                exportNode(gameModel, currentNode, writer)

                if (currentNode.hasAlternativeNextMoves()) {
                    for (branchingChild in currentNode.getNextNodes()) {
                        exportBranch(gameModel, branchingChild, writer)
                    }
                    done = true;
                } else {
                    if (!currentNode.isLastMoveInThisVariation()) {
                        currentNode = currentNode.getNextNodeInMainBranch()!!
                    } else {
                        done = true
                    }
                }
            }
            writer.write(DELIM_BRANCH_END.toString())
        }

        private fun exportNode(gameModel: GameModel, currentNode: GameNode, writer: OutputStreamWriter) {
            writer.write(DELIM_NODE_START.toString())

            if (currentNode.isRoot()) {
                exportRootNodeData(gameModel, currentNode, writer)
            }

            exportPlayedMoveData(currentNode, writer)
            exportStoneEditData(currentNode, writer)
            exportAnnotationData(currentNode, writer)
            exportCommentData(currentNode, writer)

            // TODO: Export other metadata?
        }

        private fun exportRootNodeData(gameModel: GameModel, rootNode: GameNode, writer: OutputStreamWriter) {
            writeTag(SGF_GAME_TYPE, "1", writer)
            writeTag(SGF_FILE_FORMAT, "4", writer)

            val boardSizeValue: String = if (gameModel.boardWidth == gameModel.boardHeight) {
                gameModel.boardWidth.toString()
            } else {
                gameModel.boardWidth.toString() + DELIM_TAG_VALUE_SPLIT + gameModel.boardHeight.toString()
            }
            writeTag(SGF_BOARD_SIZE, boardSizeValue, writer)

            writeTag(SGF_KOMI, gameModel.rules.getKomi().toString(), writer)
            writeTag(SGF_RULESET, gameModel.rules.getInternalName(), writer)
        }

        private fun exportAnnotationData(currentNode: GameNode, writer: OutputStreamWriter) {
            val annotationData = HashMap<String, ArrayList<String>>()

            for (annotation in currentNode.getAnnotationsOriginal()) {
                @Suppress("ThrowableNotThrown") // Anticipating future additions
                val key = when (annotation.type) {
                    AnnotationType.CIRCLE -> SGF_MARKUP_CIRCLE
                    AnnotationType.SQUARE -> SGF_MARKUP_SQUARE
                    AnnotationType.TRIANGLE -> SGF_MARKUP_TRIANGLE
                    AnnotationType.CROSS -> SGF_MARKUP_CROSS
                    AnnotationType.LABEL -> SGF_MARKUP_LABEL
                    AnnotationType.FADE -> SGF_MARKUP_DIM
                    AnnotationType.ARROW -> SGF_MARKUP_ARROW
                    AnnotationType.LINE -> SGF_MARKUP_LINE
                    else -> throw NotImplementedError("Unimplemented annotation type for export: ${annotation.type}")
                }

                annotationData.putIfAbsent(key, ArrayList())

                when (annotation) {
                    is Annotation.PointAnnotation -> {
                        val x = annotation.x
                        val y = annotation.y
                        val coordinate = getSgfCoordinates(x, y)
                        annotationData[key]!!.add(coordinate)
                    }
                    is Annotation.DirectionalAnnotation -> {
                        val xStart = annotation.x
                        val yStart = annotation.y
                        val xEnd = annotation.xEnd
                        val yEnd = annotation.yEnd
                        val coordinate = getSgfCoordinates(xStart, yStart) + DELIM_TAG_VALUE_SPLIT + getSgfCoordinates(xEnd, yEnd)
                        annotationData[key]!!.add(coordinate)
                    }
                    else -> {
                        throw NotImplementedError("Unrecognised annotation type for export: " + annotation.javaClass)
                    }
                }
            }

            writeTags(annotationData, writer)
        }

        private fun exportStoneEditData(currentNode: GameNode, writer: OutputStreamWriter) {
            val stoneEditData = HashMap<String, ArrayList<String>>()

            for (stoneEdit in currentNode.getStoneEdits()) {
                if (stoneEdit == currentNode.getPrimaryMove()) {
                    continue
                }

                val color = stoneEdit.color
                val x = stoneEdit.x
                val y = stoneEdit.y

                val key = when (color) {
                    StoneColor.BLACK -> SGF_ADD_BLACK
                    StoneColor.WHITE -> SGF_ADD_WHITE
                    StoneColor.NONE -> SGF_ADD_ERASE // Erase a stone from the position
                }

                val coordinate = getSgfCoordinates(x, y)

                stoneEditData.putIfAbsent(key, ArrayList())
                stoneEditData[key]!!.add(coordinate)
            }

            writeTags(stoneEditData, writer)
        }

        private fun exportPlayedMoveData(currentNode: GameNode, writer: OutputStreamWriter) {
            val moveType = currentNode.getType()
            var primaryMove: Stone? = null

            if (moveType == GameNodeType.MOVE_PLAYED
                    || moveType == GameNodeType.PASS
                    || moveType == GameNodeType.RESIGN) {
                primaryMove = currentNode.getPrimaryMove()!!
            }

            primaryMove?.let {
                val coordinates = if (moveType == GameNodeType.MOVE_PLAYED) getSgfCoordinates(it) else ""
                val color = it.color
                val key: String

                key = when (color) {
                    StoneColor.BLACK -> SGF_BLACK_MOVE
                    StoneColor.WHITE -> SGF_WHITE_MOVE
                    else -> throw NotImplementedError("Unsupported stone color: $color")
                }

                writeTag(key, coordinates, writer)
            }
        }

        private fun exportCommentData(currentNode: GameNode, writer: OutputStreamWriter) {
            if (currentNode.getComments().isNotBlank()) {
                writeTag(SGF_COMMENT, currentNode.getComments(), writer)
            }
        }

        private fun writeTags(data: Map<String, List<String>>, writer: OutputStreamWriter) {
            for (key in data.keys) {
                writeTag(key, data[key]!!, writer)
            }
        }

        private fun writeTag(tagKey: String, atomicValue: String, writer: OutputStreamWriter) {
            writeTag(tagKey, listOf(atomicValue), writer)
        }

        private fun writeTag(tagKey: String, tagValues: List<String>, writer: OutputStreamWriter) {
            assert(tagValues.isNotEmpty()) { "Cannot write empty values" }
            writer.write(tagKey)
            for (value in tagValues) {
                writeDelimiter(DELIM_TAG_VALUE_START, writer)
                val escapedValue = escapeDelimiters(value)
                writer.write(escapedValue)
                writeDelimiter(DELIM_TAG_VALUE_END, writer)
            }
        }

        private fun writeDelimiter(delimiter: Char, writer: OutputStreamWriter) {
            writer.write(delimiter.toString())
        }

        private val reservedValueCharacters = arrayOf(DELIM_TAG_VALUE_END, DELIM_TAG_VALUE_START)

        private fun escapeDelimiters(value: String): String {
            var result = value
            for (reservedChar in reservedValueCharacters) {
                result = result.replace(reservedChar.toString(), "\\$reservedChar", true)
            }
            return result
        }

        private fun getSgfCoordinates(move: Stone): String {
            return getSgfCoordinates(move.x, move.y)
        }

        private fun getSgfCoordinates(x: Int, y: Int): String {
            if (x < 0 || y < 0 || x > SGF_COORDINATES.length - 1 || y > SGF_COORDINATES.length - 1) {
                throw IllegalArgumentException("Illegal SGF position: ($x, $y). Value should be between " +
                        "0-${SGF_COORDINATES.length-1}")
            }
            return SGF_COORDINATES[x].toString() + SGF_COORDINATES[y].toString()
        }
    }
}