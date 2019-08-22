package codes.nibby.qipan.board;

import codes.nibby.qipan.game.GoGame;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;

/**
 * The standard go board component used in the program.
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
public class GameBoard extends Pane {

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
    private GoGame game;

    public GameBoard(GoGame game, GameBoardController controller) {
        this.game = game;
        this.controller = controller;
        this.metrics = new BoardMetrics();
        this.metrics.calibrate(this);

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

    /*
        Invoked each time this component has been resized.
        All canvases will be resized and redrawn.
     */
    private void updateSize(double width, double height) {
        super.setPrefSize(width, height);
        metrics.calibrate(this);
        layoutChildren();
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
        final double w = snapSize(getWidth()) - x - snappedRightInset();
        final double h = snapSize(getHeight()) - y - snappedBottomInset();

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

    public GoGame getGame() {
        return game;
    }

    public void setGame(GoGame game) {
        this.game = game;
        // TODO: update board graphics to display new game
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
}
