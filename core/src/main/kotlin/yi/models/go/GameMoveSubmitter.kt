package yi.models.go

import java.util.*
import kotlin.collections.HashSet

/**
 * Responsible for the creation of [GameNode] and [StateDelta] for [GameModel].
 * In other words, the class manages the logic around move submission in the game of Go.
 */
internal object GameMoveSubmitter {

    /**
     * Creates a new node from the proposed move at a specified game position.
     * If [ignoreRules] is true, this method will first check if the proposed move complies
     * with the game rules before creating the new node.
     *
     * @param gameModel The game to create the new move for.
     * @param currentNode Parent node of the node to be created.
     * @param proposedMove Information pertaining to the proposed move, see [Stone]
     * @param ignoreRules Whether to ignore rule violation when evaluating this move.
     * If this is true, and the proposed move is not in compliance with game rules,
     * it will be played anyway. This is false by default.
     */
    fun createMoveNode(gameModel: GameModel, currentNode: GameNode,
                       proposedMove: Stone, ignoreRules: Boolean = false)
            : Pair<MoveValidationResult, GameNode?> {

        val validationResult: MoveValidationResult
        val update: StateDelta?

        val validationAndDelta = validateAndCreateStateUpdate(gameModel,
                currentNode, proposedMove, ignoreRules)
        validationResult = validationAndDelta.first

        if (validationResult != MoveValidationResult.OK) {
            return Pair(validationResult, null)
        }

        update = validationAndDelta.second!!

        return Pair(validationResult, GameNode(update))
    }

    /**
     * Creates a node representing the player at this turn has passed.
     *
     * @param currentNode Parent node of the node to be created.
     */
    fun createPassNode(currentNode: GameNode) : GameNode {
        val passStateUpdate = StateDelta.forPassMove(currentNode.getStateHash())
        return GameNode(passStateUpdate)
    }

    /**
     * Creates a node representing some stones in the previous position has been
     * edited outside of game rules. In most cases this means there is an absence
     * of primary move data on this node. One known exception is game records
     * produced by OGS (Online-Go Server), where all AI-review moves do not contain
     * primary move data.
     *
     * @param currentNode Parent node of the node to be created.
     */
    fun createStoneEditNode(currentNode: GameNode): GameNode {
        val stoneEditUpdate = StateDelta.forStoneEdit(currentNode.getStateHash())
        return GameNode(stoneEditUpdate)
    }

    /**
     * Creates a node to represent the top level node in the game tree. There should
     * only be one root node at any given time in one game tree.
     *
     * @param gameModel Game model to create the root node for.
     */
    fun createRootNode(gameModel: GameModel): GameNode {
        val width = gameModel.boardWidth
        val height = gameModel.boardHeight
        val emptyPositionHash = gameModel.stateHasher.computeEmptyPositionHash(width, height)
        val rootDelta = StateDelta.forRootNode(emptyPositionHash)
        return GameNode(rootDelta)
    }

    /**
     * Validates the [proposedMove] against the game rules and if the move is legal
     * (as given by [MoveValidationResult.OK]), returns a [StateDelta] representing the
     * game state updates caused by playing this move on the game board.
     *
     * @param gameModel Game information this move belongs to.
     * @param currentNode The position at which the new move will be validated.
     * @param proposedMove Information pertaining to the proposed move, see [Stone].
     * @param ignoreRules Whether to ignore the game rules when validating this move.
     */
    private fun validateAndCreateStateUpdate(gameModel: GameModel, currentNode: GameNode,
                                             proposedMove: Stone, ignoreRules: Boolean = false)
            : Pair<MoveValidationResult, StateDelta?> {

        val proposedMovePosition = proposedMove.getPosition(gameModel.boardWidth)
        if (proposedMovePosition < 0 || proposedMovePosition >= gameModel.getIntersectionCount())
            return Pair(MoveValidationResult.ERROR_POSITION_OUT_OF_BOUNDS, null)

        val currentGameState = gameModel.getGameState(currentNode)
        val currentGamePosition = currentGameState.boardPosition

        if (!ignoreRules) {
            val nextMoveNumber = gameModel.playedMoveHistory.size
            val expectedStoneColorThisTurn = gameModel.rules.getStoneColorForTurn(nextMoveNumber, gameModel.info.getHandicapCount() > 0)

            if (expectedStoneColorThisTurn != proposedMove.color) {
                return Pair(MoveValidationResult.ERROR_WRONG_STONE_COLOR_THIS_TURN, null)
            }

            if (currentGamePosition.getStoneColorAt(proposedMovePosition) != StoneColor.NONE) {
                return Pair(MoveValidationResult.ERROR_NON_EMPTY_INTERSECTION, null)
            }
        }

        /*
            Procedure reference: https://www.red-bean.com/sgf/ff5/m_vs_ax.htm
         */

        // Overwrite
        // Create a copy of the current position, overwrite the intersection at the proposed
        // move location with the proposed  stone color
        val testGamePosition = currentGamePosition.intersectionState.copyOf(currentGamePosition.intersectionState.size)
        testGamePosition[proposedMovePosition] = proposedMove.color

        // Check for captures:
        // Scan the intersections directly adjacent to the proposed move and obtain up to
        // four strings of connected stones.
        //
        // First check if any strings of the opponent color will be captured (i.e. have
        // zero liberty) upon playing the proposed move. (capture)
        //
        // Next check if any strings of the same color as the proposed move has zero
        // liberties upon playing the proposed move. (suicide)
        val x = proposedMove.x
        val y = proposedMove.y

        // Check for captures
        val strings = HashSet<StoneString>()

        addStringIfNotVisitedAlready(x, y - 1, strings, gameModel, testGamePosition)
        addStringIfNotVisitedAlready(x, y + 1, strings, gameModel, testGamePosition)
        addStringIfNotVisitedAlready(x - 1, y, strings, gameModel, testGamePosition)
        addStringIfNotVisitedAlready(x + 1, y, strings, gameModel, testGamePosition)
        addStringIfNotVisitedAlready(x, y, strings, gameModel, testGamePosition)

        val opponentStrings = HashSet<StoneString>()

        // Check for self capture
        val friendlyStrings = HashSet<StoneString>()

        // Sets values for the two buckets above
        collateStrings(proposedMove.color, friendlyStrings, opponentStrings, strings)

        var moveIsSuicidal = false
        val capturedStones = HashSet<Stone>()
        val capturesOfOpponent = getCapturesAndUpdateGamePosition(testGamePosition, opponentStrings, gameModel.boardWidth)
        val capturesOfSelf = if (capturesOfOpponent.isEmpty())
                                getCapturesAndUpdateGamePosition(testGamePosition, friendlyStrings, gameModel.boardWidth)
                             else
                                HashSet() // If we capture opponent first, then even if
                                          // the played move has no liberties, it's not a self capture

        if (!ignoreRules) {
            if (capturesOfOpponent.size == 0 && capturesOfSelf.size > 0) {
                moveIsSuicidal = true

                if (!gameModel.rules.allowSuicideMoves()) {
                    return Pair(MoveValidationResult.ERROR_MOVE_SUICIDAL, null)
                }
            }
        }

        capturedStones.addAll(capturesOfOpponent)
        capturedStones.addAll(capturesOfSelf)

        // Play move success
        val stoneUpdates = HashSet<Stone>(capturedStones)
        if (!moveIsSuicidal) {
            stoneUpdates.add(proposedMove)
        } else {
            // This move is part of the group that is captured, but we do not include it
            // in stoneUpdates because the net difference between the currentNode board
            // state and the next state produced by this move is the existing string
            // (excluding the new move) being removed off the board. This way we ensure the
            // hasher is in the correct state.
            stoneUpdates.remove(proposedMove)
        }

        val newStateHash = gameModel.stateHasher.computeUpdateHash(currentNode.getStateHash(), stoneUpdates)
        val stateHashHistory = gameModel.getStateHashHistory()

        if (!ignoreRules) {
            // Check if this new state repeats past board positions
            if (stateHashHistory.contains(newStateHash)) {
                // Determine the reason of repetition. The two important distinction is
                // an illegal ko recapture vs generic position repeat. An illegal ko
                // recapture is an immediate repetition of currentNode.parent state
                // (2 states ago from the perspective of the new node) Whereas a generic
                // position repeat is a repetition of any state other than a ko recapture.
                val newStatePosition = stateHashHistory.size
                val repeatHashPosition = stateHashHistory.indexOf(newStateHash)

                if (newStatePosition - repeatHashPosition == 2) {
                    // Lastly, make sure we're trying to capture 1 opponent stone this
                    // turn and during opponent's capture, it's also 1 stone, and
                    // that captured stone is at the same location we're trying to play.
                    val lastKoRecaptureCapturedStones = currentNode.getCaptures()

                    // Be as concise as possible because edge case 1x1 board self-capture
                    // can also result in the same conditions and it does not qualify as
                    // a ko recapture.
                    if (lastKoRecaptureCapturedStones.size == 1
                            && lastKoRecaptureCapturedStones.iterator().next() == proposedMove
                            && currentNode.getPrimaryMove()!!.color == proposedMove.color.getOpponent()) {
                        return Pair(MoveValidationResult.ERROR_KO_RECAPTURE, null)
                    }
                }

                return Pair(MoveValidationResult.ERROR_POSITION_REPEAT, null)
            }
        }

        val update = StateDelta.forProposedMove(proposedMove, capturedStones, newStateHash)
        return Pair(MoveValidationResult.OK, update)
    }

    private fun addStringIfNotVisitedAlready(x: Int, y: Int, strings: HashSet<StoneString>,
                                             gameModel: GameModel, testPosition: Array<StoneColor?>) {
        // Check if this intersection is already part of an existing string
        if (strings.stream().anyMatch { string -> string.stones.contains(x + y * gameModel.boardWidth) }) {
            return
        }

        getString(x, y, gameModel, testPosition)?.let { strings.add(it) }
    }

    private fun getCapturesAndUpdateGamePosition(gamePosition: Array<StoneColor?>,
                                                 strings: HashSet<StoneString>,
                                                 boardWidth: Int): HashSet<Stone> {
        val captures = HashSet<Stone>()

        strings.forEach { string ->
            // String is captured
            if (string.liberties.size == 0) {
                string.stones.stream().forEach { stonePosition ->
                    val stoneX = stonePosition % boardWidth
                    val stoneY = stonePosition / boardWidth
                    val stoneAtPosition = gamePosition[stoneX + stoneY * boardWidth]

                    if (stoneAtPosition != string.color)
                        throw IllegalStateException("The stone color at ($stoneX, $stoneY) " +
                                "does not match captured stone at the same position." +
                                " gamePosition: $stoneAtPosition stringColor: ${string.color} ")

                    // Erase the captured stone from the position
                    gamePosition[stoneX + stoneY * boardWidth] = StoneColor.NONE
                    captures.add(Stone(stoneX, stoneY, string.color))
                }
            }
        }

        return captures
    }

    /**
     * Sorts an array of [StoneString] into friendly and opponent buckets, and merge
     * strings that are equal.
     */
    private fun collateStrings(friendlyColor: StoneColor, friendlyStrings: HashSet<StoneString>,
                               opponentStrings: HashSet<StoneString>, strings: HashSet<StoneString>) {
        val uniqueStrings = strings.toSet() // Exploits the property of set that elements must be unique

        uniqueStrings.forEach { string ->
            if (string.color == friendlyColor)
                friendlyStrings.add(string)
            else
                opponentStrings.add(string)
        }
    }

    private fun getString(x: Int, y:Int, gameModel: GameModel, testPosition: Array<StoneColor?>): StoneString? {
        var string: StoneString? = null

        if (x >= 0 && x < gameModel.boardWidth && y >= 0 && y < gameModel.boardHeight) {
            val nonEmpty = testPosition[x + y * gameModel.boardWidth] != StoneColor.NONE

            if (nonEmpty) {
                string = StoneString(x, y, testPosition, gameModel.boardWidth, gameModel.boardHeight)
            }
        }

        return string
    }

    /**
     * Represents a group of stones of the same color that is adjacently connected.
     */
    private class StoneString(startX: Int, startY: Int, boardPosition: Array<StoneColor?>,
                              private val boardWidth: Int, private val boardHeight: Int) {
        // Intersections on the board that are empty and adjacent to the stones in this string
        val liberties = HashSet<Int>()
        val stones = HashSet<Int>()
        var color: StoneColor = StoneColor.NONE // A little hacky, but better than null-check

        init {
            val stringColor = boardPosition[getIndex(startX, startY)]

            assert(stringColor != StoneColor.NONE)

            stringColor?.let {
                this.color = stringColor

                // Seek all adjacent intersections for allied stones and add them to the string
                val visited = HashSet<Int>()
                val toVisit = HashSet<Int>()

                toVisit.add(startX + startY * boardWidth)

                while (toVisit.size > 0) {
                    val currentStonePosition = toVisit.iterator().next()

                    toVisit.remove(currentStonePosition)
                    visited.add(currentStonePosition)

                    val x = currentStonePosition % boardWidth
                    val y = currentStonePosition / boardWidth

                    val stoneAtPosition = boardPosition[currentStonePosition]

                    if (stoneAtPosition == stringColor) {
                        stones.add(currentStonePosition)

                        // Find adjacent intersections
                        val up: Stone? = getNeighbour(x, y - 1, boardPosition, visited)
                        val down: Stone? = getNeighbour(x, y + 1, boardPosition, visited)
                        val left: Stone? = getNeighbour(x - 1, y, boardPosition, visited)
                        val right: Stone? = getNeighbour(x + 1, y, boardPosition, visited)

                        processNeighbour(stringColor, up, toVisit)
                        processNeighbour(stringColor, down, toVisit)
                        processNeighbour(stringColor, left, toVisit)
                        processNeighbour(stringColor, right, toVisit)
                    }
                }
            }
        }

        private fun getNeighbour(x: Int, y: Int, boardPosition: Array<StoneColor?>,
                                 visited: HashSet<Int>): Stone? {
            if (x < 0
                || x >= boardWidth
                || y < 0
                || y >= boardHeight
                || visited.contains(getIndex(x, y))) {
                return null
            }

            return Stone(x, y, boardPosition[getIndex(x, y)]!!)
        }

        private fun processNeighbour(stringColor: StoneColor, neighbor: Stone?, toVisit: HashSet<Int>) {
            neighbor?.let {
                val position = neighbor.getPosition(boardWidth)

                val color = neighbor.color
                if (color != StoneColor.NONE && color == stringColor) {
                    toVisit.add(position)
                }
                if (color == StoneColor.NONE) {
                    liberties.add(position)
                }
            }
        }

        private fun getIndex(x: Int, y: Int): Int {
            return x + y * boardWidth
        }

        override fun equals(other: Any?): Boolean {
            if (other is StoneString) {
                if (other.liberties.size != this.liberties.size
                        || other.stones.size != this.stones.size
                        || other.color != this.color) {
                    return false
                }

                // Check for identical stone positions. Since the two strings are
                // known to have the same size, and that the set is unordered, we can
                // find inequality if there exists one stone in the other set that don't belong in this one.
                for (stonePosition in other.stones) {
                    if (!this.stones.contains(stonePosition))
                        return false
                }

                return true
            }
            return false
        }

        override fun hashCode(): Int {
            return Objects.hash(liberties, stones, color)
        }
    }

}