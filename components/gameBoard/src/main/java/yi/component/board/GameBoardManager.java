package yi.component.board;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yi.component.ValueListener;
import yi.component.ValueListenerManager;
import yi.core.go.GameModel;
import yi.core.go.GameNode;

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

    private GameNode previewNode = null;
    private boolean debugMode = false;
    private GameModel model = null;

    private final ValueListenerManager<GameNode> previewNodeChangeListener = new ValueListenerManager<>();

    GameBoardManager() { }

    public void setBoardCanvasSize(double componentWidth, double componentHeight, GameModel game) {
        size.compute(componentWidth, componentHeight, game.getBoardWidth(),
                game.getBoardHeight(), view.coordinateLabelPosition);
    }

    public void onGameUpdate(GameModel game) {

    }

    boolean isDebugMode() {
        return debugMode;
    }

    void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public void setGameModel(@NotNull GameModel gameModel) {
        // TODO: This method was made public because of test cases. Consider using
        //       jigsaw project structure to sort out the method visibilities.
        this.model = Objects.requireNonNull(gameModel);
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
        this.previewNode = node;
        previewNodeChangeListener.fireValueChanged(node);
    }

    /**
     * @return Node whose position should be drawn over the current node position
     * on the game board.
     */
    public Optional<GameNode> getPreviewNode() {
        return Optional.ofNullable(previewNode);
    }

    public GameNode getNodeToShow() {
        return getPreviewNode().orElse(getGameModel().getCurrentNode());
    }

    public boolean isShowingCurrentPosition() {
        return getNodeToShow() == getGameModel().getCurrentNode();
    }

    public void addPreviewNodeChangeListener(ValueListener<GameNode> listener) {
        previewNodeChangeListener.addListener(listener);
    }
}
