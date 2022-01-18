@file:Suppress("MemberVisibilityCanBePrivate") // Some methods may not be used, but is intended to be part of the public API

package codes.nibby.yi.models.editor

import codes.nibby.yi.models.*
import codes.nibby.yi.models.Annotation
import codes.nibby.yi.models.editor.edit.GameModelEdit

/**
 * Component that directly manages the manipulation of [GameModel] data. Data can be
 * edited directly using the supplied methods, or backed with undo system support
 * using [recordAndApplyUndoable].
 */
class GameModelEditor(private val model: GameModel) {

    var isEditable: Boolean = true
    val undoSystem = GameModelUndoSystem(model)

    /**
     * Submits and applies an undoable edit to the game model.
     *
     * The edit supports undo/redo using [GameModelUndoSystem.performUndo] and
     * [GameModelUndoSystem.performRedo].
     */
    fun recordAndApplyUndoable(edit: GameModelEdit) {
        checkModelEditable()
        try {
            val isSuccessful = edit.performChanges(model)

            if (isSuccessful) {
                undoSystem.record(edit)
            }
        } catch (ex: GameModelEditException) {
            throw GameModelEditException("Failed to perform changes: $edit", edit)
        }
    }

    /**
     * First checks if the move can be played at the current game position in compliance
     * with the game rules. If successful, appends a new node to the game tree.
     *
     * If the move is not compliant with the game rules, the method will fail silently
     * without submitting any new node to the game tree. To play a sequence of moves
     * ensuring each move is played correctly, use [beginMoveSequence] instead.
     *
     * If there is another move that is already played at the same co-ordinates following
     * the current move, then no new node will be submitted to the game tree. Instead,
     * the current position will be set to that move.
     *
     * @return The result of the request. See [MoveSubmitResult] for more information.
     */
    fun addMove(x: Int, y: Int): MoveSubmitResult {
        return addMove(model.currentNode, x, y)
    }

    fun addMove(parent: GameNode, x: Int, y: Int): MoveSubmitResult {
        checkModelEditable()

        var identicalExistingMove: GameNode? = null

        for (child in parent.children) {
            child.getPrimaryMove()?.let {
                val moveX = it.x
                val moveY = it.y

                if (moveX == x && moveY == y) {
                    identicalExistingMove = child
                }
            }

            if (identicalExistingMove != null) {
                break
            }
        }

        val validationResult: MoveValidationResult
        val node: GameNode?
        val movePlayed: Boolean
        val reusedExistingNode: Boolean

        if (identicalExistingMove == null) {
            val validationAndNewNode = GameMoveSubmitter.createMoveNode(
                model,
                parent,
                Stone(x, y, model.getNextTurnStoneColor())
            )

            validationResult = validationAndNewNode.first
            node = validationAndNewNode.second

            if (validationResult == MoveValidationResult.OK) {
                addNode(node!!) // New node should not be null if validation result checks out
            }

            movePlayed = validationResult == MoveValidationResult.OK
            reusedExistingNode = false
            model.isModified = true
        } else {
            model.currentNode =identicalExistingMove!!

            validationResult = MoveValidationResult.OK
            node = identicalExistingMove
            movePlayed = true
            reusedExistingNode = true
        }

        return MoveSubmitResult(validationResult, node, movePlayed, reusedExistingNode)
    }

    /**
     * Forcefully submit a child move to the current move without validating it against
     * the game rules. This means that the move will be placed at the position regardless
     * if it is legal. Nearby captures will still be calculated.
     *
     * This method is often used by file importers to tolerate potentially corrupt or
     * incorrect game states when importing a game file. Normal gameplay should submit
     * moves using [addMove] and its variants instead.
     *
     * Use this method with prudence, as it may result in an erroneous game state.
     */
    fun addMoveWithoutValidation(x: Int, y: Int): MoveSubmitResult {
        checkModelEditable()
        return addMoveWithoutValidation(model.currentNode, x, y, model.getNextTurnStoneColor())
    }

    /**
     * Forcefully submit a child move to an existing node in the game tree without
     * validating it against the game rules. This means that the move will be placed
     * at the position regardless if it is legal. Nearby captures will still be calculated.
     *
     * This method is often used by file importers to tolerate potentially corrupt or
     * incorrect game states when importing a game file. Normal gameplay should submit
     * moves using [addMove] and its variants instead.
     *
     * Use this method with prudence, as it may result in an erroneous game state.
     */
    fun addMoveWithoutValidation(parent: GameNode, x: Int, y: Int,
                                 stoneColor: StoneColor
    ): MoveSubmitResult {
        checkModelEditable()
        val validationAndNewNode = GameMoveSubmitter.createMoveNode(model,
            parent, Stone(x, y, stoneColor), ignoreRules = true)
        val newNode: GameNode? = validationAndNewNode.second
        addNode(parent, newNode!!)

        return MoveSubmitResult(MoveValidationResult.OK, newNode, true)
    }

    /**
     * Play a pass for the next turn.
     */
    fun addPass(): MoveSubmitResult {
        checkModelEditable()
        var existingContinuation: GameNode? = null

        for (child in model.currentNode.children) {
            if (child.getType() == GameNodeType.PASS) {
                existingContinuation = child
            }
        }

        return if (existingContinuation != null) {
            model.currentNode = existingContinuation
            MoveSubmitResult(
                MoveValidationResult.OK, existingContinuation,
                isPlayed = true, isReusingExistingNode = true)
        } else {
            val newNode = GameMoveSubmitter.createPassNode(model.currentNode)
            addNode(newNode)
            MoveSubmitResult(
                MoveValidationResult.OK, newNode,
                isPlayed = true, isReusingExistingNode = false)
        }
    }

    /**
     * Submits a new node of type [GameNodeType.STONE_EDIT].
     *
     * The purpose of this node is to store all the manual edits made to the stones in the
     * current game position. Multiple edits to stones should be made under a single stone
     * edit node. In other words, for game records created within this game model, there
     * should never be multiple consecutive stone edit nodes.
     *
     * Consecutive stone edit nodes are permitted in the game tree because other styles
     * have been adopted by other programs / go servers.
     *
     * @return The newly created tree node for stone edits.
     */
    fun addStoneEditNode(): GameNode {
        checkModelEditable()
        val node = GameMoveSubmitter.createStoneEditNode(model.currentNode)
        addNode(node)
        return node
    }

    /**
     * Set the move comments on the current node.
     *
     * This method emits [GameModel.onNodeDataUpdate] and [GameModel.onCurrentNodeDataUpdate]
     * events.
     */
    fun setCommentOnCurrentNode(comment: String) {
        checkModelEditable()
        setComment(model.currentNode, comment)
    }

    /**
     * Set the move comments on a node in this model.
     *
     * This method emits an [GameModel.onNodeDataUpdate] event and optionally emits
     * an [GameModel.onCurrentNodeDataUpdate] event if the node being edited is the
     * current node.
     */
    fun setComment(nodeToEdit: GameNode, comment: String) {
        checkModelEditable()
        nodeToEdit.setComments(comment)
        model.onNodeDataUpdate().fireEvent(NodeEvent(nodeToEdit))
        model.isModified = true
    }

    /**
     * Adds one edited stone to the current node.
     *
     * This method emits an [GameModel.onNodeDataUpdate] event.
     */
    fun addStoneEditToCurrentNode(stoneEdit: Stone) {
        checkModelEditable()
        addStoneEdit(model.currentNode, stoneEdit)
    }

    /**
     * Adds one edited stone to a specified node.
     *
     * This method emits an [GameModel.onNodeDataUpdate] event.
     */
    fun addStoneEdit(nodeToEdit: GameNode, stoneEdit: Stone) {
        checkModelEditable()
        addStoneEdits(nodeToEdit, setOf(stoneEdit))
    }

    /**
     * Adds a collection of stone edits to the current node.
     *
     * This method emits only one [GameModel.onNodeDataUpdate] event after all stones have been
     * added.
     */
    fun addStoneEdits(nodeToEdit: GameNode, stoneEdits: Collection<Stone>) {
        checkModelEditable()
        nodeToEdit.addStoneEdits(stoneEdits, model.stateHasher, model.boardWidth, model.boardHeight)
        model.onNodeDataUpdate().fireEvent(NodeEvent(nodeToEdit))
        model.isModified = true
    }

    /**
     * Removes one edited stone from the specified node. If that stone edit does not exist,
     * does nothing.
     *
     * This method emits an [GameModel.onNodeDataUpdate] event.
     */
    fun removeStoneEdit(nodeToEdit: GameNode, stoneEdit: Stone) {
        checkModelEditable()
        nodeToEdit.removeStoneEdit(stoneEdit, model.stateHasher, model.boardWidth, model.boardHeight)
        model.onNodeDataUpdate().fireEvent(NodeEvent(nodeToEdit))
        model.isModified = true
    }

    /**
     * Adds one [Annotation] to the current move.
     *
     * This method emits an [GameModel.onCurrentNodeDataUpdate] event.
     */
    fun addAnnotationToCurrentNode(annotation: Annotation) {
        checkModelEditable()
        addAnnotationsToCurrentNode(listOf(annotation))
    }

    /**
     * Calls [addAnnotations] using the current move.
     */
    fun addAnnotationsToCurrentNode(annotations: Collection<Annotation>) {
        checkModelEditable()
        addAnnotations(model.currentNode, annotations)
    }

    /**
     * Adds one or more [Annotation] to the current move.
     *
     * This method emits an [GameModel.onCurrentNodeDataUpdate] event only once after all
     * annotations have been added.
     */
    fun addAnnotations(nodeToEdit: GameNode, annotations: Collection<Annotation>) {
        checkModelEditable()
        nodeToEdit.addAnnotations(annotations)
        model.onNodeDataUpdate().fireEvent(NodeEvent(nodeToEdit))
        model.isModified = true
    }

    /**
     * Invokes [removeAnnotation] using the current node.
     */
    fun removeAnnotationFromCurrentNode(x: Int, y: Int) {
        checkModelEditable()
        removeAnnotation(model.currentNode, x, y)
    }

    /**
     * Removes all annotation at the specified point from the current move. If one or
     * more directional annotation has a position here, it will be removed also.
     *
     * This method emits an [GameModel.onNodeDataUpdate] event once after all annotations
     * have been removed.
     */
    fun removeAnnotation(nodeToEdit: GameNode, x: Int, y: Int) {
        checkModelEditable()
        val annotationsToRemove = HashSet<Annotation>()

        val annotations = nodeToEdit.getAnnotations()
        for (annotation in annotations) {
            val onThisPoint = annotation.isOccupyingPosition(x, y)

            if (onThisPoint) {
                annotationsToRemove.add(annotation)
            }
        }

        model.currentNode.removeAnnotations(annotationsToRemove)
        model.onNodeDataUpdate().fireEvent(NodeEvent(nodeToEdit))
        model.isModified = true
    }

    /**
     * Deletes an annotation from the current node.
     *
     * This method emits a [GameModel.onNodeDataUpdate] event.
     */
    fun removeAnnotationFromCurrentNode(annotation: Annotation) {
        checkModelEditable()
        removeAnnotation(model.currentNode, annotation)
    }

    /**
     * Deletes an annotation from a specified node.
     *
     * This method emits a [GameModel.onNodeDataUpdate] event.
     */
    fun removeAnnotation(nodeToEdit: GameNode, annotation: Annotation) {
        checkModelEditable()
        nodeToEdit.removeAnnotation(annotation)
        model.onNodeDataUpdate().fireEvent(NodeEvent(nodeToEdit))
        model.isModified = true
    }

    /**
     * Deletes the given annotations from a specified node.
     *
     * This method emits a single [GameModel.onNodeDataUpdate] event after all annotations have
     * been removed.
     */
    fun removeAnnotations(nodeToEdit: GameNode, annotations: Collection<Annotation>) {
        checkModelEditable()
        nodeToEdit.removeAnnotations(annotations)
        model.onNodeDataUpdate().fireEvent(NodeEvent(nodeToEdit))
        model.isModified = true
    }

    /**
     * Removes the node and its subtree from the model. If the current move is part of the
     * subtree deleted, it will be reset to the parent of the deleted node.
     *
     * If the node to be deleted is the root, only its children will be removed and the
     * current move will be set to root.
     *
     * This method emits an [GameModel.onNodeRemove] event.
     */
    fun removeNodeSubtree(node: GameNode) {
        checkModelEditable()

        val newCurrentMove: GameNode? = getNewCurrentNodeAfterNodeRemoval(node)
        model.gameTree.removeNodeSubtree(node)
        model.onNodeRemove().fireEvent(NodeEvent(node))
        newCurrentMove?.let { model.currentNode = it }

        model.isModified = true
    }

    /**
     * Removes the node from the game tree by removing it from its parent's list of
     * children. All hierarchy information will be kept intact on the removed node and
     * its descendent nodes. If the current move is part of the subtree deleted, it will
     * be reset to the parent of the deleted node.
     *
     * It is recommended to use [removeNodeSubtree] for more permanent deletion changes,
     * as preserving dead branch hierarchy may lead to memory leaks. This method may be
     * useful if the removal is temporary, as it introduces less work to restore the
     * removed node later.
     *
     * This method emits an [GameModel.onNodeRemove] event.
     *
     * @throws IllegalStateException When attempting to remove the root node.
     */
    fun removeNode(node: GameNode) {
        check (!node.isRoot()) {
            "Cannot remove root node"
        }

        checkModelEditable()

        val newCurrentMove: GameNode? = getNewCurrentNodeAfterNodeRemoval(node)
        model.gameTree.removeNodeShallow(node)
        model.onNodeRemove().fireEvent(NodeEvent(node))
        newCurrentMove?.let { model.currentNode = it }

        model.isModified = true
    }

    private fun getNewCurrentNodeAfterNodeRemoval(nodeToRemove: GameNode): GameNode? {
        // Do it here because the node lineage will be destroyed after removing node subtree.
        // and we want to fire the current node change event after node removal event.
        val readjustCurrentNode = model.currentNode.isContinuationOf(nodeToRemove)

        // Since root cannot be deleted, there will always be a parent for the node to be removed.
        return if (readjustCurrentNode) {
            if (nodeToRemove.isRoot()) nodeToRemove
            else nodeToRemove.parent!!
        } else null
    }

    /**
     * Appends the node to the game tree and sets the current move to the new node.
     *
     * At this point the move is assumed to be legal and playable. Any rule validation should
     * take place before calling this method. In which case it may be preferable to define a
     * custom entry point to the move submission process.
     *
     * This method first emits an [GameModel.onNodeAdd] event, followed by
     * [GameModel.onCurrentNodeChange].
     */
    fun addNode(node: GameNode) {
        checkModelEditable()
        addNode(model.currentNode, node)
    }

    /**
     * Appends the child to the parent node and set the child as the current move. The
     * parent node must already exist in the game tree.
     *
     * At this point the move is assumed to be legal and playable. Any rule validation
     * should take place before calling this method. In which case it may be preferable
     * to define a custom entry point to the move submission process.
     *
     * This method first emits an [GameModel.onNodeAdd] event, followed by
     * [GameModel.onCurrentNodeChange].
     */
    fun addNode(parent: GameNode, child: GameNode) {
        checkModelEditable()
        appendNode(parent, child)
        model.currentNode = child
        model.isModified = true
    }

    /**
     * Appends the node to the current node. If the current node has no children, the node
     * will become its main branch continuation. Otherwise, it becomes a variation. This
     * method will not update the current move to the new node.
     *
     * Use [addMove] to append and set the current node.
     *
     * This method emits an [GameModel.onNodeAdd] event.
     */
    fun appendNode(node: GameNode) {
        checkModelEditable()
        appendNode(model.currentNode, node)
    }

    /**
     * Appends a child node to an existing node (parent) on the tree.
     *
     * If the parent node has no children, the child move will be come its main branch
     * continuation. Otherwise, it becomes a variation. This method will not update
     * the current move to the newly appended node.
     *
     * Use [addMove] to append and set the current node.
     *
     * This method emits an [GameModel.onNodeAdd] event.
     */
    fun appendNode(parent: GameNode, child: GameNode) {
        checkModelEditable()
        model.gameTree.appendNode(parent, child)
        model.onNodeAdd().fireEvent(NodeEvent(child))
        model.isModified = true
    }

    internal fun checkModelEditable() {
        if (!isEditable) {
            throw IllegalStateException("${GameModel::class.simpleName} is not editable")
        }
    }
}