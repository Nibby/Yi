package codes.nibby.yi.models.docformat

import codes.nibby.yi.models.*
import codes.nibby.yi.models.Annotation
import java.io.BufferedReader
import java.io.BufferedWriter
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

    override fun isLikelyLoadable(reader: BufferedReader): Boolean {
        val charArray = CharArray(64)
        val read = reader.read(charArray, 0, charArray.size)
        return if (read > 0) {
            charArray.toString().contains("GM[1]")
        } else {
            false
        }
    }

    override fun doImport(reader: BufferedReader): GameModel {
        // TODO: The SGF file may specify the charset to use in the root node, maybe pre-scan the document and set it first?
        return SgfImporter.doImport(reader)
    }

    override fun doExport(gameModel: GameModel, writer: BufferedWriter) {
        return SgfExporter.doExport(gameModel, writer)
    }

    companion object {
        internal const val SGF_COORDINATES = "abcdefghijklmnopqrstuvwxyz"

        internal const val DELIM_BRANCH_START = '('
        internal const val DELIM_BRANCH_END = ')'
        internal const val DELIM_NODE_START = ';'
        internal const val DELIM_TAG_VALUE_START = '['
        internal const val DELIM_TAG_VALUE_END = ']'
        internal const val DELIM_TAG_VALUE_SPLIT = ':'

        internal const val SGF_APPLICATION = "AP" // Program that saved this document
        internal const val SGF_GAME_PLACE = "PC" // Place where the game was played
        internal const val SGF_BLACK_NAME = "PB"
        internal const val SGF_BLACK_RANK = "BR"
        internal const val SGF_WHITE_NAME = "PW"
        internal const val SGF_WHITE_RANK = "WR"
        internal const val SGF_HANDICAP_COUNT = "HA"
        internal const val SGF_BOARD_SIZE = "SZ"
        internal const val SGF_RULESET = "RU"
        internal const val SGF_KOMI = "KM"
//        internal const val SGF_CHARSET = "CA"
        internal const val SGF_GAME_TYPE = "GM"
        internal const val SGF_FILE_FORMAT = "FF"
        internal const val SGF_BLACK_MOVE = "B"
        internal const val SGF_WHITE_MOVE = "W"
        internal const val SGF_ADD_BLACK = "AB"
        internal const val SGF_ADD_WHITE = "AW"
        internal const val SGF_ADD_ERASE = "AE"
        internal const val SGF_MARKUP_CROSS = "MA"
        internal const val SGF_MARKUP_TRIANGLE = "TR"
        internal const val SGF_MARKUP_CIRCLE = "CR"
        internal const val SGF_MARKUP_SQUARE = "SQ"
        internal const val SGF_MARKUP_LABEL = "LB"
//        internal const val SGF_MARKUP_LETTER = "L" // Old, TODO: support it?
        internal const val SGF_MARKUP_DIM = "DD"
        internal const val SGF_MARKUP_ARROW = "AR"
        internal const val SGF_MARKUP_LINE = "LN"
//        internal const val SGF_MARKUP_SL = "SL" // Old, TODO: What's this?
        internal const val SGF_COMMENT = "C"
    }

    private object SgfImporter {

        fun doImport(reader: BufferedReader): GameModel {
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
                    if (branchStack.isEmpty()) {
                        // The entire SGF record has been loaded
                        // TODO: Some servers, such as FlyOrDie, may store multiple games in
                        //       one SGF file. They are in the form of multiple self-contained
                        //       SGF data. Right now we only load the first game, but we
                        //       should load all of them eventually.
                        break;
                    } else {
                        // Was only the end of a sub-branch, SGF record still continues
                        readCharNextLoop = true
                    }
                } else if (char == DELIM_NODE_START) {
                    val readResult = readUntil(reader, DELIM_BRANCH_START, DELIM_BRANCH_END, DELIM_NODE_START)

                    val nodeData = readResult.first
                    char = readResult.second

                    val organizedNodeData = asKeyValuePairs(nodeData)
                    removeMalformedData(organizedNodeData)

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
                        gameModel.editor.appendNode(nodeParent, node)
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

                val gameRules = StandardGameRules.parse(ruleset[0]).orElse(
                    StandardGameRules.CHINESE)
                val gameModel = GameModel(width, height, gameRules)
                val rootNode = parseNode(rootNodeData, null, gameModel)
                gameModel._setRootNode(rootNode)

                val pcValue = rootNodeData.getOrDefault(SGF_GAME_PLACE, listOf(""))
                if (pcValue[0].contains("OGS: https://online-go.com/game/")) {
                    processOgsModel(gameModel, rootNode)
                } else {
                    val haValue = rootNodeData.getOrDefault(SGF_HANDICAP_COUNT, listOf("0"))
                    gameModel.info.setHandicapCount(haValue[0].toInt())
                }

                gameModel.info.setPlayerBlackName(rootNodeData.getOrDefault(
                    SGF_BLACK_NAME, listOf(""))[0])
                gameModel.info.setPlayerBlackRank(rootNodeData.getOrDefault(
                    SGF_BLACK_RANK, listOf(""))[0])
                gameModel.info.setPlayerWhiteName(rootNodeData.getOrDefault(
                    SGF_WHITE_NAME, listOf(""))[0])
                gameModel.info.setPlayerWhiteRank(rootNodeData.getOrDefault(
                    SGF_WHITE_RANK, listOf(""))[0])

                // TODO: Enumerate other node data here

                return gameModel
            } catch (e: NumberFormatException) {
                throw GameParseException("Invalid board size values: $boardSize")
            }
        }

        private fun processOgsModel(gameModel: GameModel, rootNode: GameNode) {
            // OGS is missing a HA[] value representing handicap stone count. So we have
            // to manually count them through the number of AB[] tags on root.
            gameModel.info.setHandicapCount(rootNode.getMetadataMultiValue(
                SGF_ADD_BLACK
            ).size)
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
                twoPointAnnotationConverter.invoke(data) { from, to -> annotations.add(
                    Annotation.Line(from[0], from[1], to[0], to[1])) }
            }
            nodeData.getOrDefault(SGF_MARKUP_ARROW, listOf()).forEach { data ->
                twoPointAnnotationConverter.invoke(data) { from, to -> annotations.add(
                    Annotation.Arrow(from[0], from[1], to[0], to[1])) }
            }

            gameModel.editor.addAnnotations(gameNode, annotations)
        }

        /*
            Parses all the helper stone data (AB and AW) for the node
         */
        private fun parseHelperStones(nodeData: SgfNodeData, gameNode: GameNode, gameModel: GameModel) {
            val demoBlackStones = nodeData.getOrDefault(SGF_ADD_BLACK, listOf()).map { convertCoordinates(it) }
            val demoWhiteStones = nodeData.getOrDefault(SGF_ADD_WHITE, listOf()).map { convertCoordinates(it) }
            val eraseStones = nodeData.getOrDefault(SGF_ADD_ERASE, listOf()).map { convertCoordinates(it) }

            demoBlackStones.forEach {
                gameModel.editor.addStoneEdit(gameNode, Stone(it[0], it[1], StoneColor.BLACK))
            }

            demoWhiteStones.forEach {
                gameModel.editor.addStoneEdit(gameNode, Stone(it[0], it[1], StoneColor.WHITE))
            }

            eraseStones.forEach {
                val x = it[0]
                val y = it[1]

                // Remove existing helper stones first
                gameModel.editor.removeStoneEdit(gameNode, Stone(x, y, StoneColor.BLACK))
                gameModel.editor.removeStoneEdit(gameNode, Stone(x, y, StoneColor.WHITE))

                gameModel.editor.addStoneEdit(gameNode, Stone(x, y, StoneColor.NONE))
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
                    gameNodeType = if (isMoveWithinBounds(realCoords, gameModel)) GameNodeType.MOVE_PLAYED
                                   else GameNodeType.PASS
                }
            } else if (nodeData.containsKey(SGF_WHITE_MOVE)) {
                val sgfCoords = nodeData[SGF_WHITE_MOVE][0]

                if (sgfCoords.isEmpty()) {
                    gameNodeType = GameNodeType.PASS
                } else {
                    val realCoords = convertCoordinates(sgfCoords)
                    gamePrimaryMove = Stone(realCoords[0], realCoords[1], StoneColor.WHITE)
                    gameNodeType = if (isMoveWithinBounds(realCoords, gameModel)) GameNodeType.MOVE_PLAYED
                                   else GameNodeType.PASS
                }
            } else if (nodeData.containsKey(SGF_ADD_BLACK) || nodeData.containsKey(
                    SGF_ADD_WHITE
                )) {
                gameNodeType = GameNodeType.STONE_EDIT
            }

            // TODO: Probably unlikely given that we have a good coverage. But if we do get here then maybe just
            //      insert a generic node?
            when (gameNodeType) {
                GameNodeType.MOVE_PLAYED -> {
                    val results = GameMoveSubmitter.createMoveNode(gameModel, parentNode!!, gamePrimaryMove!!, true)
                    val validationResult = results.first
                    assert(validationResult == MoveValidationResult.OK)
                    gameNode = results.second!!
                }
                GameNodeType.STONE_EDIT -> {
                    gameNode = GameMoveSubmitter.createStoneEditNode(parentNode!!)
                }
                GameNodeType.PASS -> {
                    gameNode = GameMoveSubmitter.createPassNode(parentNode!!)
                    // TODO: There's actually no differentiation between passing and resigning. The resignation node is
                    //       handled as a pass, except that the root node game result is set to W+R or B+R.
                }
                GameNodeType.ROOT -> {
                    gameNode = GameMoveSubmitter.createRootNode(gameModel)
                }
            }

            return gameNode
        }

        private fun isMoveWithinBounds(coords: Array<Int>, gameModel: GameModel): Boolean {
            val x = coords[0]
            val y = coords[1]
            return !(x < 0 || x >= gameModel.boardWidth || y < 0 || y >= gameModel.boardHeight)
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
            var index = 0
            var incrementIndex = true

            while (index < nodeData.indices.last) {
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
                        if (tagValue.lastIndex >= 0 && tagValue[tagValue.lastIndex] == '\\') {
                            // Tags such as comments may contain free-form text data
                            // including the value-end delimiter ']'. When this is part
                            // of the value itself, it is escaped with a leading forward
                            // slash.
                            tagValue.replace(tagValue.lastIndex, tagValue.lastIndex+1, char.toString())
                            index++
                            continue
                        }

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
                        val value = tagValue.toString()

                        result.putIfAbsent(key, ArrayList())
                        result[key]!!.add(value)
                        readingKeyRatherThanValue = true

                        if (index < nodeData.length - 1) {
                            val nextIndexToBeginReadingValueAt = findNextValueStart(nodeData, index)

                            if (nextIndexToBeginReadingValueAt >= 0) {
                                readingKeyRatherThanValue = false
                                skipNextChar = true
                                index = nextIndexToBeginReadingValueAt + 1
                                // Don't increment again because we've already re-positioned
                                // the index here.
                                incrementIndex = false
                            } else {
                                tagKey.clear()
                            }
                        }

                        tagValue.clear()
                    } else {
                        tagValue.append(char)
                    }
                }

                if (incrementIndex) {
                    index++
                } else {
                    incrementIndex = true
                }
            }

            return SgfNodeData(result)
        }

        private fun findNextValueStart(nodeData: String, index: Int): Int {
            var charIndex = index
            while (charIndex + 1 < nodeData.lastIndex) {
                charIndex++
                val charHere = nodeData[charIndex]

                return if (charHere == '\n' || charHere == '\r' || charHere == '\t') {
                    continue
                } else if (charHere == DELIM_TAG_VALUE_START) {
                    charIndex
                } else {
                    -1
                }
            }
            return -1
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
        fun readUntil(reader: BufferedReader, vararg delimiters: Char): Pair<String, Char> {
            var charCode: Int
            var char: Char
            val buffer = StringBuilder()
            var readingTagValue = false
            var readNextChar = true

            do {
                charCode = reader.read()
                if (charCode == -1) {
                    throw GameParseException("Premature end of file")
                }
                char = charCode.toChar()

                // Avoid parsing tag value as special delimiters
                if (char == DELIM_TAG_VALUE_START) {
                    readingTagValue = true
                } else if (char == DELIM_TAG_VALUE_END) {
                    readingTagValue = false
                }

                buffer.append(char)

                if (!readingTagValue && delimiters.contains(char)) {
                    readNextChar = false
                }
            } while (readNextChar)

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
//                        println("Removed $valueToRemove")
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


        /*
         * Defines the value format of some SGF tags so that its validity can be verified.
         * Invalid SGF tags are skipped to avoid creating a bad game tree from malformed
         * game file data.
         */
        private enum class SgfTagFormatConstraints(val tagKey: String, vararg val acceptableValueFormats: SgfTagValueConstraint) {

            B("B",
                SgfTagValueConstraint.Empty,
                SgfTagValueConstraint.OnePointLocation
            ),
            W("W",
                SgfTagValueConstraint.Empty,
                SgfTagValueConstraint.OnePointLocation
            ),

            MA("MA", SgfTagValueConstraint.OnePointLocation),
            SQ("SQ", SgfTagValueConstraint.OnePointLocation),
            TR("TR", SgfTagValueConstraint.OnePointLocation),
            CR("CR", SgfTagValueConstraint.OnePointLocation),
            DD("DD", SgfTagValueConstraint.OnePointLocation),
            LB("LB", SgfTagValueConstraint.LabelAnnotation),

            AR("AR", SgfTagValueConstraint.TwoPointLocation),
            LN("LN", SgfTagValueConstraint.TwoPointLocation),

            KM("KM", SgfTagValueConstraint.Numerical);

            fun isValueValid(valueToTest: String): Boolean {
                for (format in acceptableValueFormats) {
                    if (format.isValueValid(valueToTest)) {
                        return true
                    }
                }
                return false
            }
        }

        private enum class SgfTagValueConstraint {
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
            };

            /*
                Fields like "Comment" are purely textual, and have no format constraints,
                so we don't list them here.
             */

            abstract fun isValueValid(valueToTest: String): Boolean

            fun isValidCoordinateValue(value: Char): Boolean {
                return SGF_COORDINATES.indexOf(value) >= 0
            }
        }
    }

    internal object SgfExporter {

        internal const val SGF_EXPORTED_FILE_FORMAT_VERSION = 4

        fun doExport(gameModel: GameModel, writer: BufferedWriter) {
            exportBranch(gameModel, gameModel.getRootNode(), writer)
        }

        private fun exportBranch(gameModel: GameModel, branchStartNode: GameNode, writer: BufferedWriter) {
            var currentNode = branchStartNode
            var done = false
            writer.write(DELIM_BRANCH_START.toString())
            while (!done) {
                exportNode(gameModel, currentNode, writer)

                if (currentNode.hasAlternativeVariations()) {
                    for (branchingChild in currentNode.getChildNodes()) {
                        exportBranch(gameModel, branchingChild, writer)
                    }
                    done = true
                } else {
                    if (!currentNode.isLastMoveInThisVariation()) {
                        currentNode = currentNode.getChildNodeInMainBranch()!!
                    } else {
                        done = true
                    }
                }
            }
            writer.write(DELIM_BRANCH_END.toString())
        }

        private fun exportNode(gameModel: GameModel, currentNode: GameNode, writer: BufferedWriter) {
            writer.write(DELIM_NODE_START.toString())

            val nodeData = ExportableNodeData()

            if (currentNode.isRoot()) {
                mapRootNodeData(gameModel, nodeData)
            }

            mapPlayedMoveData(gameModel, currentNode, nodeData)
            mapStoneEditData(currentNode, nodeData)
            mapAnnotationData(currentNode, nodeData)
            mapCommentData(currentNode, nodeData)
            mapMetadata(currentNode, nodeData)
            
            nodeData.export(writer)
        }

        private fun mapMetadata(currentNode: GameNode, nodeData: ExportableNodeData) {
            for (key in currentNode.getMetadataKeys()) {
                val values = currentNode.getMetadataMultiValue(key)

                for (value in values) {
                    if (!nodeData.contains(key, value)) {
                        nodeData.append(key, value)
                    }
                }
            }
        }

        private fun mapRootNodeData(gameModel: GameModel, nodeData: ExportableNodeData) {
            nodeData.put(SGF_GAME_TYPE, "1")
            nodeData.put(SGF_FILE_FORMAT, SGF_EXPORTED_FILE_FORMAT_VERSION.toString())

            val appName = gameModel.info.getApplicationName()
            if (appName.isNotBlank()) {
                nodeData.put(SGF_APPLICATION, appName)
            }

            val boardSizeValue: String = if (gameModel.boardWidth == gameModel.boardHeight) {
                gameModel.boardWidth.toString()
            } else {
                gameModel.boardWidth.toString() + DELIM_TAG_VALUE_SPLIT + gameModel.boardHeight.toString()
            }
            nodeData.put(SGF_BOARD_SIZE, boardSizeValue)
            nodeData.put(SGF_KOMI, gameModel.info.getKomi().toString())
            nodeData.put(SGF_RULESET, gameModel.rules.getInternalName())

            if (gameModel.info.getHandicapCount() > 0) {
                nodeData.put(SGF_HANDICAP_COUNT, gameModel.info.getHandicapCount().toString())
            }
        }

        private fun mapAnnotationData(currentNode: GameNode, nodeData: ExportableNodeData) {
            for (annotation in currentNode.getAnnotations()) {
                @Suppress("ThrowableNotThrown") // Anticipating future additions
                val key = when (annotation.type) {
                    AnnotationType.CIRCLE -> SGF_MARKUP_CIRCLE
                    AnnotationType.SQUARE -> SGF_MARKUP_SQUARE
                    AnnotationType.TRIANGLE -> SGF_MARKUP_TRIANGLE
                    AnnotationType.CROSS -> SGF_MARKUP_CROSS
                    AnnotationType.LABEL -> SGF_MARKUP_LABEL
                    AnnotationType.DIM -> SGF_MARKUP_DIM
                    AnnotationType.ARROW -> SGF_MARKUP_ARROW
                    AnnotationType.LINE -> SGF_MARKUP_LINE
                    else -> throw NotImplementedError("Unimplemented annotation type for export: ${annotation.type}")
                }

                when (annotation) {
                    is Annotation.PointAnnotation -> {
                        val x = annotation.x
                        val y = annotation.y
                        val coordinate = getSgfCoordinates(x, y)
                        var data = coordinate

                        if (annotation is Annotation.Label) {
                            data += DELIM_TAG_VALUE_SPLIT + annotation.text
                        }

                        nodeData.append(key, data)
                    }
                    is Annotation.DirectionalAnnotation -> {
                        val xStart = annotation.x
                        val yStart = annotation.y
                        val xEnd = annotation.xEnd
                        val yEnd = annotation.yEnd
                        val coordinate = getSgfCoordinates(xStart, yStart) + DELIM_TAG_VALUE_SPLIT + getSgfCoordinates(xEnd, yEnd)

                        nodeData.append(key, coordinate)
                    }
                    else -> {
                        throw NotImplementedError("Unrecognised annotation type for export: " + annotation.javaClass)
                    }
                }
            }
        }

        private fun mapStoneEditData(currentNode: GameNode, nodeData: ExportableNodeData) {
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
                nodeData.append(key, coordinate)
            }
        }

        private fun mapPlayedMoveData(gameModel: GameModel, currentNode: GameNode, nodeData: ExportableNodeData) {
            val moveType = currentNode.getType()

            if (moveType == GameNodeType.MOVE_PLAYED) {
                currentNode.getPrimaryMove()!!.let {
                    val coordinates = if (moveType == GameNodeType.MOVE_PLAYED) getSgfCoordinates(it) else ""

                    val key: String = when (val color = it.color) {
                        StoneColor.BLACK -> SGF_BLACK_MOVE
                        StoneColor.WHITE -> SGF_WHITE_MOVE
                        else -> throw NotImplementedError("Unsupported stone color: $color")
                    }

                    nodeData.put(key, coordinates)
                }
            } else if (moveType == GameNodeType.PASS) {
                val ruleset = gameModel.rules
                val key = when (val expectedColor =
                        ruleset.getStoneColorForTurn(currentNode.moveNumber-1, gameModel.info.getHandicapCount() > 0)) {
                    StoneColor.BLACK -> SGF_BLACK_MOVE
                    StoneColor.WHITE -> SGF_WHITE_MOVE
                    else -> throw NotImplementedError("Unsupported stone color: $expectedColor")
                }

                nodeData.put(key, "")
            }
        }

        private fun mapCommentData(currentNode: GameNode, nodeData: ExportableNodeData) {
            if (currentNode.getComments().isNotBlank()) {
                nodeData.put(SGF_COMMENT, currentNode.getComments())
            }
        }



        private fun writeDelimiter(delimiter: Char, writer: BufferedWriter) {
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

        private class ExportableNodeData {

            private val dataMap = HashMap<String, ArrayList<String>>()

            fun put(sgfTag: String, value: String) {
                val singleValue = ArrayList<String>()
                singleValue.add(value)
                this.put(sgfTag, singleValue)
            }

            fun put(sgfTag: String, values: ArrayList<String>) {
                dataMap[sgfTag] = values
            }

            fun append(sgfTag: String, value: String) {
                dataMap.putIfAbsent(sgfTag, ArrayList())
                dataMap[sgfTag]!!.add(value)
            }

            fun contains(sgfTag: String, value: String?): Boolean {
                return if (!dataMap.containsKey(sgfTag)) {
                    false
                } else if (value != null) {
                    dataMap[sgfTag]!!.contains(value)
                } else {
                    return dataMap.containsKey(sgfTag)
                }
            }

            fun export(writer: BufferedWriter) {
                writeTags(dataMap, writer)
            }

            private fun writeTags(data: Map<String, List<String>>, writer: BufferedWriter) {
                for (key in data.keys) {
                    val value = data[key] ?: error("No value exists for key: $key")
                    writeTag(key, value, writer)
                }
            }

            private fun writeTag(tagKey: String, tagValues: List<String>, writer: BufferedWriter) {
                assert(tagValues.isNotEmpty()) { "Cannot write empty values" }
                writer.write(tagKey)
                for (value in tagValues) {
                    writeDelimiter(DELIM_TAG_VALUE_START, writer)
                    val escapedValue = escapeDelimiters(value)
                    writer.write(escapedValue)
                    writeDelimiter(DELIM_TAG_VALUE_END, writer)
                }
            }
        }
    }
}