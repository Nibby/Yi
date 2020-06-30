package codes.nibby.yi.go

import codes.nibby.yi.common.MoveNode
import java.util.*
import kotlin.collections.HashSet

/**
 * Responsible for the creation of [MoveNode] and [GameStateUpdate] for [GoGameModel]. In other words, the class manages the logic around
 * move submission in the game of Go.
 */
internal object GoMoveHelper {

    /**
     * Creates a new move node from the proposed move at a specified game position. If [validateBeforeCreate] is true, this method will first check
     * if the proposed move complies with the game rules before creating the new node.
     *
     * This resulting node can then be submitted to the game tree through [GoGameEngine].
     *
     * @param gameModel The game to create the new move for.
     * @param currentPosition The parent node of the newly created move node. In other words, the game state on which to play the new move.
     * @param validateBeforeCreate Whether to validate the move against the game rules first. If this is non-null, and the proposed move is not in compliance with game rules,
     *                             then no new node be created.
     * @param proposedMove Information pertaining to the proposed move, see [StoneData]
     */
    fun createMoveNodeForProposedMove(gameModel: GoGameModel, currentPosition: MoveNode<GameStateUpdate>, validateBeforeCreate: Boolean, proposedMove: StoneData)
            : Pair<MoveValidationResult, MoveNode<GameStateUpdate>?> {

        var validationResult = MoveValidationResult.OK
        val update: GameStateUpdate?

        if (validateBeforeCreate) {
            val validationAndDelta = validateProposedMoveAndCreateStateUpdate(gameModel, currentPosition, proposedMove)
            validationResult = validationAndDelta.first

            if (validationResult != MoveValidationResult.OK) {
                return Pair(validationResult, null)
            }

            update = validationAndDelta.second!!
        } else {
            update = GameStateUpdateFactory.createForProposedMove(proposedMove, HashSet(), 0)
        }

        return Pair(validationResult, MoveNode(update))
    }

    fun createMoveNodeForPass(gameModel: GoGameModel, currentPosition: MoveNode<GameStateUpdate>) : MoveNode<GameStateUpdate> {
        val passStateUpdate = GameStateUpdateFactory.createForPassMove(currentPosition.data!!.stateHash)
        return MoveNode(passStateUpdate)
    }

    fun createMoveNodeForResignation(gameModel: GoGameModel, currentPosition: MoveNode<GameStateUpdate>) : MoveNode<GameStateUpdate> {
        val resignStateUpdate = GameStateUpdateFactory.createForResignationMove(currentPosition.data!!.stateHash)
        return MoveNode(resignStateUpdate)
    }

    /**
     * Validates the [proposedMove] against the game rules and if the move is legal (as given by [MoveValidationResult.OK]), returns a [GameStateUpdate]
     * representing the game state updates caused by playing this move on the game board.
     *
     * @param gameModel Game information this move belongs to
     * @param currentNode The position at which the new move will be validated
     * @param proposedMove Information pertaining to the proposed move, see [StoneData]
     */
    fun validateProposedMoveAndCreateStateUpdate(gameModel: GoGameModel, currentNode: MoveNode<GameStateUpdate>, proposedMove: StoneData)
            : Pair<MoveValidationResult, GameStateUpdate?> {

        val proposedMovePosition = proposedMove.getIndex(gameModel.boardWidth)
        if (proposedMovePosition < 0 || proposedMovePosition >= gameModel.getIntersectionCount())
            return Pair(MoveValidationResult.ERROR_POSITION_OUT_OF_BOUNDS, null)

        val nextTurnNumber = currentNode.getDistanceToRoot() + 1
        val expectedStoneColorThisTurn = gameModel.rules.getStoneColorForTurn(nextTurnNumber)
        if (expectedStoneColorThisTurn != proposedMove.stoneColor)
            return Pair(MoveValidationResult.ERROR_WRONG_STONE_COLOR_THIS_TURN, null)

        val currentGameState = gameModel.resolveGameState(currentNode)
        val currentGamePosition = currentGameState.gamePosition
        if (currentGamePosition.getStoneColorAt(proposedMovePosition) != GoStoneColor.NONE)
            return Pair(MoveValidationResult.ERROR_NON_EMPTY_INTERSECTION, null)

        /*
            Procedure reference: https://www.red-bean.com/sgf/ff5/m_vs_ax.htm
         */

        // Overwrite
        // Create a copy of the current position, overwrite the intersection at the proposed move location with the proposed
        // stone color
        val testGamePosition = currentGamePosition.intersectionState.copyOf(currentGamePosition.intersectionState.size)
        testGamePosition[proposedMovePosition] = proposedMove.stoneColor

        // Check for captures:
        // Scan the intersections directly adjacent to the proposed move and obtain up to four strings of connected stones
        // First check if any strings of the opponent color will be captured (i.e. have zero liberty) upon playing the proposed move. (capture)
        // Next check if any strings of the same color as the proposed move has zero liberties upon playing the proposed move. (suicide)
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
        collateStrings(proposedMove.stoneColor, friendlyStrings, opponentStrings, strings)

        val capturedStones = HashSet<StoneData>()
        val capturesOfOpponent = getCaptures(opponentStrings, gameModel)
        val capturesOfSelf = if (capturesOfOpponent.isEmpty()) getCaptures(friendlyStrings, gameModel) else HashSet() // If we capture opponent first, then even if the played move has no liberties, it's not a self capture

        if (capturesOfOpponent.size == 0 && capturesOfSelf.size > 0 && !gameModel.rules.allowSuicideMoves()) {
            // Suicide
            return Pair(MoveValidationResult.ERROR_MOVE_SUICIDAL, null)
        }

        capturedStones.addAll(capturesOfOpponent)
        capturedStones.addAll(capturesOfSelf)

        // Play move success
        val stoneUpdates = HashSet<StoneData>(capturedStones)
        stoneUpdates.add(proposedMove)

        val newStateHash = gameModel.stateHasher.calculateUpdateHash(currentNode.data!!.stateHash, stoneUpdates)
        val stateHashHistory = gameModel.getStateHashHistory()

        // Check if this new state repeats past board positions
        if (stateHashHistory.contains(newStateHash)) {
            // Determine the reason of repetition. The two important distinction is an illegal ko recapture vs generic position repeat.
            // An illegal ko recapture is an immediate repetition of currentNode.parent state (2 states ago from the perspective of the new node)
            // Whereas a generic position repeat is a repetition of any state other than a ko recapture.
            val newStatePosition = stateHashHistory.size
            val repeatHashPosition = stateHashHistory.indexOf(newStateHash)

            if (newStatePosition - repeatHashPosition == 2) {
                // Lastly, make sure we're trying to capture 1 opponent stone this turn and during opponent's capture, it's also 1 stone, and
                // that captured stone is at the same location we're trying to play.
                val lastKoRecaptureCapturedStones = currentNode.data!!.captures

                // Be as concise as possible because edge case 1x1 board self-capture can also result in the same conditions
                // and it does not qualify as a ko recapture.
                if (lastKoRecaptureCapturedStones.size == 1
                        && lastKoRecaptureCapturedStones.iterator().next() == proposedMove
                        && currentNode.data!!.primaryMove!!.stoneColor == proposedMove.stoneColor.getOpponent()) {
                    return Pair(MoveValidationResult.ERROR_KO_RECAPTURE, null)
                }
            }

            return Pair(MoveValidationResult.ERROR_POSITION_REPEAT, null)
        }

        val update = GameStateUpdateFactory.createForProposedMove(proposedMove, capturedStones, newStateHash)
        return Pair(MoveValidationResult.OK, update)
    }

    private fun addStringIfNotVisitedAlready(x: Int, y: Int, strings: HashSet<StoneString>, gameModel: GoGameModel, testPosition: Array<GoStoneColor?>) {
        // Check if this intersection is already part of an existing string
        if (strings.stream().anyMatch { string -> string.stones.contains(x + y * gameModel.boardWidth) })
            return

        getString(x, y, gameModel, testPosition)?.let { strings.add(it) }
    }

    private fun getCaptures(strings: HashSet<StoneString>, gameModel: GoGameModel): HashSet<StoneData> {
        val captures = HashSet<StoneData>()

        strings.forEach { string ->
            // String is captured
            if (string.liberties.size == 0) {
                string.stones.stream().forEach { position ->
                    val stoneX = position % gameModel.boardWidth
                    val stoneY = position / gameModel.boardHeight
                    captures.add(StoneData(stoneX, stoneY, string.color))
                }
            }
        }

        return captures
    }

    /**
     * Sorts an array of [StoneString] into friendly and opponent buckets, and merge strings that are equal.
     */
    private fun collateStrings(friendlyColor: GoStoneColor, friendlyStrings: HashSet<StoneString>, opponentStrings: HashSet<StoneString>, strings: HashSet<StoneString>) {
        val uniqueStrings = strings.toSet() // Exploits the property of set that elements must be unique

        uniqueStrings.forEach { string ->
            if (string.color == friendlyColor)
                friendlyStrings.add(string)
            else
                opponentStrings.add(string)
        }
    }

    private fun getString(x: Int, y:Int, gameModel: GoGameModel, testPosition: Array<GoStoneColor?>): StoneString? {
        var string: StoneString? = null

        if (x >= 0 && x < gameModel.boardWidth && y >= 0 && y < gameModel.boardHeight) {
            val nonEmpty = testPosition[x + y * gameModel.boardWidth] != GoStoneColor.NONE

            if (nonEmpty) {
                string = StoneString(x, y, testPosition, gameModel.boardWidth, gameModel.boardHeight)
            }
        }

        return string
    }

    /**
     * Represents a group of stones of the same color that is adjacently connected.
     */
    private class StoneString(startX: Int, startY: Int, boardPosition: Array<GoStoneColor?>, private val boardWidth: Int, private val boardHeight: Int) {
        // Intersections on the board that are empty and adjacent to the stones in this string
        val liberties = HashSet<Int>()
        val stones = HashSet<Int>()
        var color: GoStoneColor = GoStoneColor.NONE // A little hacky, but better than null-check

        init {
            val stringColor = boardPosition[getIndex(startX, startY)]

            assert(stringColor != GoStoneColor.NONE)

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
                        val up: StoneData? = getNeighbour(x, y - 1, boardPosition, visited)
                        val down: StoneData? = getNeighbour(x, y + 1, boardPosition, visited)
                        val left: StoneData? = getNeighbour(x - 1, y, boardPosition, visited)
                        val right: StoneData? = getNeighbour(x + 1, y, boardPosition, visited)

                        processNeighbour(stringColor, up, visited, toVisit)
                        processNeighbour(stringColor, down, visited, toVisit)
                        processNeighbour(stringColor, left, visited, toVisit)
                        processNeighbour(stringColor, right, visited, toVisit)
                    }
                }
            }
        }

        private fun getNeighbour(x: Int, y: Int, boardPosition: Array<GoStoneColor?>, visited: HashSet<Int>): StoneData? {
            if (x < 0 || x >= boardWidth || y < 0 || y >= boardHeight || visited.contains(getIndex(x, y)))
                return null

            return StoneData(x, y, boardPosition[getIndex(x, y)]!!)
        }

        private fun processNeighbour(stringColor: GoStoneColor, neighbor: StoneData?, visited: HashSet<Int>, toVisit: HashSet<Int>) {
            neighbor?.let {
                val position = neighbor.getIndex(boardWidth)

                val color = neighbor.stoneColor
                if (color != GoStoneColor.NONE && color == stringColor) {
                    toVisit.add(position)
                }
                if (color == GoStoneColor.NONE) {
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

                // Check for identical stone positions. Since the two strings are known to have the same size, and that
                // the set is unordered, we can find inequality if there exists one stone in the other set that don't belong in this one.
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