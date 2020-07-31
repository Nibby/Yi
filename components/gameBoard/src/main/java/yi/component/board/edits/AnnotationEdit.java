package yi.component.board.edits;

import yi.component.board.GameBoardModel;
import yi.core.go.Annotation;
import yi.core.go.GameModel;
import yi.core.go.GameNode;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class AnnotationEdit extends MoveBasedUndoableEdit {

    private final long sessionId; // When the mouse is dragged across the board, all annotations created are of the same session so that they can be merged.
    private final Set<Annotation> annotationsToAdd;
    private final Set<Annotation> annotationsToRemove;

    // Have static factory methods to instantiate this, prohibits Collections.unmodifiableSet() being used.
    AnnotationEdit(GameNode affectedMove, Set<Annotation> annotationsToAdd, Set<Annotation> annotationsToRemove, long sessionId) {
        super(affectedMove);
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
        gameModel.removeAnnotations(getAffectedMove(), annotationsToAdd);
        gameModel.addAnnotations(getAffectedMove(), annotationsToRemove);

        return true;
    }

    @Override
    protected boolean _performEdit(GameModel gameModel) {
        var annotations = getAffectedMove().getStateDelta().getAnnotationsOnThisNode();

        // TODO: As a defensive measure, we should probably validate the state is valid first before
        //       making any changes.
        annotations.removeAll(annotationsToRemove);
        gameModel.addAnnotationsToCurrentMove(annotationsToAdd);

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

    // Static factory methods
    public static AnnotationEdit forNew(GameBoardModel.GameNodeWrapper affectedMove, Annotation annotation, long sessionId) {
        // Cannot use Collections.singleton() because the annotation edit may merge more annotations if
        // they were made in rapid succession.
        var toAdd = new HashSet<Annotation>();
        toAdd.add(annotation);

        return new AnnotationEdit(affectedMove._internalNode(), toAdd, new HashSet<>(), sessionId);
    }

    public static AnnotationEdit forRemoval(GameBoardModel.GameNodeWrapper affectedNode, Annotation annotation, long sessionId) {
        // Cannot use Collections.singleton() because the annotation edit may merge more annotations if
        // they were made in rapid succession.
        var toRemove = new HashSet<Annotation>();
        toRemove.add(annotation);

        return new AnnotationEdit(affectedNode._internalNode(), new HashSet<>(), toRemove, sessionId);
    }
}
