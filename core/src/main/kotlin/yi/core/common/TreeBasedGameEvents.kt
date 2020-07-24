package yi.core.common

/**
 * An event for one node in a [TreeBasedGameModel]
 */
class NodeEvent<NodeData> constructor (val node: GameNode<NodeData>?)

/**
 * An event source for events related to one node in a [TreeBasedGameModel].
 */
class NodeEventHook<NodeData> : EventHook<NodeEvent<NodeData>?>()