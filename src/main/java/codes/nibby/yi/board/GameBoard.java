package codes.nibby.yi.board;

import codes.nibby.yi.config.Config;
import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameListener;
import codes.nibby.yi.game.GameNode;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;

import java.util.*;

/**
 * The standard go board component used in the program.
 * This class manages all the graphical aspect of the <strong>Game</strong> object.
 * It consists of a stack of <strong>three</strong> canvas layers,
 * in draw order (back to front):
 *
 * <ol>
 *     <li>BoardBackgroundCanvas</li>
 *     <li>BoardInputCanvas</li>
 *     <li>BoardStaticCanvas</li>
 * </ol>
 *
 * <strong>Layer 1: BoardBackgroundCanvas</strong>
 * The first layer displays the grid, star points, coordinate labels, and most
 * importantly, stone shadows. Shadows are kept in their own layer for stone
 * animation purposes.
 *
 * @see BoardBackgroundCanvas
 *
 * <strong>Layer 2: BoardStaticCanvas</strong>
 * This layer draws all static objects on the go board. These include placed stones,
 * annotations.
 *
 * @see BoardStaticCanvas
 *
 * <strong>Layer 3: BoardInputCanvas</strong>
 * The input canvas handles all mouse and keyboard input, redraw cursor indicators
 * without redrawing the entire board. Furthermore, the input canvas serves as a
 * fine ground for stone placement animations (again without redrawing the rest
 * of the board).
 *
 * @see BoardInputCanvas
 *
 * Note that the canvas themselves are solely concerned with the rendering. Most of the
 * logic behind the canvas are done here in the 'parent' class.
 *
 * @author Kevin Yang
 * created on 23 August 2019
 */
public class GameBoard extends Pane implements GameListener {

    private static final double STONE_WOBBLE_FACTOR = 3d;

    /*
        The three stacked canvas layers
     */
    private BoardBackgroundCanvas canvasBg;
    private BoardStaticCanvas canvasStatic;
    private BoardInputCanvas canvasInput;

    // Board render parameters
    private BoardMetrics metrics;

    // Controller to handle game board input logic
    private GameBoardController controller;

    // The go game to be represented on the board
    private Game game;

    // A list of renderable objects (sourced from game.currentNode)
    private Stone[] stones;
    private List<Stone> stonesStatic, stonesAnimated;

    public GameBoard(Game game, GameBoardController controller) {
        this.game = game;
        this.game.addGameListener(this);
        this.controller = controller;
        this.controller.initialize(game, this);
        this.metrics = new BoardMetrics();
        this.metrics.calibrate(this);

        int capacity = game.getBoardWidth() * game.getBoardHeight();
        this.stones = new Stone[capacity];
        this.stonesAnimated = new ArrayList<>();
        this.stonesStatic = new ArrayList<>();
        canvasBg = new BoardBackgroundCanvas(this);
        canvasStatic = new BoardStaticCanvas(this);
        canvasInput = new BoardInputCanvas(this);
        getChildren().add(0, canvasBg);
        getChildren().add(1, canvasStatic);
        getChildren().add(2, canvasInput);

        setCursor(Cursor.HAND);

        /*
            Maintains the bounds of the canvas stack upon resize.
         */
        widthProperty().addListener(e -> updateSize(getWidth(), getHeight()));
        heightProperty().addListener(e -> updateSize(getWidth(), getHeight()));
    }

    /**
     * Picks displayable game data from the current game node and add them to the
     * board for display.
     *
     * If the <pre>flush</pre> flag is false, then the board objects will retain
     * what it already has, provided that the game node contains the same element too.
     * This is done so that the boardObjects map doesn't have to be reset and rebuilt
     * each time.
     *
     * However, if the flush is set to true, then the entire boardObjects map is reset
     * and rebuilt from ground up.
     *
     * @param node The current node data.
     * @param flush Whether to clear existing object cache first.
     */
    public void updateBoardObjects(GameNode node, boolean flush, boolean newMove) {
        if (flush) {
            int capacity = game.getBoardWidth() * game.getBoardHeight();
            this.stones = new Stone[capacity];
        }
        stonesStatic.clear();
        stonesAnimated.clear();

        // First scan through the stone data on the current node
        int[] nodeStoneData = node.getStoneData();
        int boardWidth = getGame().getBoardWidth();

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

            // TODO: Adjust this later
            if (stones[i] != null) {
                // Check if it's a brand new move. If so, wobble it if applicable.
                boolean wobble = false;
                if (newMove) {
                    int[] move = node.getCurrentMove();
                    if (x == move[0] && y == move[1]) {
                        stones[i].setWobble(STONE_WOBBLE_FACTOR);
                        stonesAnimated.add(stones[i]);
                        wobble = true;

                        // Displace the nearby stones (if fuzzy effect is on)
                        // TODO: Use bigCollision parameter to determine sound effect
                        boolean bigCollision = false;
                        boolean snap = false;
                        List<Stone> wobbles = new ArrayList<>();
                        wobbles.add(stones[i]);
                        List<Integer> adjacentPoints = game.getNeighborIndices(x, y);
                        List<Stone> adjacent = new ArrayList<>();
                        adjacentPoints.forEach(pt -> adjacent.add(stones[pt]));
                        for (Stone s : adjacent) {
                            if (s == null)
                                continue;

                            if (Math.abs(s.getY() - y) == 1 || (int) (Math.random() * 3) == 1) {
                                double wobbleAmount = (Math.abs(s.getY() - y) == 1)
                                        ? STONE_WOBBLE_FACTOR
                                        : (Math.random() + 0.1d) * STONE_WOBBLE_FACTOR / 2;
                                if (Math.abs(s.getY() - y) == 1) {
                                    stones[i].setWobble(wobbleAmount);
                                    snap = true;
                                } else {
                                    s.setWobble(wobbleAmount);
                                    s.nudge(s.getX() - x, s.getY() - y, metrics);
                                }
                                wobbles.add(s);
                                // Collision detection
                                if ((int) (Math.random() * 5) < 2) {
                                    adjacentPoints = game.getNeighborIndices(s.getX(), s.getY());
                                    List<Stone> adjacent2 = new ArrayList<>();
                                    adjacentPoints.forEach(pt -> adjacent2.add(stones[pt]));
                                    if (adjacent2.size() >= 2)
                                        bigCollision = true;
                                    for (Stone ss : adjacent2) {
                                        if (ss == null)
                                            continue;
                                        if (ss.equals(stones[i]) || ss.equals(s))
                                            continue;
                                        ss.setWobble((Math.random() + 0.1d) * STONE_WOBBLE_FACTOR / 2);
                                        ss.nudge(s.getX() - x, s.getY() - y, metrics);
                                        wobbles.add(ss);
                                    }
                                }
                            }
                        }
                    }
                }

                if (!wobble) {
                    if (stones[i].shouldWobble())
                        stonesAnimated.add(stones[i]);
                    else
                        stonesStatic.add(stones[i]);
                }
            }
        }
    }

    @Override
    public void gameInitialized(Game game) {

    }

    @Override
    public void gameCurrentMoveUpdate(GameNode currentMove, boolean newMove) {
        // TODO: Should flush always be false here?
        updateBoardObjects(currentMove, false, newMove);

        render();
    }

    /*
        Invoked each time this component has been resized.
        All canvases will be resized and redrawn.
     */
    private void updateSize(double width, double height) {
        super.setPrefSize(width, height);
        layoutChildren();
        metrics.calibrate(this);
        render();
    }

    public void render() {
        canvasBg.render();
        canvasStatic.render();
        canvasInput.render();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        adjustBounds(canvasBg);
        adjustBounds(canvasStatic);
        adjustBounds(canvasInput);
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
        return controller;
    }

    public void setController(GameBoardController controller) {
        this.controller = controller;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
        updateBoardObjects(game.getCurrentNode(), true, false);
    }

    protected BoardInputCanvas getInputCanvas() {
        return canvasInput;
    }

    protected BoardStaticCanvas getStaticCanvas() {
        return canvasStatic;
    }

    protected BoardBackgroundCanvas getBackgroundCanvas() {
        return canvasBg;
    }

    public BoardMetrics getMetrics() {
        return metrics;
    }

    public Stone[] getAllRenderableStones() {
        return stones;
    }

    public List<Stone> getStaticStones() {
        return stonesStatic;
    }

    public List<Stone> getAnimatedStones() {
        return stonesAnimated;
    }
}
