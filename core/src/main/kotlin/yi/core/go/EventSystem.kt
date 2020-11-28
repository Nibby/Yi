package yi.core.go

/**
 * An event for one node in a [GameModel]
 */
class NodeEvent constructor (val node: GameNode)

/**
 * An event source for events related to one node in a [GameModel].
 */
class NodeEventHook : EventHook<NodeEvent>()