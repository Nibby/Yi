package yi.component.board.edits;

import yi.models.go.GameNode;

import java.util.Optional;

public final class PlayMoveEditAccessor {

    private PlayMoveEditAccessor() {

    }

    public static Optional<GameNode> getGameNode(PlayMoveEdit edit) {
        return Optional.ofNullable(edit.getSubmittedNode());
    }
}
