package yi.component.board.edits;

import yi.core.go.Annotation;
import yi.core.go.GameModel;
import yi.core.go.GameNode;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class AnnotationEdit extends UndoableEdit {

    private final GameNode affectedNode;
    private final long sessionId; // When the mouse is dragged across the board, all annotations created are of the same session so that they can be merged.
    private final Set<Annotation> annotationsToAdd;
    private final Set<Annotation> annotationsToRemove;

    // Have static factory methods to instantiate this, prohibits Collections.unmodifiableSet() being used.
    AnnotationEdit(GameNode affectedNode, Set<Annotation> annotationsToAdd, Set<Annotation> annotationsToRemove, long sessionId) {
        this.affectedNode = Objects.requireNonNull(affectedNode);
        this.annotationsToAdd = annotationsToAdd;
        this.annotationsToRemove = annotationsToRemove;
        this.sessionId = sessionId;
    }

    public boolean isSameSession(AnnotationEdit anotherEdit) {
        return this.sessionId == anotherEdit.sessionId;
    }

    @Override
    protected boolean _rollbackEdit(GameModel gameModel) {
        // TODO: This will actually fire two node update events, which isn't ideal.
        //       Change the API so that multiple events can be done in one "transaction" and have one event firing...
        gameModel.removeAnnotations(getAffectedNode(), annotationsToAdd);
        gameModel.addAnnotations(getAffectedNode(), annotationsToRemove);

        return true;
    }

    @Override
    protected boolean _performEdit(GameModel gameModel) {
        // TODO: As a defensive measure, we should probably validate the state is valid first before
        //       making any changes.
        gameModel.removeAnnotations(getAffectedNode(), annotationsToRemove);
        gameModel.addAnnotationsToCurrentNode(annotationsToAdd);

        return true;
    }

    @Override
    protected boolean canRollback() {
        return true; // TODO: I forgot what this is supposed to do.
    }

    /**
     * Merges the edits from the provided annotation edit with this one. After merging, the provided edit can
     * be safely discarded.
     *
     * @param undoable The annotation to merge.
     */
    public void merge(AnnotationEdit undoable) {
        undoable.annotationsToAdd.forEach(toAdd -> {
            this.annotationsToRemove.remove(toAdd);
            this.annotationsToAdd.add(toAdd);
        });

        undoable.annotationsToRemove.forEach(toRemove -> {
            this.annotationsToAdd.remove(toRemove);
            this.annotationsToRemove.add(toRemove);
        });
    }

    public GameNode getAffectedNode() {
        return affectedNode;
    }

    // Static factory methods
    public static AnnotationEdit forNew(GameNode nodeToEdit, Annotation annotation, long sessionId) {
        // Cannot use Collections.singleton() because the annotation edit may merge more annotations if
        // they were made in rapid succession.
        var toAdd = new HashSet<Annotation>();
        toAdd.add(annotation);

        return new AnnotationEdit(nodeToEdit, toAdd, new HashSet<>(), sessionId);
    }

    public static AnnotationEdit forRemoval(GameNode nodeToEdit, Annotation annotation, long sessionId) {
        // Cannot use Collections.singleton() because the annotation edit may merge more annotations if
        // they were made in rapid succession.
        var toRemove = new HashSet<Annotation>();
        toRemove.add(annotation);

        return new AnnotationEdit(nodeToEdit, new HashSet<>(), toRemove, sessionId);
    }
}
