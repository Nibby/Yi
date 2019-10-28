package codes.nibby.yi.game.rules;

import codes.nibby.yi.game.GameNode;

public class ProposalResult {

    private Type type;
    private GameNode newNode;
    public ProposalResult(Type type) {
        this(type, null);
    }

    public ProposalResult(Type type, GameNode newNode) {
        this.type = type;
        this.newNode = newNode;
    }

    public GameNode getNewNode() {
        return newNode;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        SUCCESS,
        INVALID_KO,
        INVALID_REPEATING,
        INVALID_COLOR_MISMATCH,
        INVALID_STONE_EXISTS,
        INVALID_SUICIDE,
        INVALID_UNKNOWN_ERROR,
    }
}
