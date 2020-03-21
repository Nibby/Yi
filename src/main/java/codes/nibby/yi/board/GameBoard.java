package codes.nibby.yi.board;

import codes.nibby.yi.Sound;
import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameListener;
import codes.nibby.yi.game.GameNode;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The standard go board component used in the program.
 * This class manages all the graphical aspect of the <strong>Game</strong> object.
 * It consists of a stack of canvas layers in draw order (back to front):
 *
 * <ol>
 *     <li>BoardViewCanvas</li>
 *     <li>BoardInputCanvas</li>
 * </ol>
 *
 * <p>
 * Note that the canvas themselves are solely concerned with the rendering. Most of the
 * input handling logic are done here in the container class.
 * </p>
 */
public class GameBoard extends Pane implements GameListener {

    // TODO: Move me elsewhere.
    private static final double STONE_WOBBLE_FACTOR = 3d;

    private final BoardViewCanvas canvasView;
    private final BoardInputCanvas canvasInput;
    private final List<BoardCanvasLayer> canvasLayers;

    // Board render parameters
    private BoardMetrics metrics;

    // Controller to handle game board input logic
    private WeakReference<GameBoardController> controller;

    // The go game to be represented on the board
    private Game game;

    // A list of renderable objects (sourced from game.currentNode)
    private Stone[] stones;
    private BoardInputHintType inputHint = BoardInputHintType.DYNAMIC;

    public GameBoard(Game game, GameBoardController controller) {
        this.game = game;
        this.game.addGameListener(this);
        controller.setBoard(this); // Obey the GameBoardController contract.
        this.controller = new WeakReference<>(controller);
        this.metrics = new BoardMetrics();

        int capacity = game.getBoardWidth() * game.getBoardHeight();
        this.stones = new Stone[capacity];

        List<BoardCanvasLayer> canvasToUse = new ArrayList<>();
        {
            canvasToUse.add(canvasView = new BoardViewCanvas());
            canvasToUse.add(canvasInput = new BoardInputCanvas(this, controller));
        }
        canvasLayers = Collections.unmodifiableList(canvasToUse);
        canvasLayers.forEach(canvas -> getChildren().add(canvas));

        setCursor(Cursor.HAND);

        /*
            Maintains the bounds of the canvas stack upon resize.
         */
        widthProperty().addListener(e -> updateSize(getWidth(), getHeight()));
        heightProperty().addListener(e -> updateSize(getWidth(), getHeight()));
        updateSize(getWidth(), getHeight());
    }

    /**
     * Picks displayable game data from the current game node and add them to the
     * board for display.
     * <p>
     * If the <pre>flush</pre> flag is false, then the board objects will retain
     * what it already has, provided that the game node contains the same element too.
     * This is done so that the boardObjects map doesn't have to be reset and rebuilt
     * each time.
     * <p>
     * However, if the flush is set to true, then the entire boardObjects map is reset
     * and rebuilt from ground up.
     *
     * @param node  The current node data.
     * @param flush Whether to clear existing object cache first.
     */
    public void updateBoardObjects(GameNode node, boolean flush, boolean newMove) {
        if (flush) {
            int capacity = game.getBoardWidth() * game.getBoardHeight();
            this.stones = new Stone[capacity];
        }

        // First scan through the stone data on the current node
        int[] nodeStoneData = node.getStoneData();
        int boardWidth = game.getBoardWidth();

        for (int i = 0; i < nodeStoneData.length; i++) {
            int x = i % boardWidth;
            int y = i / boardWidth;

            // TODO: Implement this
            /*
                Scenarios:

                1. A stone exists in nodeStoneData[] but not in stone[] and versa
                    - The new stones from source is the difference in data between
                      the old and new nodes.
                    - Add the new ones to the screen,
                        TODO: static or animated? DEFAULT STATIC FOR NOW
                        TODO: how to just animate the stone once when it's placed?


                2. No data exists in nodeStoneData[] but it exists in stone[]
                    - User is probably backtracking in the game tree
                    - Remove all stones[] instances that do not match source

                3. Conflicting stone data exist in nodeStoneData[] and stone[]
                    - Always resort to source.

             */
            switch (nodeStoneData[i]) {
                case Stone.NONE:
                    // Source has no stones, therefore we should have none here too.
                    stones[i] = null;
                    break;
                case Stone.BLACK:
                    // Source has a black stone, adjust to black.
                    if (stones[i] != null)
                        stones[i].setColor(Stone.BLACK);
                    else
                        stones[i] = new Stone(Stone.BLACK, x, y);
                    break;
                case Stone.WHITE:
                    // Source has a white stone, adjust to white.
                    if (stones[i] != null)
                        stones[i].setColor(Stone.WHITE);
                    else
                        stones[i] = new Stone(Stone.WHITE, x, y);
                    break;
            }
        }
        // TODO: Adjust this later
        if (newMove) {
            Sound.playStonePlacement(null);
        }
    }

    @Override
    public void gameInitialized(Game game) {
        this.game = game;
        updateBoardObjects(game.getCurrentNode(), true, true);
        render();
    }

    @Override
    public void gameNodeUpdated(Game game, GameNode currentMove, boolean newMove) {
        // TODO: Should flush always be false here?
        updateBoardObjects(currentMove, false, newMove);

        render();
    }

    @Override
    public void gameModified(Game game) {

    }

    /*
        Invoked each time this component has been resized.
        All canvases will be resized and redrawn.
     */
    private void updateSize(double width, double height) {
        super.setPrefSize(width, height);
        layoutChildren();
        metrics.calibrate(this, game.getBoardWidth(), game.getBoardHeight());
        render();
    }

    public void render() {
        canvasLayers.forEach(canvas -> canvas.render(game, this));
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        canvasLayers.forEach(this::adjustBounds);
    }

    private void adjustBounds(Canvas canvas) {
        final double x = snappedLeftInset();
        final double y = snappedTopInset();
        final double w = snapSizeX(getWidth()) - x - snappedRightInset();
        final double h = snapSizeY(getHeight()) - y - snappedBottomInset();

        canvas.setLayoutX(x);
        canvas.setLayoutY(y);
        canvas.setWidth(w);
        canvas.setHeight(h);
    }

    public GameBoardController getController() {
        return controller.get();
    }

    public void setController(GameBoardController controller) {
        this.controller = new WeakReference<>(controller);
    }

    public void setGame(Game game) {
        this.game = game;
    }

    Game getGame() {
        return game;
    }

    public BoardMetrics getMetrics() {
        return metrics;
    }

    public Stone[] getAllRenderableStones() {
        return stones;
    }

    public BoardInputHintType getInputHint() {
        return inputHint;
    }

    public void setInputHint(BoardInputHintType inputHint) {
        this.inputHint = inputHint;
    }
}
