package yi.core.common

interface TreeBasedGameModel<NodeData> {

    fun getCurrentMove(): GameNode<NodeData>

    fun getCurrentMoveNumber(): Int

    fun setCurrentMove(node: GameNode<NodeData>)

    fun getPreviousMove(): GameNode<NodeData>?

    fun toPreviousMove(): GameNode<NodeData>?

    fun getNextMove(): GameNode<NodeData>?

    fun getNextMoves(): Collection<GameNode<NodeData>>

    fun toNextMove(): GameNode<NodeData>?

    fun submitMove(node: GameNode<NodeData>)

}