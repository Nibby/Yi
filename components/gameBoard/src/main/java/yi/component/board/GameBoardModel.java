package yi.component.board;

import yi.component.board.edits.Undoable;
import yi.core.go.*;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * The main interface between the {@link GameModel} and the rest of the board modules.
 */
public final class GameBoardModel {

    private GameModel gameModel;
    private GameState currentGameState;

    GameBoardModel() { }

    public void setGameModel(GameModel gameModel) {
        this.gameModel = gameModel;
        update(gameModel);
    }

    public void update(GameModel gameModel) {
        if (this.gameModel != gameModel) {
            this.gameModel = gameModel;
        }

        this.currentGameState = this.gameModel.getCurrentGameState();
    }

    // Must not be public because its state can be modified.
    GameState getCurrentGameState() {
        return currentGameState;
    }

    public GamePosition getCurrentGamePosition() {
        return getCurrentGameState().getGamePosition();
    }

    public int getBoardWidth() {
        return gameModel.getBoardWidth();
    }

    public int getBoardHeight() {
        return gameModel.getBoardHeight();
    }

    public StoneColor getNextTurnStoneColor() {
        return gameModel.getNextTurnStoneColor();
    }

    public int getCurrentMoveNumber() {
        return gameModel.getCurrentMoveNumber();
    }

    public int getNextMoveNumber() {
        return gameModel.getNextMoveNumber();
    }

    public void toPreviousMove() {
        gameModel.toPreviousMove();
    }

    public void toNextMove() {
        gameModel.toNextMove();
    }

    /**
     *
     * @return A {@link GameNode} wrapper which exposes some properties of the current move.
     */
    public GameNodeWrapper getCurrentMove() {
        return new GameNodeWrapper(gameModel.getCurrentMove());
    }

    // TODO: Move this to the editing model
//    public void removeAnnotationsOnCurrentMove(int gridX, int gridY) {
//        gameModel.removeAnnotationsFromCurrentMove(gridX, gridY);
//    }

    /**
     * This must be package-private because we should never expose the model for other classes to access
     * other than {@link GameModelEditor}.
     *
     * @return The game model if it is set.
     */
    Optional<GameModel> getGameModel() {
        return Optional.ofNullable(gameModel);
    }

    /**
     *
     * @return An immutable set of all annotations that are currently on this node.
     */
    public Set<Annotation> getAllAnnotationsOnCurrentMove() {
        return Collections.unmodifiableSet(gameModel.getAnnotationsOnCurrentMove());
    }

    /**
     * Wraps a {@link GameModel} and exposes only methods that do not permit the modification of game state.
     * Use {@link GameModelEditor#recordAndApply(Undoable, GameBoardManager)} to edit to the game model.
     */
    public static final class GameNodeWrapper {
        private final GameNode node;

        private GameNodeWrapper(GameNode node) {
            this.node = node;
        }

        public Optional<Stone> getPrimaryMove() {
            var primaryMove = node.getStateDelta().getPrimaryMove();
            return Optional.ofNullable(primaryMove);
        }

        public Set<Annotation> getAnnotations() {
            var annotations = node.getStateDelta().getAnnotationsOnThisNode();
            return Collections.unmodifiableSet(annotations);
        }

        public boolean hasAnnotationAt(int x, int y) {
            return getAnnotations().stream().anyMatch(annotation -> annotation.isOccupyingPosition(x, y));
        }

        public Optional<Annotation> getAnnotationAt(int x, int y) {
            return getAnnotations().stream().filter(annotation -> annotation.isOccupyingPosition(x, y)).findAny();
        }

        /**
         * <b>Do not use this method to update node state</b>. Use {@link GameModelEditor#recordAndApply(Undoable, GameBoardManager)}
         * and its variants to make changes to the game model. Failure to do so breaks the undo/redo mechanism.
         *
         * @return The underlying game node wrapped by this class.
         */
        // FIXME: Due to the current package structure, it is unfortunate that we have to expose this in order for annotation edits to work.
        //        If the project adopts the jigsaw setup, remove this.
        public GameNode _internalNode() {
            return node;
        }
    }
}
