package codes.nibby.yi.weiqi

import codes.nibby.yi.common.MoveNode
import java.util.*
import kotlin.collections.HashSet

/**
 * Responsible for the creation of [MoveNode] and [GameStateUpdate] for [GoGameModel]. In other words, the class manages the logic around
 * move submission in the game of Go.
 */
internal object GoMoveHelper {

    /**
     * Same as [createMoveNode] except the new move node is created at the current position defined by [gameModel]
     *
     * @param gameModel The game to create the new move for.
     * @param validateAgainstRules Whether to validate the move against the game rules first. If this is non-null, and the proposed move is not
     *                             in compliance with game rules, then no new node be created.
     * @param proposedMove Information pertaining to the proposed move, see [StoneData]
     */
    fun createMoveNode(gameModel: GoGameModel, validateAgainstRules: Boolean, proposedMove: StoneData)
            : Pair<MoveValidationResult, MoveNode<GameStateUpdate>?> {

        return createMoveNode(gameModel, gameModel.currentNode, validateAgainstRules, proposedMove)
    }

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
    fun createMoveNode(gameModel: GoGameModel, currentPosition: MoveNode<GameStateUpdate>, validateBeforeCreate: Boolean, proposedMove: StoneData)
            : Pair<MoveValidationResult, MoveNode<GameStateUpdate>?> {

        var validationResult = MoveValidationResult.OK
        val update: GameStateUpdate?

        if (validateBeforeCreate) {
            val validationAndDelta = validateAndCreateDelta(gameModel, currentPosition, proposedMove)
            validationResult = validationAndDelta.first

            if (validationResult != MoveValidationResult.OK) {
                return Pair(validationResult, null)
            }

            update = validationAndDelta.second!!
        } else {
            update = GameStateUpdateFactory.createPrimaryMoveUpdate(proposedMove, HashSet(), 0)
        }

        return Pair(validationResult, MoveNode(update))
    }

    /**
     * Validates the [proposedMove] against the game rules and if the move is legal (as given by [MoveValidationResult.OK]), returns a [GameStateUpdate]
     * representing the game state updates caused by playing this move on the game board.
     *
     * @param gameModel Game information this move belongs to
     * @param currentNode The position at which the new move will be validated
     * @param proposedMove Information pertaining to the proposed move, see [StoneData]
     */
    fun validateAndCreateDelta(gameModel: GoGameModel, currentNode: MoveNode<GameStateUpdate>, proposedMove: StoneData)
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

        // Step 1: Overwrite
        // Create a copy of the current position, overwrite the intersection at the proposed move location with the proposed
        // stone color
        val testGamePosition = currentGamePosition.intersectionState.copyOf(currentGamePosition.intersectionState.size)
        testGamePosition[proposedMovePosition] = proposedMove.stoneColor

        // Step 2: Check for captures
        // Scan the intersections directly adjacent to the proposed move and obtain up to four strings of connected stones
        // First check if any strings of the opponent color will be captured (i.e. have zero liberty) upon playing the proposed move. (capture)
        // Next check if any strings of the same color as the proposed move has zero liberties upon playing the proposed move. (suicide)

        val x = proposedMove.x
        val y = proposedMove.y

        val stringUp: StoneString? = getString(x, y - 1, gameModel, testGamePosition)
        val stringDown: StoneString? = getString(x, y + 1, gameModel, testGamePosition)
        val stringLeft: StoneString? = getString(x - 1, y, gameModel, testGamePosition)
        val stringRight: StoneString? = getString(x + 1, y, gameModel, testGamePosition)

        val friendlyStrings = HashSet<StoneString>()
        val opponentStrings = HashSet<StoneString>()

        // Sets values for the two buckets above
        coalesceStrings(proposedMove.stoneColor, friendlyStrings, opponentStrings, stringUp, stringLeft, stringDown, stringRight)

        val capturedStones = HashSet<StoneData>()
        val capturesOfOpponent = getCaptures(opponentStrings, gameModel)
        val capturesOfSelf = getCaptures(friendlyStrings, gameModel)

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
        val update = GameStateUpdateFactory.createPrimaryMoveUpdate(proposedMove, capturedStones, newStateHash)
        return Pair(MoveValidationResult.OK, update)
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
    private fun coalesceStrings(friendlyColor: GoStoneColor, friendlyStrings: HashSet<StoneString>, opponentStrings: HashSet<StoneString>, vararg strings: StoneString?) {
        val uniqueStrings = strings.toSet() // Exploits the property of set that elements must be unique

        uniqueStrings.forEach { string ->
            string?.let {
                if (it.color == friendlyColor)
                    friendlyStrings.add(it)
                else
                    opponentStrings.add(it)
            }
        }
    }

    private fun getString(x: Int, y:Int, gameModel: GoGameModel, testPosition: Array<GoStoneColor?>): StoneString? {
        var string: StoneString? = null

        val inBounds = x >= 0 && x < gameModel.boardWidth && y >= 0 && y < gameModel.boardHeight
        val nonEmpty = testPosition[x + y * gameModel.boardWidth] != GoStoneColor.NONE

        if (inBounds && nonEmpty) {
            string = StoneString(x, y, testPosition, gameModel.boardWidth, gameModel.boardHeight)
        }

        return string
    }

    /**
     * Represents a group of stones of the same color that is adjacently connected.
     */
    private class StoneString(startX: Int, startY: Int, boardPosition: Array<GoStoneColor?>, private val boardWidth: Int, boardHeight: Int) {
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
                    val x = currentStonePosition % boardWidth
                    val y = currentStonePosition / boardWidth

                    val stoneAtPosition = boardPosition[currentStonePosition]

                    if (stoneAtPosition == stringColor) {
                        stones.add(currentStonePosition)

                        // Find adjacent intersections
                        var up: StoneData? = null
                        var down: StoneData? = null
                        var left: StoneData? = null
                        var right: StoneData? = null

                        // Assert that the position of each GoStoneColor is non-null because the copied position
                        // is of the same size as the original position, so no new element should be inserted.
                        if (x - 1 >= 0) {
                            left = StoneData(x - 1, y, boardPosition[getIndex(x - 1, y)]!!)
                        }

                        if (x + 1 < boardWidth) {
                            right = StoneData(x + 1, y, boardPosition[getIndex(x + 1, y)]!!)
                        }

                        if (y - 1 >= 0) {
                            up = StoneData(x, y - 1, boardPosition[getIndex(x, y - 1)]!!)
                        }

                        if (y + 1 < boardHeight) {
                            down = StoneData(x, y + 1, boardPosition[getIndex(x, y + 1)]!!)
                        }

                        processNeighbor(stringColor, up, visited, toVisit)
                        processNeighbor(stringColor, down, visited, toVisit)
                        processNeighbor(stringColor, left, visited, toVisit)
                        processNeighbor(stringColor, right, visited, toVisit)
                    }
                }
            }
        }

        private fun processNeighbor(stringColor: GoStoneColor, neighbor: StoneData?, visited: HashSet<Int>, toVisit: HashSet<Int>) {
            neighbor?.let {
                val position = neighbor.getIndex(boardWidth)
                visited.add(position)

                val color = neighbor.stoneColor
                if (color == stringColor) {
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