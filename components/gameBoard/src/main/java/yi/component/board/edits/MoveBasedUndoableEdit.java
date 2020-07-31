package yi.component.board.edits;

import org.jetbrains.annotations.NotNull;
import yi.core.go.GameNode;

import java.util.Objects;

public abstract class MoveBasedUndoableEdit extends UndoableEdit {

    private final GameNode affectedMove;
    
    public MoveBasedUndoableEdit(@NotNull GameNode affectedMove) {
        this.affectedMove = Objects.requireNonNull(affectedMove);
    }

    public GameNode getAffectedMove() {
        return affectedMove;
    }
}
