package codes.nibby.yi.board;

import codes.nibby.yi.Sound;
import codes.nibby.yi.editor.EditorToolType;
import codes.nibby.yi.editor.GameEditorWindow;
import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameListener;
import codes.nibby.yi.game.GameNode;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.InputStream;
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

    /**
     * An optional fx component can be placed around the edges of the board
     * for aesthetic purposes. The board metrics will make space accordingly.
     *
     * TODO: This is a hack, refactor me.
     */
    private ToolBar topToolBar;

    private final BoardViewCanvas canvasView;
    private final BoardInputCanvas canvasInput;
    private final List<BoardCanvasLayer> canvasLayers;

    // Board render parameters
    private BoardMetrics metrics;

    // Controller to handle game board input logic
    private GameBoardController controller;

    // The go game to be represented on the board
    private Game game;

    // A list of renderable objects (sourced from game.currentNode)
    private Stone[] stones;
    private BoardInputHintType inputHint = BoardInputHintType.DYNAMIC;

    public GameBoard(Game game, GameBoardController controller, ToolBar toolbar) {
        this.game = game;
        this.controller = controller;
        this.controller.initialize(game, this);
        this.topToolBar = toolbar;
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

        if (getTopToolBar() != null) {
            getChildren().add(canvasToUse.size(), topToolBar);
            StackPane.setAlignment(topToolBar, Pos.TOP_CENTER);
        }

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
    public void gameNodeUpdated(GameNode currentMove, boolean newMove) {
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
        if (topToolBar != null)
            topToolBar.setPrefWidth(width);
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
        return controller;
    }

    public void setController(GameBoardController controller) {
        this.controller = controller;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public BoardMetrics getMetrics() {
        return metrics;
    }

    public Stone[] getAllRenderableStones() {
        return stones;
    }

    public ToolBar getTopToolBar() {
        return topToolBar;
    }

    public void setTopToolBar(ToolBar topToolBar) {
        this.topToolBar = topToolBar;
    }

    public BoardInputHintType getInputHint() {
        return inputHint;
    }

    public void setInputHint(BoardInputHintType inputHint) {
        this.inputHint = inputHint;
    }

    /**
     * A HUD toolbar above the game board, offers input controls.
     *
     * @author Kevin Yang
     * Created on 29 August 2019
     */
    public static class GameBoardToolBar extends ToolBar {

        public static final int BUTTON_SIZE = BoardMetrics.RESERVED_TOOLBAR_SIZE - 15;

        private GameEditorWindow editor;

        private Label moveLabel;
        private ToggleGroup annotationGroup;
        private ToggleButton buttonPlay;
        private ToggleButton buttonTriangle;
        private ToggleButton buttonCircle;
        private ToggleButton buttonSquare;
        private ToggleButton buttonCross;
        private ToggleButton buttonNumber;
        private ToggleButton buttonLetter;
        private ToggleButton buttonMarkBlack;
        private ToggleButton buttonMarkWhite;
        private ToggleButton buttonCoordinates;
        private Button buttonShowTray;

        public GameBoardToolBar(GameEditorWindow editor) {
            this.editor = editor;
            annotationGroup = new ToggleGroup();

            Label lbTools = new Label("Tools: ");
            lbTools.getStyleClass().add("label");
            getItems().add(lbTools);

            addToolButton(buttonPlay = new ToggleButton(), "play", "Place stones", true);
            buttonPlay.selectedProperty().addListener(e -> {
                editor.getController().setToolType(EditorToolType.PLAY_MOVE);
                if (editor.getGameBoard() != null)
                    editor.getGameBoard().setInputHint(BoardInputHintType.DYNAMIC);
            });

            Label lbSep1 = new Label("-");
            lbSep1.getStyleClass().add("label");
            getItems().add(lbSep1);

            addToolButton(buttonMarkBlack = new ToggleButton(), "circle-thick", "Add black stones", true);
            buttonMarkBlack.selectedProperty().addListener(e -> {
                editor.getController().setToolType(EditorToolType.ADD_HELPER_BLACK);
                if (editor.getGameBoard() != null)
                    editor.getGameBoard().setInputHint(BoardInputHintType.STONE_BLACK);
            });

            addToolButton(buttonMarkWhite = new ToggleButton(), "circle-thick-fill", "Add white stones", true);
            buttonMarkWhite.selectedProperty().addListener(e -> {
                editor.getController().setToolType(EditorToolType.ADD_HELPER_WHITE);
                if (editor.getGameBoard() != null)
                    editor.getGameBoard().setInputHint(BoardInputHintType.STONE_WHITE);
            });

            Label lbSep2 = new Label("-");
            lbSep2.getStyleClass().add("label");
            getItems().add(lbSep2);

            addToolButton(buttonTriangle = new ToggleButton(), "triangle", "Triangle marker", true);
            buttonTriangle.selectedProperty().addListener(e -> {
                editor.getController().setToolType(EditorToolType.MARKUP_TRIANGLE);
                if (editor.getGameBoard() != null)
                    editor.getGameBoard().setInputHint(BoardInputHintType.MARKUP_TRIANGLE);
            });

            addToolButton(buttonSquare = new ToggleButton(), "square", "Square marker", true);
            buttonSquare.selectedProperty().addListener(e -> {
                editor.getController().setToolType(EditorToolType.MARKUP_SQUARE);
                if (editor.getGameBoard() != null)
                    editor.getGameBoard().setInputHint(BoardInputHintType.MARKUP_SQUARE);
            });

            addToolButton(buttonCircle = new ToggleButton(), "circle", "Circle marker", true);
            buttonCircle.selectedProperty().addListener(e -> {
                editor.getController().setToolType(EditorToolType.MARKUP_CIRCLE);
                if (editor.getGameBoard() != null)
                    editor.getGameBoard().setInputHint(BoardInputHintType.MARKUP_CIRCLE);
            });

            addToolButton(buttonCross = new ToggleButton(), "cross", "Cross marker", true);
            buttonCross.selectedProperty().addListener(e -> {
                editor.getController().setToolType(EditorToolType.MARKUP_CROSS);
                if (editor.getGameBoard() != null)
                    editor.getGameBoard().setInputHint(BoardInputHintType.MARKUP_CROSS);
            });

            addToolButton(buttonNumber = new ToggleButton("1"), "", "Number marker", true);
            buttonNumber.selectedProperty().addListener(e -> {
                editor.getController().setToolType(EditorToolType.MARKUP_LABEL_NUMBER);
                if (editor.getGameBoard() != null)
                    editor.getGameBoard().setInputHint(BoardInputHintType.MARKUP_LABEL);
            });

            addToolButton(buttonLetter = new ToggleButton("A"), "", "Letter marker", true);
            buttonLetter.selectedProperty().addListener(e -> {
                editor.getController().setToolType(EditorToolType.MARKUP_LABEL_LETTER);
                if (editor.getGameBoard() != null)
                    editor.getGameBoard().setInputHint(BoardInputHintType.MARKUP_LABEL);
            });

            HBox divider = new HBox();
            HBox.setHgrow(divider, Priority.ALWAYS);
            getItems().add(divider);

            addToolButton(buttonCoordinates = new ToggleButton(), "grid", "Show grid labels", false);
            buttonCoordinates.selectedProperty().addListener(e -> {
//            editor.getGobanEditor().setDrawLabels(buttonShowLabel.isSelected());
            });

            // TODO somehow verify the intial stat

            buttonShowTray = new Button("", null);
            buttonShowTray.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
            ImageView buttonShowTrayIcon = new ImageView(new Image(GameBoardToolBar.class.getResourceAsStream("/icons/arrow_right_invert.png")));
            buttonShowTrayIcon.setFitWidth(BUTTON_SIZE);
            buttonShowTrayIcon.setFitHeight(BUTTON_SIZE);
            buttonShowTray.setGraphic(buttonShowTrayIcon);
            buttonShowTray.setOnAction(evt -> {
                boolean newState = !editor.getLayout().isShowingRightSidebar();
                editor.getLayout().setShowRightSidebar(newState);
                String iconPath;
                if (newState == true)
                    iconPath = "/icons/arrow_down_invert.png";
                else
                    iconPath = "/icons/arrow_right_invert.png";
                ImageView icon = new ImageView(new Image(GameBoardToolBar.class.getResourceAsStream(iconPath)));
                icon.setFitWidth(BUTTON_SIZE);
                icon.setFitHeight(BUTTON_SIZE);
                buttonShowTray.setGraphic(icon);
            });
            getItems().add(buttonShowTray);

            annotationGroup.selectToggle(buttonPlay);

            getStyleClass().add("editor_board_tb");
            setMinHeight(BoardMetrics.RESERVED_TOOLBAR_SIZE);
            setMaxHeight(BoardMetrics.RESERVED_TOOLBAR_SIZE);
            setPrefHeight(BoardMetrics.RESERVED_TOOLBAR_SIZE);
            setOrientation(Orientation.HORIZONTAL);
        }

        private void addToolButton(ToggleButton b, String icon, String tooltip, boolean group) {
            b.applyCss();
            Tooltip tt = new Tooltip(tooltip);
            tt.setShowDelay(Duration.millis(500));
            tt.setHideDelay(Duration.millis(500));
            tt.setShowDuration(Duration.seconds(2));
            b.setTooltip(tt);
            b.setPrefWidth(BUTTON_SIZE);
            b.setPrefHeight(b.getPrefWidth());
            b.getStyleClass().add("editor_board_tb_toggle");

            if (!icon.isEmpty()) {
                InputStream inputStream = GameBoardToolBar.class.getResourceAsStream("/icons/" + icon + "_invert.png");
                ImageView icn = new ImageView(new Image(inputStream));
                icn.setFitWidth(b.getPrefWidth() - 10);
                icn.setFitHeight(b.getPrefHeight() - 10);
                b.setGraphic(icn);
            }

            if (group) {
                b.selectedProperty().addListener(e -> {
                    boolean selected = b.isSelected();
                    if (!selected) {
                        boolean hasSelection = false;
                        for (Toggle toggle : annotationGroup.getToggles()) {
                            if (toggle.isSelected()) {
                                hasSelection = true;
                                break;
                            }
                        }

                        if (!hasSelection)
                            b.setSelected(true);
                    }
                });
                annotationGroup.getToggles().add(b);
            }

            b.selectedProperty().addListener(e -> {
                boolean selected = b.isSelected();
                if (!icon.isEmpty()) {
                    InputStream inputStream = GameBoardToolBar.class
                            .getResourceAsStream("/icons/" + icon + ((selected) ? "" : "_invert") + ".png");
                    ImageView iconImage = new ImageView(new Image(inputStream));
                    iconImage.setFitHeight(b.getPrefHeight() - 10);
                    iconImage.setFitWidth(b.getPrefWidth() - 10);

                    b.setGraphic(iconImage);
                }
            });
            getItems().add(b);
        }
    }
}
