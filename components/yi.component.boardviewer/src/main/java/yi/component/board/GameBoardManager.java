package yi.component.board;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.common.BooleanProperty;
import yi.common.BooleanPropertyListener;
import yi.common.NullableProperty;
import yi.common.NullablePropertyListener;
import yi.common.audio.StoneAudioSet;
import yi.models.go.GameModel;
import yi.models.go.GameNode;

import java.util.Objects;
import java.util.Optional;

/**
 * Main controller class for {@link GameBoardCanvas} state. Receives UI and game events and manages the interaction
 * of subcomponents within the game module.
 */
public final class GameBoardManager {

    public final GameBoardSize size = new GameBoardSize();
    public final GameBoardView view = new GameBoardView();
    public final GameModelEditor edit = new GameModelEditor();
    public final GameBoardAudio audio = new GameBoardAudio();

    private final NullableProperty<GameNode> previewNode = new NullableProperty<>(null);
    private final BooleanProperty showCoordinates = new BooleanProperty(false);
    private boolean debugMode = false;
    private GameModel model = null;

    public GameBoardManager() { }

    public void setBoardCanvasSize(double componentWidth, double componentHeight, GameModel game) {
        size.compute(componentWidth, componentHeight, game.getBoardWidth(),
                game.getBoardHeight(), view.coordinateLabelPosition);
    }

    boolean isDebugMode() {
        return debugMode;
    }

    protected final void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    protected final void setGameModel(@NotNull GameModel gameModel) {
        var isSameModel = this.model == gameModel;
        this.model = Objects.requireNonNull(gameModel);

        if (!isSameModel) {
            edit.clearEditHistory();
        }
    }

    public @NotNull GameModel getGameModel() {
        Objects.requireNonNull(model, "No game model set. Use setGameModel() once " +
                "before calling getGameModel()");
        return model;
    }

    public boolean hasGameModel() {
        return model != null;
    }

    /**
     * Sets a node whose position will be rendered on the board in lieu of the
     * current node. The value may be {@code null}, in which case the current
     * node position (as given by {@link GameModel#getCurrentNode()} will be
     * drawn instead.
     *
     * @param node Node whose position to be rendered.
     */
    public void setPreviewNode(@Nullable GameNode node) {
        this.previewNode.set(node);
    }

    /**
     * @return Node whose position should be drawn over the current node position
     * on the game board.
     */
    public Optional<GameNode> getPreviewNode() {
        return previewNode.get();
    }

    public GameNode getNodeToShow() {
        return getPreviewNode().orElse(getGameModel().getCurrentNode());
    }

    public boolean isShowingCurrentPosition() {
        return getNodeToShow() == getGameModel().getCurrentNode();
    }

    public void addPreviewNodeChangeListener(NullablePropertyListener<GameNode> listener) {
        previewNode.addListener(listener);
    }

    public void addShowCoordinateValueListener(BooleanPropertyListener listener) {
        showCoordinates.addListener(listener);
    }

    public void setShowCoordinates(boolean doShow) {
        view.coordinateLabelPosition = doShow ? CoordinateLabelPosition.ALL_SIDES : CoordinateLabelPosition.NONE;
        if (hasGameModel() && size.hasComputedOnce()) {
            size.recompute(view.coordinateLabelPosition);
        }
        showCoordinates.set(doShow);
    }

    public boolean isShowingCoordinates() {
        return showCoordinates.get();
    }
}
