package yi.core.common

class NodeEvent<NodeData> constructor (val node: GameNode<NodeData>?)
class NodeEventHook<NodeData> : EventHook<NodeEvent<NodeData>?>()