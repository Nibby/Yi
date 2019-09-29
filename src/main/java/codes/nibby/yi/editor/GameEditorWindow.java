package codes.nibby.yi.editor;

import codes.nibby.yi.Yi;
import codes.nibby.yi.board.GameBoard;
import codes.nibby.yi.config.Config;
import codes.nibby.yi.config.UiStylesheets;
import codes.nibby.yi.editor.component.GameBoardToolBar;
import codes.nibby.yi.editor.component.MoveCommentPane;
import codes.nibby.yi.editor.component.GameEditorMenuBar;
import codes.nibby.yi.editor.component.GameTreePane;
import codes.nibby.yi.editor.layout.LayoutType;
import codes.nibby.yi.editor.layout.AbstractLayout;
import codes.nibby.yi.game.Game;
import codes.nibby.yi.game.GameListener;
import codes.nibby.yi.game.GameNode;
import codes.nibby.yi.game.rules.GameRules;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * The main editor window.
 * Mainly deals with UI layout within the editor.
 *
 * @author Kevin Yang
 * Created on 29 August 2019
 */
public class GameEditorWindow extends Stage {

    private static final int START_WIDTH = 800;
    private static final int START_HEIGHT = 600;

    private Game game;
    private GameBoard gameBoard;
    private EditorBoardController controller;
    private GameTreePane gameTreePane;
    private MoveCommentPane moveCommentPane;
    private GameEditorMenuBar toolBar;
    private GameBoardToolBar boardToolBar;

    private AbstractLayout layout;
    private Scene scene;

    public GameEditorWindow() {
        controller = new EditorBoardController();
        initializeComponents();
        initializeScene();
        game.initialize();

        UiStylesheets.applyTo(scene);
    }

    /*
        Instantiates all the required objects for the window.
        The components are not added to the scene, that is
        handled by the perspective manager.
     */
    private void initializeComponents() {
        game = new Game(GameRules.CHINESE, 19, 19);
        boardToolBar = new GameBoardToolBar(this);
        gameBoard  = new GameBoard(game, controller, boardToolBar);
        toolBar = new GameEditorMenuBar(this);
        gameTreePane = new GameTreePane(this);
        moveCommentPane = new MoveCommentPane(this);

        game.addGameListener(gameTreePane);
    }


    private void initializeScene() {
        setTitle(Yi.TITLE);

        layout = AbstractLayout.generate(this);
        Pane root = layout.getContentPane();
        scene = new Scene(root, START_WIDTH, START_HEIGHT);
        setScene(scene);
    }

    public LayoutType getPerspective() {
        return Config.getEditorLayout();
    }

    public void setPerspective(LayoutType perspective) {
        LayoutType oldPerspective = Config.getEditorLayout();
        if (oldPerspective.equals(perspective))
            return;

        Config.setEditorLayout(perspective);
        layout = AbstractLayout.generate(this);
        Pane root = layout.getContentPane();
        scene.setRoot(root);
    }

    public Game getGame() {
        return game;
    }

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    public GameTreePane getGameTreePane() {
        return gameTreePane;
    }

    public MoveCommentPane getMoveCommentPane() {
        return moveCommentPane;
    }

    public GameEditorMenuBar getToolBar() {
        return toolBar;
    }

    public EditorBoardController getController() {
        return controller;
    }

    public AbstractLayout getLayout() {
        return layout;
    }
}
